import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { ReportesService } from '../reportes.service';
import { KpiCardComponent } from '../shared/kpi-card/kpi-card.component';
import { EmbudoReclutamientoComponent } from '../embudo/embudo-reclutamiento.component';
import { CaracterizacionComponent } from '../caracterizacion/caracterizacion.component';
import { MchatAnalisisComponent } from '../mchat/mchat-analisis.component';
import { CarsAnalisisComponent } from '../cars/cars-analisis.component';
import { VinelandAnalisisComponent } from '../vineland/vineland-analisis.component';
import { CorrelacionesComponent } from '../correlaciones/correlaciones.component';

@Component({
  selector: 'app-reportes-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    KpiCardComponent,
    EmbudoReclutamientoComponent,
    CaracterizacionComponent,
    MchatAnalisisComponent,
    CarsAnalisisComponent,
    VinelandAnalisisComponent,
    CorrelacionesComponent,
  ],
  templateUrl: './reportes-dashboard.component.html',
})
export class ReportesDashboardComponent {
  private readonly service = inject(ReportesService);

  readonly resumen = toSignal(
    this.service.getResumen().pipe(catchError(() => of(null))),
    { initialValue: null }
  );

  readonly tasaAdmision = () => {
    const r = this.resumen();
    if (!r || r.totalFormularios === 0) return null;
    return (r.formulariosAdmitidos / r.totalFormularios * 100).toFixed(1) + '%';
  };
}
