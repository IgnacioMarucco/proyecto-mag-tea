import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NgxEchartsDirective } from 'ngx-echarts';
import { catchError, of } from 'rxjs';
import { ReportesService } from '../reportes.service';
import type { EChartsOption } from 'echarts';

@Component({
  selector: 'app-cars-analisis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective],
  templateUrl: './cars-analisis.component.html',
})
export class CarsAnalisisComponent {
  private readonly service = inject(ReportesService);

  readonly data = toSignal(
    this.service.getCars().pipe(catchError(() => of(null))),
    { initialValue: undefined }
  );

  readonly histogramaOptions = () => {
    const d = this.data();
    if (!d) return null;
    const labels = d.distribucionRawScore.map(s => s.label);
    const values = d.distribucionRawScore.map(s => s.n);
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', top: '14%', bottom: '14%', containLabel: true },
      xAxis: {
        type: 'category',
        data: labels,
        axisLabel: { fontSize: 9, rotate: 30 },
        name: 'Raw Score',
        nameLocation: 'end',
      },
      yAxis: { type: 'value', name: 'N', axisLabel: { fontSize: 10 } },
      series: [{
        type: 'bar',
        data: values,
        itemStyle: { color: '#6366f1', borderRadius: [3, 3, 0, 0] },
        markLine: {
          silent: true,
          data: [
            { xAxis: '27.5-30.0', name: 'Umbral leve (30)', label: { formatter: '30', position: 'insideStartTop', fontSize: 10 } },
            { xAxis: '35.0-37.5', name: 'Umbral severo (36.5)', label: { formatter: '36.5', position: 'insideStartTop', fontSize: 10 } },
          ],
          lineStyle: { type: 'dashed', color: '#ef4444' },
        },
      }],
    } as EChartsOption;
  };

  readonly donutOptions = () => {
    const d = this.data();
    if (!d || d.totalConCars === 0) return null;
    const total = d.totalConCars;
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { fontSize: 10 } },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '42%'],
        label: { show: false },
        data: [
          { name: 'Mínimo / No TEA', value: d.minimoNoTea,  itemStyle: { color: '#22c55e' } },
          { name: 'Leve-Moderado',   value: d.leveModerado, itemStyle: { color: '#f59e0b' } },
          { name: 'Severo',          value: d.severo,       itemStyle: { color: '#ef4444' } },
        ],
      }],
    } as EChartsOption;
  };
}
