import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  CarsData,
  CorrelacionPunto,
  DashboardAnalitica,
  DemograficoData,
  EjeCorrelacion,
  EmbudoData,
  FiltroReportes,
  MchatData,
  ResumenGeneral,
  VinelandData,
} from './reportes.models';

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/reportes';

  private buildParams(filtros?: FiltroReportes): HttpParams {
    let params = new HttpParams();
    if (filtros?.tipoPaciente && filtros.tipoPaciente !== 'TODOS') {
      params = params.set('tipoPaciente', filtros.tipoPaciente);
    }
    for (const e of filtros?.edades ?? []) {
      params = params.append('edades', e);
    }
    return params;
  }

  getDashboard(filtros?: FiltroReportes) {
    return this.http.get<DashboardAnalitica>(`${this.base}/dashboard`, { params: this.buildParams(filtros) });
  }

  getCorrelaciones(ejeX: EjeCorrelacion, ejeY: EjeCorrelacion, filtros?: FiltroReportes) {
    const params = this.buildParams(filtros).set('ejeX', ejeX).set('ejeY', ejeY);
    return this.http.get<CorrelacionPunto[]>(`${this.base}/correlaciones`, { params });
  }
}
