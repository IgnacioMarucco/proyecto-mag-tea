import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import { AnticuerposData, ComparacionGrupos } from '../../../core/models/reporte.model';
import type { EChartsOption } from 'echarts';

const RANGO_COLORS = ['#94a3b8', '#818cf8', '#6366f1', '#3730a3'];
const RANGO_LABELS = ['Rango 0 (Control, < 1314)', 'Rango 1 (1314–2500)', 'Rango 2 (2501–8000)', 'Rango 3 (> 8000)'];

@Component({
  selector: 'app-anticuerpos-analisis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective],
  templateUrl: './anticuerpos-analisis.component.html',
})
export class AnticuerposAnalisisComponent {
  readonly data        = input.required<AnticuerposData | null | undefined>();
  readonly comparacion = input.required<ComparacionGrupos | null | undefined>();

  readonly isLoading = computed(() => this.data() === undefined);
  readonly hasError  = computed(() => this.data() === null);

  readonly barOptions = computed((): EChartsOption | null => {
    const d = this.data();
    if (!d || d.distribucionRangos.length === 0) return null;

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => {
          const p = Array.isArray(params) ? params[0] : params;
          const idx = p.dataIndex as number;
          return `<strong>${RANGO_LABELS[idx]}</strong><br/>${p.value} pacientes (${d.distribucionRangos[idx].porcentaje}%)`;
        },
      },
      grid: { left: '3%', right: '8%', top: '4%', bottom: '4%', containLabel: true },
      xAxis: { type: 'value', axisLabel: { fontSize: 10 } },
      yAxis: {
        type: 'category',
        data: d.distribucionRangos.map(r => r.label),
        axisLabel: { fontSize: 10 },
      },
      series: [{
        type: 'bar',
        data: d.distribucionRangos.map((r, i) => ({
          value: r.n,
          itemStyle: { color: RANGO_COLORS[i] ?? '#6366f1', borderRadius: [0, 4, 4, 0] },
        })),
        label: { show: true, position: 'right', fontSize: 10 },
      }],
    };
  });

  readonly comparacionOptions = computed((): EChartsOption | null => {
    const c = this.comparacion();
    if (!c || !c.problema || !c.control) return null;

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: (params: any) => {
          const arr = Array.isArray(params) ? params : [params];
          return arr.map((p: any) => `${p.marker}${p.seriesName}: ${p.value} ± ${p.seriesIndex === 0 ? c.problema.sdBtu : c.control.sdBtu}`).join('<br/>');
        },
      },
      legend: { bottom: 0, textStyle: { fontSize: 11 } },
      grid: { left: '3%', right: '8%', top: '8%', bottom: '14%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['BTU (media)'],
        axisLabel: { fontSize: 10 },
      },
      yAxis: {
        type: 'value',
        name: 'Media BTU',
        nameTextStyle: { fontSize: 10, color: '#64748b' },
        axisLabel: { fontSize: 10 },
      },
      series: [
        {
          name: 'Caso Problema',
          type: 'bar',
          itemStyle: { color: '#f97316' },
          data: [{ value: c.problema.mediaBtu }],
          label: { show: true, position: 'top', fontSize: 11, formatter: '{c}' },
          barWidth: '30%',
        },
        {
          name: 'Control',
          type: 'bar',
          itemStyle: { color: '#6366f1' },
          data: [{ value: c.control.mediaBtu }],
          label: { show: true, position: 'top', fontSize: 11, formatter: '{c}' },
          barWidth: '30%',
        },
      ],
    };
  });
}
