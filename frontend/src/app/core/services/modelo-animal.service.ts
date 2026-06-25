import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PageParams } from '../models/page-response.model';
import {
  ModeloAnimalListItem,
  ModeloAnimalResponse,
  ModeloAnimalReporte,
  ModeloAnimalCreate,
  ModeloAnimalUpdate,
  ModeloAnimalInoculacionCreate,
  SexoRaton,
  VocalizacionesCreate,
  TresCamarasCreate,
  MicroscopiaCreate,
} from '../models/modelo-animal.model';

export interface ModeloAnimalListParams extends PageParams {
  poolId?: number;
  sexo?: SexoRaton;
  uso?: string;
  rango?: number;
  soloAlertas?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ModeloAnimalService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/modelos-animales';

  findAll(params: ModeloAnimalListParams = {}): Observable<PageResponse<ModeloAnimalListItem>> {
    let p = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);
    if (params.q)           p = p.set('q', params.q);
    if (params.sortBy)      p = p.set('sortBy', params.sortBy);
    if (params.sortDir)     p = p.set('sortDir', params.sortDir);
    if (params.poolId)      p = p.set('poolId', params.poolId);
    if (params.sexo)        p = p.set('sexo', params.sexo);
    if (params.uso)         p = p.set('uso', params.uso);
    if (params.rango != null) p = p.set('rango', params.rango);
    if (params.soloAlertas) p = p.set('soloAlertas', params.soloAlertas);
    return this.http.get<PageResponse<ModeloAnimalListItem>>(this.base, { params: p });
  }

  findByCode(identificador: string): Observable<ModeloAnimalResponse> {
    return this.http.get<ModeloAnimalResponse>(`${this.base}/by-code/${identificador}`);
  }

  findById(id: number): Observable<ModeloAnimalResponse> {
    return this.http.get<ModeloAnimalResponse>(`${this.base}/${id}`);
  }

  create(dto: ModeloAnimalCreate): Observable<ModeloAnimalResponse> {
    return this.http.post<ModeloAnimalResponse>(this.base, dto);
  }

  update(id: number, dto: ModeloAnimalUpdate): Observable<ModeloAnimalResponse> {
    return this.http.put<ModeloAnimalResponse>(`${this.base}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  patchInoculacion(id: number, dto: ModeloAnimalInoculacionCreate): Observable<ModeloAnimalResponse> {
    return this.http.patch<ModeloAnimalResponse>(`${this.base}/${id}/inoculacion`, dto);
  }

  patchVocalizaciones(id: number, dto: VocalizacionesCreate): Observable<ModeloAnimalResponse> {
    return this.http.patch<ModeloAnimalResponse>(`${this.base}/${id}/vocalizaciones`, dto);
  }

  patchTresCamaras(id: number, dto: TresCamarasCreate): Observable<ModeloAnimalResponse> {
    return this.http.patch<ModeloAnimalResponse>(`${this.base}/${id}/tres-camaras`, dto);
  }

  patchMicroscopia(id: number, dto: MicroscopiaCreate): Observable<ModeloAnimalResponse> {
    return this.http.patch<ModeloAnimalResponse>(`${this.base}/${id}/microscopia`, dto);
  }

  getReporte(identificador: string): Observable<ModeloAnimalReporte> {
    return this.http.get<ModeloAnimalReporte>(`${this.base}/by-code/${identificador}/reporte`);
  }
}
