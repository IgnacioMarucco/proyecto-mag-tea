import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { catchError, map, of, tap } from 'rxjs';
import { ReportesService } from '../../../core/services/reporte.service';
import { EmbudoReclutamientoComponent } from '../embudo/embudo-reclutamiento.component';
import { CaracterizacionComponent } from '../caracterizacion/caracterizacion.component';
import { MchatAnalisisComponent } from '../mchat/mchat-analisis.component';
import { CarsAnalisisComponent } from '../cars/cars-analisis.component';
import { VinelandAnalisisComponent } from '../vineland/vineland-analisis.component';
import { CorrelacionesComponent } from '../correlaciones/correlaciones.component';
import { AnticuerposAnalisisComponent } from '../anticuerpos/anticuerpos-analisis.component';
import { DonacionesAnalisisComponent } from '../donaciones/donaciones-analisis.component';
import { ExportacionComponent } from '../exportacion/exportacion.component';
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
    DonacionesAnalisisComponent,
    ExportacionComponent,
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

  readonly tabActivo = signal<0 | 1 | 2 | 3 | 4 | 5>(0);
  loading = signal(true);
  error   = signal(false);

  private readonly _dashboard = toSignal(
    this.service.getDashboard().pipe(
      tap(() => this.loading.set(false)),
      catchError(() => { this.error.set(true); this.loading.set(false); return of(null); })
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
}
