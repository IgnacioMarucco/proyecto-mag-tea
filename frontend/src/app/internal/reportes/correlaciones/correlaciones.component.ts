import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { NgxEchartsDirective } from 'ngx-echarts';
import { FormsModule } from '@angular/forms';
import { catchError, of, switchMap } from 'rxjs';
import { ReportesService } from '../reportes.service';
import {
  EJE_LABELS,
  EjeCorrelacion,
  PARES_CORRELACION,
} from '../reportes.models';
import type { EChartsOption } from 'echarts';

@Component({
  selector: 'app-correlaciones',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective, FormsModule],
  templateUrl: './correlaciones.component.html',
})
export class CorrelacionesComponent {
  private readonly service = inject(ReportesService);

  readonly pares = PARES_CORRELACION;
  readonly ejeLabels = EJE_LABELS;

  readonly parSeleccionado = signal(0);

  readonly ejeX = computed(() => this.pares[this.parSeleccionado()].x);
  readonly ejeY = computed(() => this.pares[this.parSeleccionado()].y);

  private readonly puntos = toSignal(
    toObservable(this.parSeleccionado).pipe(
      switchMap(i => {
        const par = this.pares[i];
        return this.service.getCorrelaciones(par.x, par.y).pipe(catchError(() => of(null)));
      })
    ),
    { initialValue: undefined }
  );

  readonly chartOptions = computed<EChartsOption | null>(() => {
    const data = this.puntos();
    if (!data) return null;

    return {
      tooltip: { trigger: 'item', formatter: 'Código: {b}' },
      grid: { left: '3%', right: '6%', top: '8%', bottom: '16%', containLabel: true },
      xAxis: {
        type: 'value',
        name: this.ejeLabels[this.ejeX()],
        nameLocation: 'middle',
        nameGap: 28,
        axisLabel: { fontSize: 10 },
      },
      yAxis: {
        type: 'value',
        name: this.ejeLabels[this.ejeY()],
        nameLocation: 'middle',
        nameGap: 40,
        axisLabel: { fontSize: 10 },
      },
      series: [{
        type: 'scatter',
        symbolSize: 8,
        data: data.map(p => ({ value: [p.x, p.y], name: p.codigoNumerico })),
        itemStyle: { color: '#6366f1', opacity: 0.75 },
      }],
    };
  });

  readonly isLoading = computed(() => this.puntos() === undefined);
  readonly hasError  = computed(() => this.puntos() === null);
  readonly isEmpty   = computed(() => Array.isArray(this.puntos()) && (this.puntos() as unknown[]).length === 0);

  parLabel(i: number): string {
    const p = this.pares[i];
    return `${this.ejeLabels[p.x]} vs ${this.ejeLabels[p.y]}`;
  }
}
