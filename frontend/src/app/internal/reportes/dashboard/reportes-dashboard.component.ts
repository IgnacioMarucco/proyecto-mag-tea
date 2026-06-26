import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { ReportesService } from '../../../core/services/reporte.service';
import { FILTRO_DEFAULT, FiltroReportes } from '../../../core/models/reporte.model';
import { EmbudoReclutamientoComponent } from '../embudo/embudo-reclutamiento.component';
import { CaracterizacionComponent } from '../caracterizacion/caracterizacion.component';
import { MchatAnalisisComponent } from '../mchat/mchat-analisis.component';
import { CarsAnalisisComponent } from '../cars/cars-analisis.component';
import { VinelandAnalisisComponent } from '../vineland/vineland-analisis.component';
import { CorrelacionesComponent } from '../correlaciones/correlaciones.component';
import { AnticuerposAnalisisComponent } from '../anticuerpos/anticuerpos-analisis.component';
import { ExportacionComponent } from '../exportacion/exportacion.component';
import { ListToolbarComponent, FilterGroup } from '../../../shared/list-toolbar/list-toolbar.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';

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
    AnticuerposAnalisisComponent,
    ExportacionComponent,
    ListToolbarComponent,
    PageHeaderComponent,
  ],
  templateUrl: './reportes-dashboard.component.html',
})
export class ReportesDashboardComponent {
  private readonly service = inject(ReportesService);
  private readonly route   = inject(ActivatedRoute);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  readonly tabActivo = signal<0 | 1 | 2 | 3 | 4>(0);
  readonly filtros   = signal<FiltroReportes>(FILTRO_DEFAULT);
  loading = signal(false);
  error   = signal(false);

  private readonly _dashboard = toSignal(
    toObservable(this.filtros).pipe(
      tap(() => { this.loading.set(true); this.error.set(false); }),
      switchMap(f => this.service.getDashboard(f).pipe(
        tap(() => this.loading.set(false)),
        catchError(() => { this.error.set(true); this.loading.set(false); return of(null); })
      ))
    ),
    { initialValue: undefined }
  );

  readonly resumen     = computed(() => { const d = this._dashboard(); return d === undefined ? undefined : d?.resumen     ?? null; });
  readonly embudo      = computed(() => { const d = this._dashboard(); return d === undefined ? undefined : d?.embudo      ?? null; });
  readonly demografico = computed(() => { const d = this._dashboard(); return d === undefined ? undefined : d?.demografico ?? null; });
  readonly mchat       = computed(() => { const d = this._dashboard(); return d === undefined ? undefined : d?.mchat       ?? null; });
  readonly cars        = computed(() => { const d = this._dashboard(); return d === undefined ? undefined : d?.cars        ?? null; });
  readonly vineland    = computed(() => { const d = this._dashboard(); return d === undefined ? undefined : d?.vineland    ?? null; });
  readonly anticuerpos = computed(() => { const d = this._dashboard(); return d === undefined ? undefined : d?.anticuerpos ?? null; });
  readonly comparacion = computed(() => { const d = this._dashboard(); return d === undefined ? undefined : d?.comparacion ?? null; });

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
