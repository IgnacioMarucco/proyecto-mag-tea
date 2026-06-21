import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { SueroTuboInput } from '../../core/models/suero.model';

@Component({
  selector: 'app-tubo-quantity-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tubo-quantity-table.component.html',
})
export class TuboQuantityTableComponent {
  tubos       = input<SueroTuboInput[]>([]);
  tubosChange = output<SueroTuboInput[]>();

  readonly total = computed(() =>
    this.tubos().reduce((s, t) => s + (t.cantidadInicial || 0), 0)
  );

  update(posicion: string, rawValue: string): void {
    const cantidadInicial = parseFloat(rawValue) || 0;
    this.tubosChange.emit(
      this.tubos().map(t => t.posicion === posicion ? { ...t, cantidadInicial } : t)
    );
  }
}
