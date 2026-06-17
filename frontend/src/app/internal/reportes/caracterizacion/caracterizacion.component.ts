import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import { DemograficoData } from '../reportes.models';
import type { Distribucion } from '../reportes.models';
import type { EChartsOption } from 'echarts';

const SEXO_LABELS: Record<string, string> = {
  FEMENINO: 'Femenino',
  MASCULINO: 'Masculino',
};

const DERIVACION_LABELS: Record<string, string> = {
  INSTAGRAM: 'Instagram',
  SUGERIDO_PARTICIPANTE: 'Participante',
  SUGERIDO_EQUIPO_TERAPEUTICO: 'Eq. Terapéutico',
  SUGERIDO_MEDICO: 'Médico',
  OTRO: 'Otro',
};

@Component({
  selector: 'app-caracterizacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective],
  templateUrl: './caracterizacion.component.html',
})
export class CaracterizacionComponent {
  readonly data = input.required<DemograficoData | null | undefined>();

  readonly sexoOptions = computed(() => {
    const d = this.data();
    if (!d) return null;
    return this.buildDonut(d.sexo.map(s => ({ name: SEXO_LABELS[s.label] ?? s.label, value: s.n })));
  });

  readonly derivacionOptions = computed(() => {
    const d = this.data();
    if (!d) return null;
    const existing = new Map(d.fuenteDerivacion.map(s => [s.label, s.n]));
    const allKeys = Object.keys(DERIVACION_LABELS);
    return this.buildBar(allKeys.map(k => ({
      label: DERIVACION_LABELS[k],
      n: existing.get(k) ?? 0,
    })));
  });

  private buildDonut(data: { name: string; value: number }[]): EChartsOption {
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { fontSize: 11 } },
      series: [{
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '42%'],
        label: { show: false },
        data,
        color: ['#f472b6', '#60a5fa'],
      }],
    };
  }

  private buildBar(data: { label: string; n: number }[]): EChartsOption {
    return {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '8%', top: '4%', bottom: '4%', containLabel: true },
      xAxis: { type: 'value', axisLabel: { fontSize: 10 } },
      yAxis: {
        type: 'category',
        data: data.map(d => d.label),
        axisLabel: { fontSize: 10 },
      },
      series: [{
        type: 'bar',
        data: data.map(d => d.n),
        itemStyle: { color: '#6366f1', borderRadius: [0, 4, 4, 0] },
        label: { show: true, position: 'right', fontSize: 10 },
      }],
    };
  }
}
