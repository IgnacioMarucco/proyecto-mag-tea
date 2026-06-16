import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NgxEchartsDirective } from 'ngx-echarts';
import { catchError, map, of } from 'rxjs';
import { ReportesService } from '../reportes.service';
import type { EChartsOption } from 'echarts';

@Component({
  selector: 'app-embudo-reclutamiento',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgxEchartsDirective],
  template: `
    <div class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <h3 class="mb-4 text-sm font-semibold text-slate-700">Embudo de Reclutamiento</h3>
      @if (options()) {
        <div echarts [options]="options()!" [autoResize]="true" class="h-72 w-full"></div>
      } @else if (options() === null) {
        <p class="text-sm text-slate-400">Sin datos disponibles</p>
      } @else {
        <div class="h-72 animate-pulse rounded bg-slate-50"></div>
      }
    </div>
  `,
})
export class EmbudoReclutamientoComponent {
  private readonly service = inject(ReportesService);

  readonly options = toSignal(
    this.service.getEmbudo().pipe(
      map(data => this.buildOptions(data.etapas)),
      catchError(() => of(null))
    ),
    { initialValue: undefined }
  );

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
        data: etapas.map(e => ({
          name: e.nombre,
          value: e.n,
        })),
        color: ['#6366f1', '#818cf8', '#a5b4fc', '#c7d2fe', '#ddd6fe', '#ede9fe'],
      }],
    };
  }
}
