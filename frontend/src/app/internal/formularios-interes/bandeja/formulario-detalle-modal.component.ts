import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { EstadoFormulario, FormularioInteresResponse } from '../../../core/models/formulario-interes.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { EdadPipe } from '../../../core/pipes/edad.pipe';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { ModalContainerComponent } from '../../../shared/modal-container/modal-container.component';

@Component({
  selector: 'app-formulario-detalle-modal',
  imports: [StatusBadgeComponent, EdadPipe, FechaPipe, ModalContainerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './formulario-detalle-modal.component.html',
})
export class FormularioDetalleModalComponent {
  formulario = input.required<FormularioInteresResponse>();
  closed     = output<void>();

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
}
