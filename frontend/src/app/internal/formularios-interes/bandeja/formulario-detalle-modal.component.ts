import { afterNextRender, ChangeDetectionStrategy, Component, ElementRef, input, output, viewChild } from '@angular/core';
import { EstadoFormulario, FormularioInteresResponse } from '../../../core/models/formulario-interes.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { EdadPipe } from '../../../core/pipes/edad.pipe';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';

@Component({
  selector: 'app-formulario-detalle-modal',
  imports: [StatusBadgeComponent, EdadPipe, FechaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './formulario-detalle-modal.component.html',
})
export class FormularioDetalleModalComponent {
  formulario = input.required<FormularioInteresResponse>();

  closed = output<void>();

  private readonly modalDialog = viewChild<ElementRef<HTMLElement>>('modalDialog');

  constructor() {
    afterNextRender(() => { this.modalDialog()?.nativeElement.focus(); });
  }

  readonly estadoLabels: Record<EstadoFormulario, string> = {
    PENDIENTE:  'Pendiente',
    CONTACTADO: 'Contactado',
    ADMITIDO:   'Admitido',
    DESCARTADO: 'Descartado',
  };

  readonly estadoColors: Record<EstadoFormulario, string> = {
    PENDIENTE:  'bg-warning/10 text-warning',
    CONTACTADO: 'bg-primary-light text-primary',
    ADMITIDO:   'bg-accent-light text-accent',
    DESCARTADO: 'bg-background text-text-muted border border-border',
  };

  readonly comoConocioLabels: Record<string, string> = {
    INSTAGRAM:                   'Instagram',
    SUGERIDO_PARTICIPANTE:       'Sugerencia de otro participante',
    SUGERIDO_EQUIPO_TERAPEUTICO: 'Equipo terapéutico',
    SUGERIDO_MEDICO:             'Un médico',
    OTRO:                        'Otro',
  };

  onModalKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Tab') return;
    const dialog = this.modalDialog()?.nativeElement;
    if (!dialog) return;
    const focusable = Array.from(
      dialog.querySelectorAll<HTMLElement>(
        'button:not([disabled]), [href], input:not([disabled]), [tabindex]:not([tabindex="-1"])'
      )
    ).filter(el => el.offsetParent !== null);
    if (focusable.length === 0) return;
    const first = focusable[0];
    const last  = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }
}
