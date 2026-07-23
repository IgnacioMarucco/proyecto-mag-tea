import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { NgxEchartsDirective } from 'ngx-echarts';
import { catchError, of, switchMap } from 'rxjs';
import { ReportesService } from '../../../core/services/reporte.service';
import { EJE_LABELS, PARES_CORRELACION } from '../../../core/models/reporte.model';
import type { EChartsOption } from 'echarts';

@Component({
  selector: 'app-correlaciones',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective],
  templateUrl: './correlaciones.component.html',
})
export class CorrelacionesComponent {
  private readonly service = inject(ReportesService);

  readonly pares = PARES_CORRELACION;
  readonly ejeLabels = EJE_LABELS;

  readonly parSeleccionado = signal(0);

  readonly ejeX = computed(() => this.pares[this.parSeleccionado()].x);
  readonly ejeY = computed(() => this.pares[this.parSeleccionado()].y);

  private readonly resp = toSignal(
    toObservable(this.parSeleccionado).pipe(
      switchMap(i => {
        const par = this.pares[i];
        return this.service.getCorrelaciones(par.x, par.y).pipe(catchError(() => of(null)));
      })
    ),
    { initialValue: undefined }
  );

  readonly isLoading = computed(() => this.resp() === undefined);
  readonly hasError  = computed(() => this.resp() === null);
  readonly isEmpty   = computed(() => {
    const r = this.resp();
    return r !== null && r !== undefined && r.puntos.length === 0;
  });

  readonly chartOptions = computed<EChartsOption | null>(() => {
    const resp = this.resp();
    if (!resp) return null;

    const reg = this.calcRegression(resp.puntos.map(p => p.x), resp.puntos.map(p => p.y));

    return {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          if (params.seriesIndex !== 0) return '';
          const [x, y] = params.value as [number, number];
          return `<strong>${params.name}</strong><br/>${this.ejeLabels[this.ejeX()]}: ${x}<br/>${this.ejeLabels[this.ejeY()]}: ${y}`;
        },
      },
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
      series: [
        {
          type: 'scatter',
          symbolSize: 8,
          data: resp.puntos.map(p => ({
            value: [p.x, p.y],
            name: p.codigoNumerico,
            itemStyle: { color: p.tipoPaciente === 'PROBLEMA' ? '#f97316' : '#6366f1', opacity: 0.8 },
          })),
        },
        {
          type: 'line',
          data: reg ?? [],
          symbol: 'none',
          silent: true,
          tooltip: { show: false },
          lineStyle: { color: '#94a3b8', type: 'dashed', width: 1.5 },
        },
      ],
    };
  });

  private calcRegression(xs: number[], ys: number[]): [number, number][] | null {
    const n = xs.length;
    if (n < 2) return null;
    const mx = xs.reduce((a, b) => a + b, 0) / n;
    const my = ys.reduce((a, b) => a + b, 0) / n;
    let num = 0, den = 0;
    for (let i = 0; i < n; i++) {
      num += (xs[i] - mx) * (ys[i] - my);
      den += (xs[i] - mx) ** 2;
    }
    if (den === 0) return null;
    const m = num / den;
    const b = my - m * mx;
    const xMin = Math.min(...xs);
    const xMax = Math.max(...xs);
    return [[xMin, m * xMin + b], [xMax, m * xMax + b]];
  }

  parLabel(i: number): string {
    const p = this.pares[i];
    return `${this.ejeLabels[p.x]} vs ${this.ejeLabels[p.y]}`;
  }
}
