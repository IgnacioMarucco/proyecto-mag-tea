import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { VinelandData } from '../../../core/models/reporte.model';

@Component({
  selector: 'app-vineland-analisis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './vineland-analisis.component.html',
})
export class VinelandAnalisisComponent {
  readonly data = input.required<VinelandData | null | undefined>();
}
