import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { ModeloAnimalResponse } from '../../../../core/models/modelo-animal.model';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { FechaPipe } from '../../../../core/pipes/fecha.pipe';
import { RANGO_COLORS, RANGO_LABELS } from '../../../../shared/utils/btu.utils';
import { SEXO_COLORS, SEXO_LABELS } from '../../../../shared/utils/modelo-animal.utils';

@Component({
  selector: 'app-datos-basicos-ma-section',
  imports: [StatusBadgeComponent, FechaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './datos-basicos-ma-section.component.html',
})
export class DatosBasicosMaSectionComponent {
  modeloAnimal = input.required<ModeloAnimalResponse>();

  readonly rangoColors = RANGO_COLORS;
  readonly rangoLabels = RANGO_LABELS;
  readonly sexoLabels = SEXO_LABELS;
  readonly sexoColors = SEXO_COLORS;

  readonly diaNacimiento = computed(() => {
    const ma = this.modeloAnimal();
    const nac = new Date(ma.fechaNacimiento + 'T00:00:00');
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const diff = Math.floor((hoy.getTime() - nac.getTime()) / (1000 * 60 * 60 * 24));
    return diff + 1;
  });

}
