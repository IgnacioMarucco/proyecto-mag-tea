import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { EstadoFormulario, FormularioInteresResponse } from '../../../core/models/formulario-interes.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { EdadPipe } from '../../../core/pipes/edad.pipe';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { ModalContainerComponent } from '../../../shared/modal-container/modal-container.component';
import { ESTADO_FORMULARIO_COLORS, ESTADO_FORMULARIO_LABELS } from '../../../shared/utils/formulario.utils';

@Component({
  selector: 'app-formulario-detalle-modal',
  imports: [StatusBadgeComponent, EdadPipe, FechaPipe, ModalContainerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './formulario-detalle-modal.component.html',
})
export class FormularioDetalleModalComponent {
  formulario = input.required<FormularioInteresResponse>();
  closed     = output<void>();

  readonly estadoLabels = ESTADO_FORMULARIO_LABELS;
  readonly estadoColors = ESTADO_FORMULARIO_COLORS;

  readonly comoConocioLabels: Record<string, string> = {
    INSTAGRAM:                   'Instagram',
    SUGERIDO_PARTICIPANTE:       'Sugerencia de otro participante',
    SUGERIDO_EQUIPO_TERAPEUTICO: 'Equipo terapéutico',
    SUGERIDO_MEDICO:             'Un médico',
    OTRO:                        'Otro',
  };
}
