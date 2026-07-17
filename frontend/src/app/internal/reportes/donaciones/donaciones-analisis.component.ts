import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { NgxEchartsDirective } from 'ngx-echarts';
import { catchError, of } from 'rxjs';
import { ReportesService } from '../../../core/services/reporte.service';
import type { EChartsOption } from 'echarts';

const COLOR_ESTADO: Record<string, string> = {
  APROBADO: '#22c55e',
  PENDIENTE: '#f59e0b',
  RECHAZADO: '#ef4444',
  CANCELADO: '#94a3b8',
};

@Component({
  selector: 'app-donaciones-analisis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective, DecimalPipe],
  templateUrl: './donaciones-analisis.component.html',
})
export class DonacionesAnalisisComponent {
  private readonly service = inject(ReportesService);

  private readonly resp = toSignal(
    this.service.getDonaciones().pipe(catchError(() => of(null))),
    { initialValue: undefined }
  );

  readonly isLoading = computed(() => this.resp() === undefined);
  readonly hasError = computed(() => this.resp() === null);

  readonly totalRecaudado = computed(() => this.resp()?.totalRecaudado ?? 0);
  readonly cantidadAprobadas = computed(() => this.resp()?.cantidadAprobadas ?? 0);
  readonly montoPromedio = computed(() => this.resp()?.montoPromedio ?? 0);
  readonly donantes = computed(() => this.resp()?.donantes ?? []);

  readonly barOptions = computed<EChartsOption | null>(() => {
    const r = this.resp();
    if (!r) return null;
    return {
      tooltip: { trigger: 'axis', formatter: (p: any) => `${p[0].name}: $${p[0].value.toLocaleString('es-AR')}` },
      grid: { left: '3%', right: '4%', top: '8%', bottom: '10%', containLabel: true },
      xAxis: { type: 'category', data: r.recaudacionPorMes.map(m => m.periodo), axisLabel: { fontSize: 10 } },
      yAxis: { type: 'value', axisLabel: { fontSize: 10 } },
      series: [{
        type: 'bar',
        data: r.recaudacionPorMes.map(m => m.monto),
        itemStyle: { color: '#6366f1', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 40,
      }],
    };
  });

  readonly pieOptions = computed<EChartsOption | null>(() => {
    const r = this.resp();
    if (!r) return null;
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { fontSize: 11 } },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: r.porEstado.map(d => ({
          name: d.label,
          value: d.n,
          itemStyle: { color: COLOR_ESTADO[d.label] },
        })),
        label: { formatter: '{b}: {c}' },
      }],
    };
  });
}
