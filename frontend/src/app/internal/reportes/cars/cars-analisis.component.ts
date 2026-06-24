import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import { CarsData } from '../../../core/models/reporte.model';
import type { EChartsOption } from 'echarts';

@Component({
  selector: 'app-cars-analisis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective],
  templateUrl: './cars-analisis.component.html',
})
export class CarsAnalisisComponent {
  readonly data = input.required<CarsData | null | undefined>();

  readonly histogramaOptions = computed(() => {
    const d = this.data();
    if (!d) return null;
    const labels = d.distribucionRawScore.map(s => s.label);
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', top: '8%', bottom: '20%', containLabel: true },
      xAxis: {
        type: 'category',
        data: labels,
        axisLabel: { fontSize: 9, rotate: 30 },
        name: 'Raw Score CARS-2',
        nameLocation: 'middle',
        nameGap: 38,
        nameTextStyle: { fontSize: 11, fontWeight: 'bold' },
      },
      yAxis: {
        type: 'value',
        name: 'Frecuencia (N)',
        nameLocation: 'middle',
        nameGap: 38,
        nameRotate: 90,
        nameTextStyle: { fontSize: 11, fontWeight: 'bold' },
        axisLabel: { fontSize: 10 },
      },
      series: [{
        type: 'bar',
        data: d.distribucionRawScore.map(s => ({
          value: s.n,
          itemStyle: { color: this.getColorPorBin(s.label), borderRadius: [3, 3, 0, 0] },
        })),
        markLine: {
          silent: true,
          symbol: ['none', 'none'],
          label: { show: true, position: 'insideEndTop', fontSize: 9, color: '#64748b', fontWeight: 'bold' },
          lineStyle: { type: 'dashed', color: '#94a3b8', width: 1.5 },
          data: [
            { name: 'Leve-Mod.', xAxis: '27-30' },
            { name: 'Severo',    xAxis: '33-36.5' },
          ],
        },
      }],
    } as EChartsOption;
  });

  readonly donutOptions = computed(() => {
    const d = this.data();
    if (!d || d.totalConCars === 0) return null;
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { show: false },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        label: { show: false },
        data: [
          { name: 'Mínimo / No TEA', value: d.minimoNoTea,  itemStyle: { color: '#22c55e' } },
          { name: 'Leve-Moderado',   value: d.leveModerado, itemStyle: { color: '#f59e0b' } },
          { name: 'Severo',          value: d.severo,       itemStyle: { color: '#ef4444' } },
        ],
      }],
    } as EChartsOption;
  });

  private getColorPorBin(label: string): string {
    const min = parseFloat(label.split('-')[0]);
    if (min < 30)   return '#22c55e';
    if (min < 36.5) return '#f59e0b';
    return '#ef4444';
  }
}
