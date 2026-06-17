import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { PacienteService } from '../../../../core/services/paciente.service';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { PacienteResponse, PacienteMchatSeguimiento } from '../../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { MchatPreguntasComponent } from '../../../../shared/mchat-preguntas/mchat-preguntas.component';
import { ModalContainerComponent } from '../../../../shared/modal-container/modal-container.component';

@Component({
  selector: 'app-resultado-mchat-section',
  imports: [StatusBadgeComponent, MchatPreguntasComponent, ModalContainerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './resultado-mchat-section.component.html',
})
export class ResultadoMchatSectionComponent {
  private readonly service = inject(PacienteService);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  readonly requiereSeguimiento = computed(() => {
    const score = this.paciente().mchatScoreTotal;
    return score != null && score >= 3 && score <= 7;
  });

  readonly seguimientoItemsAsArray = computed((): boolean[] | null => {
    const p = this.paciente();
    if (p.mchatSeguimientoFallas === null) return null;
    return Array.from({ length: 20 }, (_, i) => !!(p[`seguimientoItem${i + 1}` as keyof PacienteResponse]));
  });

  readonly resultadoFinalLabels: Record<string, string> = {
    POSITIVA: 'Pesquisa positiva', NEGATIVA: 'Pesquisa negativa',
  };
  readonly resultadoFinalColors: Record<string, string> = {
    POSITIVA: 'bg-danger-light text-danger', NEGATIVA: 'bg-accent-light text-accent',
  };

  openModal(): void {
    this.saveError.set(null);
    this.showModal.set(true);
  }

  onGuardar(respuestas: boolean[]): void {
    if (this.saving()) return;
    const dto = Object.fromEntries(respuestas.map((v, i) => [`item${i + 1}`, v])) as unknown as PacienteMchatSeguimiento;
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchMchatSeguimiento(this.paciente().id, dto).subscribe({
      next: p  => { this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }
}
