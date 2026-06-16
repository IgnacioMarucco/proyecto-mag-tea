import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NgxEchartsDirective } from 'ngx-echarts';
import { catchError, of } from 'rxjs';
import { ReportesService } from '../reportes.service';
import type { Distribucion } from '../reportes.models';
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
  imports: [NgxEchartsDirective],
  templateUrl: './mchat-analisis.component.html',
})
export class MchatAnalisisComponent {
  private readonly service = inject(ReportesService);

  readonly data = toSignal(
    this.service.getMchat().pipe(catchError(() => of(null))),
    { initialValue: undefined }
  );

  readonly histogramaOptions = () => {
    const d = this.data();
    if (!d) return null;
    const labels = d.distribucionScores.map(s => s.label);
    const values = d.distribucionScores.map(s => s.n);
    return {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', top: '8%', bottom: '12%', containLabel: true },
      xAxis: {
        type: 'category',
        data: labels,
        axisLabel: { fontSize: 10 },
        name: 'Score',
        nameLocation: 'end',
      },
      yAxis: { type: 'value', name: 'N', axisLabel: { fontSize: 10 } },
      series: [{
        type: 'bar',
        data: labels.map((l, i) => ({
          value: values[i],
          itemStyle: { color: colorPorScore(Number(l)) },
        })),
        barMaxWidth: 30,
      }],
    } as EChartsOption;
  };

  readonly resultadoOptions = () => {
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
  };

  readonly itemsTamizajeOptions = () => this.buildItemsChart(this.data()?.itemsFalladosTamizaje);
  readonly itemsSeguimientoOptions = () => this.buildItemsChart(this.data()?.itemsFalladosSeguimiento);

  private buildItemsChart(items: Distribucion[] | undefined): EChartsOption | null {
    if (!items) return null;
    const top10 = items.slice(0, 10);
    const INVERTIDOS = new Set(MCHAT_PREGUNTAS.filter(p => p.invertida).map(p => p.numero));
    return {
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const p = Array.isArray(params) ? params[0] : params;
          const num = Number(p.name.replace('Ítem ', ''));
          const pregunta = MCHAT_PREGUNTAS[num - 1];
          const texto = pregunta
            ? `<div style="max-width:320px;white-space:normal;font-size:11px;margin-top:4px;color:#6b7280">${pregunta.texto}</div>`
            : '';
          return `<strong>${p.name}</strong>: ${p.value} fallas${texto}`;
        },
      },
      grid: { left: '3%', right: '8%', top: '4%', bottom: '4%', containLabel: true },
      xAxis: { type: 'value', axisLabel: { fontSize: 10 } },
      yAxis: {
        type: 'category',
        data: top10.map(i => i.label),
        axisLabel: { fontSize: 10 },
      },
      series: [{
        type: 'bar',
        data: top10.map(i => ({
          value: i.n,
          itemStyle: {
            color: INVERTIDOS.has(Number(i.label.replace('Ítem ', ''))) ? '#f59e0b' : '#6366f1',
            borderRadius: [0, 4, 4, 0],
          },
        })),
        label: {
          show: true,
          position: 'right',
          formatter: '{c}',
          fontSize: 10,
        },
      }],
    };
  }
}
