import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NgxEchartsDirective } from 'ngx-echarts';
import { catchError, map, of } from 'rxjs';
import { ReportesService } from '../reportes.service';
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
  template: `
    <div class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <h3 class="mb-4 text-sm font-semibold text-slate-700">Caracterización Demográfica</h3>
      @if (data()) {
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="mb-2 text-xs font-medium text-slate-500">Distribución por Sexo</p>
            <div echarts [options]="sexoOptions()!" [autoResize]="true" class="h-44 w-full"></div>
          </div>
          <div>
            <p class="mb-2 text-xs font-medium text-slate-500">Fuente de Derivación</p>
            <div echarts [options]="derivacionOptions()!" [autoResize]="true" class="h-44 w-full"></div>
          </div>
        </div>
      } @else if (data() === null) {
        <p class="text-sm text-slate-400">Sin datos disponibles</p>
      } @else {
        <div class="h-44 animate-pulse rounded bg-slate-50"></div>
      }
    </div>
  `,
})
export class CaracterizacionComponent {
  private readonly service = inject(ReportesService);

  readonly data = toSignal(
    this.service.getDemografico().pipe(catchError(() => of(null))),
    { initialValue: undefined }
  );

  readonly sexoOptions = () => {
    const d = this.data();
    if (!d) return null;
    return this.buildDonut(d.sexo.map(s => ({ name: SEXO_LABELS[s.label] ?? s.label, value: s.n })));
  };

  readonly derivacionOptions = () => {
    const d = this.data();
    if (!d) return null;
    return this.buildBar(d.fuenteDerivacion.map(s => ({
      label: DERIVACION_LABELS[s.label] ?? s.label,
      n: s.n,
    })));
  };

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
