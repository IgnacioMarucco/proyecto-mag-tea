import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  CarsData,
  CorrelacionPunto,
  DemograficoData,
  EjeCorrelacion,
  EmbudoData,
  MchatData,
  ResumenGeneral,
  VinelandData,
} from './reportes.models';

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/reportes';

  getResumen() {
    return this.http.get<ResumenGeneral>(`${this.base}/resumen`);
  }

  getEmbudo() {
    return this.http.get<EmbudoData>(`${this.base}/embudo`);
  }

  getDemografico() {
    return this.http.get<DemograficoData>(`${this.base}/demografico`);
  }

  getMchat() {
    return this.http.get<MchatData>(`${this.base}/mchat`);
  }

  getCars() {
    return this.http.get<CarsData>(`${this.base}/cars`);
  }

  getVineland() {
    return this.http.get<VinelandData>(`${this.base}/vineland`);
  }

  getCorrelaciones(ejeX: EjeCorrelacion, ejeY: EjeCorrelacion) {
    const params = new HttpParams().set('ejeX', ejeX).set('ejeY', ejeY);
    return this.http.get<CorrelacionPunto[]>(`${this.base}/correlaciones`, { params });
  }
}
