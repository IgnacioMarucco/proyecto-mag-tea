import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import { VinelandData } from '../reportes.models';
import type { EChartsOption } from 'echarts';

@Component({
  selector: 'app-vineland-analisis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective],
  templateUrl: './vineland-analisis.component.html',
})
export class VinelandAnalisisComponent {
  readonly data = input.required<VinelandData | null | undefined>();

  readonly radarOptions = computed(() => {
    const d = this.data();
    if (!d || d.totalConVineland === 0) return null;

    const maxVal = 160;
    return {
      tooltip: { trigger: 'item' },
      radar: {
        indicator: [
          { name: 'Comunicación',   max: maxVal },
          { name: 'Autovalimiento', max: maxVal },
          { name: 'Social',         max: maxVal },
          { name: 'Motor',          max: maxVal },
        ],
        splitNumber: 4,
        axisName: { color: '#475569', fontSize: 11 },
      },
      series: [{
        type: 'radar',
        data: [{
          value: [d.mediaComunicacion, d.mediaAutovalimiento, d.mediaSocial, d.mediaMotor],
          name: 'Media de la muestra',
          areaStyle: { color: 'rgba(99, 102, 241, 0.15)' },
          lineStyle: { color: '#6366f1' },
          itemStyle: { color: '#6366f1' },
        }],
      }],
    } as EChartsOption;
  });
}
