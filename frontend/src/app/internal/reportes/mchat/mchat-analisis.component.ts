import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { NgxEchartsDirective } from 'ngx-echarts';
import { MchatData } from '../../../core/models/reporte.model';
import type { Distribucion } from '../../../core/models/reporte.model';
import type { EChartsOption } from 'echarts';
import { MCHAT_PREGUNTAS } from '../../../shared/constants/mchat.constants';

function colorPorScore(score: number): string {
  if (score <= 2) return '#22c55e';
  if (score <= 7) return '#f59e0b';
  return '#ef4444';
}

@Component({
  selector: 'app-mchat-analisis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective, DecimalPipe],
  templateUrl: './mchat-analisis.component.html',
})
export class MchatAnalisisComponent {
  readonly data = input.required<MchatData | null | undefined>();

  readonly histogramaOptions = computed(() => {
    const d = this.data();
    if (!d) return null;
    const labels = d.distribucionScores.map(s => s.label);
    const values = d.distribucionScores.map(s => s.n);
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', top: 20, bottom: 32, containLabel: true },
      xAxis: [
        {
          type: 'category',
          data: labels,
          axisLabel: { fontSize: 10 },
          name: 'Score',
          nameLocation: 'middle',
          nameGap: 20,
          nameTextStyle: { fontSize: 10, color: '#64748b', fontWeight: 500 },
        },
        {
          type: 'value',
          min: -0.5,
          max: 20.5,
          show: false,
        }
      ],
      yAxis: {
        type: 'value',
        name: 'N',
        nameLocation: 'end',
        nameGap: 8,
        nameTextStyle: { fontSize: 10, color: '#64748b', fontWeight: 500, align: 'right' },
        axisLabel: { fontSize: 10 },
        axisLine: { onZero: false },
      },
      series: [
        {
          type: 'bar',
          xAxisIndex: 0,
          data: labels.map((l, i) => ({
            value: values[i],
            itemStyle: { color: colorPorScore(Number(l)) },
          })),
          barMaxWidth: 30,
        },
        {
          type: 'line',
          xAxisIndex: 1,
          data: [],
          markLine: {
            symbol: ['none', 'none'],
            silent: true,
            label: {
              show: true,
              position: 'end',
              formatter: '{b}',
              fontSize: 9,
              color: '#64748b',
              fontWeight: 'bold',
            },
            lineStyle: {
              type: 'dashed',
              color: '#94a3b8',
              width: 1,
            },
            data: [
              { name: 'Medio', xAxis: 2.5 },
              { name: 'Alto', xAxis: 7.5 },
            ],
          },
        }
      ],
    } as EChartsOption;
  });

  readonly resultadoOptions = computed(() => {
    const d = this.data();
    if (!d) return null;
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { fontSize: 11 } },
      series: [{
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '42%'],
        label: { show: false },
        data: d.resultadoFinal.map(r => ({
          name: r.label === 'POSITIVA' ? 'Positiva' : 'Negativa',
          value: r.n,
        })),
        color: ['#ef4444', '#22c55e'],
      }],
    } as EChartsOption;
  });

  readonly itemsTamizajeOptions = computed(() => this.buildItemsChart(this.data()?.itemsFalladosTamizaje));
  readonly itemsSeguimientoOptions = computed(() => this.buildItemsChart(this.data()?.itemsFalladosSeguimiento));

  private buildItemsChart(items: Distribucion[] | undefined): EChartsOption | null {
    if (!items) return null;
    const top20 = items.slice(0, 20);
    const INVERTIDOS = new Set(MCHAT_PREGUNTAS.filter(p => p.invertida).map(p => p.numero));
    return {
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const p = Array.isArray(params) ? params[0] : params;
          const num = Number(p.name);
          const pregunta = MCHAT_PREGUNTAS[num - 1];
          const texto = pregunta
            ? `<div style="max-width:320px;white-space:normal;font-size:11px;margin-top:4px;color:#6b7280">${pregunta.texto}</div>`
            : '';
          return `<strong>Ítem ${num}</strong>: ${p.value} fallas${texto}`;
        },
      },
      grid: { left: '3%', right: '4%', top: 20, bottom: 32, containLabel: true },
      xAxis: {
        type: 'category',
        data: top20.map(i => i.label.replace('Ítem ', '')),
        axisLabel: { fontSize: 10, rotate: 0 },
        name: 'Ítem',
        nameLocation: 'middle',
        nameGap: 20,
        nameTextStyle: { fontSize: 10, color: '#64748b', fontWeight: 500 },
      },
      yAxis: {
        type: 'value',
        name: 'Fallas',
        nameLocation: 'end',
        nameGap: 8,
        nameTextStyle: { fontSize: 10, color: '#64748b', fontWeight: 500, align: 'right' },
        axisLabel: { fontSize: 10 },
      },
      series: [{
        type: 'bar',
        data: top20.map(i => ({
          value: i.n,
          itemStyle: {
            color: INVERTIDOS.has(Number(i.label.replace('Ítem ', ''))) ? '#f59e0b' : '#6366f1',
            borderRadius: [3, 3, 0, 0],
          },
        })),
        label: { show: true, position: 'top', formatter: '{c}', fontSize: 10 },
      }],
    };
  }
}
