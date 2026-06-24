import {
  ChangeDetectionStrategy, Component, ElementRef, Injector,
  afterNextRender, effect, inject, input, output, signal, viewChild,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { PacienteService } from '../../../../core/services/paciente.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { EdadPipe } from '../../../../core/pipes/edad.pipe';
import { FechaPipe } from '../../../../core/pipes/fecha.pipe';
import { CriteriosSectionComponent } from './criterios-section.component';
import { ConfirmModalComponent } from '../../../../shared/confirm-modal/confirm-modal.component';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';

@Component({
  selector: 'app-datos-basicos-section',
  imports: [RouterLink, StatusBadgeComponent, EdadPipe, FechaPipe, CriteriosSectionComponent, ConfirmModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './datos-basicos-section.component.html',
})
export class DatosBasicosSectionComponent {
  private readonly injector = inject(Injector);
  private readonly service  = inject(PacienteService);

  paciente      = input.required<PacienteResponse>();
  updated       = output<PacienteResponse>();

  activeTab     = signal<'datos' | 'criterios' | 'consentimiento'>('datos');
  notesExpanded = signal(false);
  isOverflowing = signal(false);

  showConfirmModal = signal(false);
  confirming       = signal(false);
  confirmError     = signal<string | null>(null);

  private readonly notesEl = viewChild<ElementRef<HTMLParagraphElement>>('notesEl');

  readonly criteriosEstadoLabels: Record<string, string> = {
    APTO: 'Apto para el protocolo',
    EXCLUIDO: 'Excluido del protocolo',
    INCOMPLETO: 'Criterios incompletos',
  };
  readonly criteriosEstadoColors: Record<string, string> = {
    APTO:       'bg-accent-light text-accent',
    EXCLUIDO:   'bg-danger-light text-danger',
    INCOMPLETO: 'bg-warning/10 text-warning',
  };

  constructor() {
    effect(() => {
      this.paciente();
      this.notesExpanded.set(false);
      this.isOverflowing.set(false);
      afterNextRender(() => {
        const el = this.notesEl()?.nativeElement;
        if (el) this.isOverflowing.set(el.scrollHeight > el.clientHeight);
      }, { injector: this.injector });
    });
  }

  confirmConsent(): void {
    this.confirming.set(true);
    this.confirmError.set(null);
    this.service.patchConsentimiento(this.paciente().codigoNumerico, { consentimientoFirmado: true }).subscribe({
      next:  p   => { this.updated.emit(p); this.showConfirmModal.set(false); this.confirming.set(false); },
      error: err => { this.confirmError.set(extractErrorMessage(err, 'Error al guardar')); this.confirming.set(false); },
    });
  }

  getCriterioValue(key: string): boolean {
    return !!(this.paciente() as unknown as Record<string, unknown>)[key];
  }

}
