import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { switchMap, tap } from 'rxjs';
import { PacienteService } from '../../../../core/services/paciente.service';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import {
  MchatService,
  MchatRespuestasResponse,
  MchatSubmit,
} from '../../../../core/services/mchat.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { MchatPreguntasComponent } from '../../../../shared/mchat-preguntas/mchat-preguntas.component';
import { ModalContainerComponent } from '../../../../shared/modal-container/modal-container.component';

@Component({
  selector: 'app-mchat-section',
  imports: [StatusBadgeComponent, MchatPreguntasComponent, ModalContainerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './mchat-section.component.html',
})
export class MchatSectionComponent {
  private readonly pacienteService = inject(PacienteService);
  private readonly mchatService    = inject(MchatService);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  mchatRespuestas        = signal<MchatRespuestasResponse | null>(null);
  mchatRespuestasLoading = signal(false);

  showModal     = signal(false);
  editMode      = signal(false);
  saving        = signal(false);
  saveError     = signal<string | null>(null);
  reenviarError = signal<string | null>(null);

  readonly mchatRespuestasAsArray = computed((): boolean[] | null => {
    const r = this.mchatRespuestas();
    if (!r) return null;
    return Array.from({ length: 20 }, (_, i) => !!(r[`p${i + 1}` as keyof MchatRespuestasResponse]));
  });

  readonly mchatEstadoLabels: Record<string, string> = {
    PENDIENTE: 'Pendiente', COMPLETADO: 'Completado',
    EXPIRADO: 'Expirado', NO_ENVIADO: 'Sin enviar',
  };
  readonly mchatEstadoColors: Record<string, string> = {
    PENDIENTE:  'bg-warning-light text-warning',
    COMPLETADO: 'bg-accent-light text-accent',
    EXPIRADO:   'bg-danger-light text-danger',
    NO_ENVIADO: 'bg-background text-text-muted border border-border',
  };
  readonly riesgoLabels: Record<string, string> = {
    BAJO_RIESGO: 'Bajo riesgo', MEDIANO_RIESGO: 'Riesgo mediano', ALTO_RIESGO: 'Alto riesgo',
  };
  readonly riesgoColors: Record<string, string> = {
    BAJO_RIESGO:    'bg-accent-light text-accent',
    MEDIANO_RIESGO: 'bg-warning-light text-warning',
    ALTO_RIESGO:    'bg-danger-light text-danger',
  };

  openModal(): void {
    this.saveError.set(null);
    const p = this.paciente();
    const hasRespuestas = p.mchatEstado === 'COMPLETADO';
    this.editMode.set(!hasRespuestas);
    if (hasRespuestas && !this.mchatRespuestas()) {
      this.mchatRespuestasLoading.set(true);
      this.mchatService.getRespuestasByPaciente(p.id).subscribe({
        next: r => { this.mchatRespuestas.set(r); this.mchatRespuestasLoading.set(false); },
        error: () => this.mchatRespuestasLoading.set(false),
      });
    }
    this.showModal.set(true);
  }

  enterEditMode(): void {
    this.saveError.set(null);
    this.editMode.set(true);
  }

  onGuardar(respuestas: boolean[]): void {
    if (this.saving()) return;
    const dto = Object.fromEntries(respuestas.map((v, i) => [`p${i + 1}`, v])) as unknown as MchatSubmit;
    this.saving.set(true);
    this.saveError.set(null);
    this.mchatService.upsertRespuestasByPaciente(this.paciente().id, dto).pipe(
      tap(r => this.mchatRespuestas.set(r)),
      switchMap(() => this.pacienteService.findDetail(this.paciente().codigoNumerico)),
    ).subscribe({
      next:  p   => { this.updated.emit(p); this.editMode.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }

  reenviar(): void {
    this.reenviarError.set(null);
    this.pacienteService.reenviarMchat(this.paciente().codigoNumerico).subscribe({
      next:  p => this.updated.emit(p),
      error: () => this.reenviarError.set('No se pudo reenviar el enlace. Intentá de nuevo.'),
    });
  }
}
