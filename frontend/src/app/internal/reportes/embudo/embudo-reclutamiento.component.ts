import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import { EmbudoData } from '../reportes.models';
import type { EChartsOption } from 'echarts';

@Component({
  selector: 'app-embudo-reclutamiento',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective],
  templateUrl: './embudo-reclutamiento.component.html',
})
export class EmbudoReclutamientoComponent {
  readonly data = input.required<EmbudoData | null | undefined>();

  readonly options = computed(() => {
    const d = this.data();
    if (!d) return d;
    return this.buildOptions(d.etapas);
  });

  private buildOptions(etapas: { nombre: string; n: number; porcentajeRespecto1raEtapa: number }[]): EChartsOption {
    return {
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        type: 'funnel',
        sort: 'none',
        left: '5%',
        width: '90%',
        minSize: '10%',
        maxSize: '100%',
        label: { show: true, position: 'inside', formatter: '{b}: {c}' },
        itemStyle: { borderWidth: 0 },
        data: etapas.map(e => ({ name: e.nombre, value: e.n })),
        color: ['#6366f1', '#818cf8', '#a5b4fc', '#c7d2fe'],
      }],
    };
  }
}
