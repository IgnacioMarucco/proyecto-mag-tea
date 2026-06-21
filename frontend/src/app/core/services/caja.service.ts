import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PageParams } from '../models/page-response.model';
import { CajaListItem, CajaResponse, CajaCreate, CajaUpdate, CajaOcupacion } from '../models/caja.model';

export interface CajaListParams extends PageParams {
  freezer?: string;
}

@Injectable({ providedIn: 'root' })
export class CajaService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/cajas';

  findAll(params: CajaListParams = {}): Observable<PageResponse<CajaListItem>> {
    let p = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 50);
    if (params.q)       p = p.set('q', params.q);
    if (params.freezer) p = p.set('freezer', params.freezer);
    if (params.sortBy)  p = p.set('sortBy', params.sortBy);
    if (params.sortDir) p = p.set('sortDir', params.sortDir);
    return this.http.get<PageResponse<CajaListItem>>(this.base, { params: p });
  }

  findById(id: number): Observable<CajaResponse> {
    return this.http.get<CajaResponse>(`${this.base}/${id}`);
  }

  create(dto: CajaCreate): Observable<CajaResponse> {
    return this.http.post<CajaResponse>(this.base, dto);
  }

  update(id: number, dto: CajaUpdate): Observable<CajaResponse> {
    return this.http.put<CajaResponse>(`${this.base}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  getOcupacion(id: number, excludeSueroId?: number): Observable<CajaOcupacion> {
    let p = new HttpParams();
    if (excludeSueroId != null) p = p.set('excludeSueroId', excludeSueroId);
    return this.http.get<CajaOcupacion>(`${this.base}/${id}/ocupacion`, { params: p });
  }
}
