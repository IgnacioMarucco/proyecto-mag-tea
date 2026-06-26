import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  CorrelacionResponse,
  DashboardAnalitica,
  EjeCorrelacion,
  FiltroReportes,
} from '../models/reporte.model';

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
    return this.http.get<CorrelacionResponse>(`${this.base}/correlaciones`, { params });
  }

  exportarRatones() {
    return this.http.get('/api/v1/exportar/ratones', { responseType: 'blob' });
  }

  exportarPacientes() {
    return this.http.get('/api/v1/exportar/pacientes', { responseType: 'blob' });
  }

  exportarPoolComposicion() {
    return this.http.get('/api/v1/exportar/pool-composicion', { responseType: 'blob' });
  }

  exportarRatonesXlsx() {
    return this.http.get('/api/v1/exportar/ratones/xlsx', { responseType: 'blob' });
  }

  exportarPacientesXlsx() {
    return this.http.get('/api/v1/exportar/pacientes/xlsx', { responseType: 'blob' });
  }

  exportarPoolComposicionXlsx() {
    return this.http.get('/api/v1/exportar/pool-composicion/xlsx', { responseType: 'blob' });
  }

  exportarCompletoXlsx() {
    return this.http.get('/api/v1/exportar/completo/xlsx', { responseType: 'blob' });
  }
}
