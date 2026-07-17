import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  CorrelacionResponse,
  DashboardAnalitica,
  DonacionesAnalitica,
  EjeCorrelacion,
} from '../models/reporte.model';

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/reportes';

  getDashboard() {
    return this.http.get<DashboardAnalitica>(`${this.base}/dashboard`);
  }

  getCorrelaciones(ejeX: EjeCorrelacion, ejeY: EjeCorrelacion) {
    const params = new HttpParams().set('ejeX', ejeX).set('ejeY', ejeY);
    return this.http.get<CorrelacionResponse>(`${this.base}/correlaciones`, { params });
  }

  getDonaciones() {
    return this.http.get<DonacionesAnalitica>(`${this.base}/donaciones`);
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
