import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/page-response.model';
import { FormularioInteresCreate, FormularioInteresResponse } from '../models/formulario-interes.model';

export interface FormularioListParams {
  page?: number;
  size?: number;
  q?: string;
  estados?: string[];
  sortBy?: string;
  sortDir?: string;
}

@Injectable({ providedIn: 'root' })
export class FormularioInteresService {
  private readonly http = inject(HttpClient);
  private readonly BASE = '/api/v1/formularios-interes';

  findAll(params: FormularioListParams = {}): Observable<PageResponse<FormularioInteresResponse>> {
    let p = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);
    if (params.q)       p = p.set('q', params.q);
    if (params.sortBy)  p = p.set('sortBy', params.sortBy);
    if (params.sortDir) p = p.set('sortDir', params.sortDir);
    params.estados?.forEach(e => { p = p.append('estados', e); });
    return this.http.get<PageResponse<FormularioInteresResponse>>(this.BASE, { params: p });
  }

  findById(id: number): Observable<FormularioInteresResponse> {
    return this.http.get<FormularioInteresResponse>(`${this.BASE}/${id}`);
  }

  create(dto: FormularioInteresCreate): Observable<FormularioInteresResponse> {
    return this.http.post<FormularioInteresResponse>(this.BASE, dto);
  }

  cambiarEstado(id: number, estado: string): Observable<FormularioInteresResponse> {
    return this.http.patch<FormularioInteresResponse>(`${this.BASE}/${id}/estado`, { estado });
  }

  revertirAPendiente(id: number): Observable<FormularioInteresResponse> {
    return this.http.patch<FormularioInteresResponse>(`${this.BASE}/${id}/estado`, { estado: 'PENDIENTE' });
  }
}
