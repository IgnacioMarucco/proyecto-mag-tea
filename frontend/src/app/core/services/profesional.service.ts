import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/page-response.model';
import { ProfesionalCreate, ProfesionalResponse, ProfesionalUpdate } from '../models/profesional.model';

export interface ProfesionalListParams {
  page?: number;
  size?: number;
  q?: string;
  rol?: string;
  sortBy?: string;
  sortDir?: string;
}

@Injectable({ providedIn: 'root' })
export class ProfesionalService {
  private readonly http = inject(HttpClient);
  private readonly BASE = '/api/v1/profesionales';

  findAll(params: ProfesionalListParams = {}): Observable<PageResponse<ProfesionalResponse>> {
    let p = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 50);
    if (params.q)      p = p.set('q', params.q);
    if (params.rol)    p = p.set('rol', params.rol);
    if (params.sortBy) p = p.set('sortBy', params.sortBy);
    if (params.sortDir) p = p.set('sortDir', params.sortDir);
    return this.http.get<PageResponse<ProfesionalResponse>>(this.BASE, { params: p });
  }

  findById(id: number): Observable<ProfesionalResponse> {
    return this.http.get<ProfesionalResponse>(`${this.BASE}/${id}`);
  }

  create(dto: ProfesionalCreate): Observable<ProfesionalResponse> {
    return this.http.post<ProfesionalResponse>(this.BASE, dto);
  }

  update(id: number, dto: ProfesionalUpdate): Observable<ProfesionalResponse> {
    return this.http.put<ProfesionalResponse>(`${this.BASE}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${id}`);
  }
}
