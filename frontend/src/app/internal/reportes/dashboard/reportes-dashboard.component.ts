import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { catchError, of, switchMap } from 'rxjs';
import { ReportesService } from '../reportes.service';
import { FILTRO_DEFAULT, FiltroReportes } from '../reportes.models';
import { EmbudoReclutamientoComponent } from '../embudo/embudo-reclutamiento.component';
import { CaracterizacionComponent } from '../caracterizacion/caracterizacion.component';
import { MchatAnalisisComponent } from '../mchat/mchat-analisis.component';
import { CarsAnalisisComponent } from '../cars/cars-analisis.component';
import { VinelandAnalisisComponent } from '../vineland/vineland-analisis.component';
import { CorrelacionesComponent } from '../correlaciones/correlaciones.component';
import { ListToolbarComponent, FilterGroup } from '../../../shared/list-toolbar/list-toolbar.component';

@Component({
  selector: 'app-reportes-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    EmbudoReclutamientoComponent,
    CaracterizacionComponent,
    MchatAnalisisComponent,
    CarsAnalisisComponent,
    VinelandAnalisisComponent,
    CorrelacionesComponent,
    ListToolbarComponent,
  ],
  templateUrl: './reportes-dashboard.component.html',
})
export class ReportesDashboardComponent {
  private readonly service = inject(ReportesService);

  readonly tabActivo = signal<0 | 1 | 2>(0);
  readonly filtros   = signal<FiltroReportes>(FILTRO_DEFAULT);

  private readonly _dashboard = toSignal(
    toObservable(this.filtros).pipe(
      switchMap(f => this.service.getDashboard(f).pipe(catchError(() => of(null))))
    ),
    { initialValue: undefined }
  );

  readonly resumen    = computed(() => this._dashboard()?.resumen    ?? null);
  readonly embudo     = computed(() => this._dashboard()?.embudo     ?? (this._dashboard() === null ? null : undefined));
  readonly demografico = computed(() => this._dashboard()?.demografico ?? (this._dashboard() === null ? null : undefined));
  readonly mchat      = computed(() => this._dashboard()?.mchat      ?? (this._dashboard() === null ? null : undefined));
  readonly cars       = computed(() => this._dashboard()?.cars       ?? (this._dashboard() === null ? null : undefined));
  readonly vineland   = computed(() => this._dashboard()?.vineland   ?? (this._dashboard() === null ? null : undefined));

  readonly tasaAdmision = computed(() => {
    const r = this.resumen();
    if (!r || r.totalFormularios === 0) return null;
    return (r.formulariosAdmitidos / r.totalFormularios * 100).toFixed(1) + '%';
  });

  readonly filterGroups: FilterGroup[] = [
    {
      key: 'tipoPaciente',
      label: 'Tipo de paciente',
      multiSelect: true,
      options: [
        { key: 'PROBLEMA', label: 'Caso problema' },
        { key: 'CONTROL',  label: 'Control' },
      ],
    },
    {
      key: 'edad',
      label: 'Edad',
      multiSelect: true,
      options: [
        { key: '2', label: '2 años' },
        { key: '3', label: '3 años' },
        { key: '4', label: '4 años' },
        { key: '5', label: '5+ años' },
      ],
    },
  ];

  readonly filtrosActivos = computed((): Record<string, string | string[]> => {
    const f = this.filtros();
    const result: Record<string, string | string[]> = {};
    if (f.tipoPaciente !== 'TODOS') result['tipoPaciente'] = [f.tipoPaciente];
    if (f.edades.length) result['edad'] = f.edades.map(String);
    return result;
  });

  onFiltersChange(raw: Record<string, string | string[]>): void {
    const tipoArr = raw['tipoPaciente'] as string[] | undefined;
    const tipo: FiltroReportes['tipoPaciente'] =
      tipoArr?.length === 1 ? (tipoArr[0] as 'PROBLEMA' | 'CONTROL') : 'TODOS';

    const edades = ((raw['edad'] as string[] | undefined) ?? []).map(Number);

    this.filtros.set({ tipoPaciente: tipo, edades });
  }
}
