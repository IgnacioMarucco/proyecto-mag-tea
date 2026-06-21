import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PageParams } from '../models/page-response.model';
import {
  SueroListItem,
  SueroResponse,
  SueroCreate,
  SueroUpdate,
  SueroUso,
  SueroDisponibilidad,
} from '../models/suero.model';

export interface SueroListParams extends PageParams {
  rango?: string[];
  uso?: SueroUso[];
  codigoPaciente?: string;
}

@Injectable({ providedIn: 'root' })
export class SueroService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/sueros';

  findAll(params: SueroListParams = {}): Observable<PageResponse<SueroListItem>> {
    let p = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);
    if (params.q)       p = p.set('q', params.q);
    if (params.sortBy)  p = p.set('sortBy', params.sortBy);
    if (params.sortDir) p = p.set('sortDir', params.sortDir);
    params.rango?.forEach(r => { p = p.append('rangos', r); });
    params.uso?.forEach(u   => { p = p.append('uso',   u); });
    if (params.codigoPaciente) p = p.set('codigoPaciente', params.codigoPaciente);
    return this.http.get<PageResponse<SueroListItem>>(this.base, { params: p });
  }

  findById(id: number): Observable<SueroResponse> {
    return this.http.get<SueroResponse>(`${this.base}/${id}`);
  }

  findByCodigo(codigo: string): Observable<SueroResponse> {
    return this.http.get<SueroResponse>(`${this.base}/by-codigo/${encodeURIComponent(codigo)}`);
  }

  create(dto: SueroCreate): Observable<SueroResponse> {
    return this.http.post<SueroResponse>(this.base, dto);
  }

  update(id: number, dto: SueroUpdate): Observable<SueroResponse> {
    return this.http.put<SueroResponse>(`${this.base}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  getDisponibilidad(): Observable<SueroDisponibilidad[]> {
    return this.http.get<SueroDisponibilidad[]>(`${this.base}/disponibilidad-pool`);
  }
}
