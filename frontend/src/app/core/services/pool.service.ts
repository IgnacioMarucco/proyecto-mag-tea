import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PageParams } from '../models/page-response.model';
import { PoolListItem, PoolResponse, PoolCreate, PoolUpdate } from '../models/pool.model';

export interface PoolListParams extends PageParams {
  rangos?: string[];
  usos?: string[];
}

@Injectable({ providedIn: 'root' })
export class PoolService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/pools';

  findAll(params: PoolListParams = {}): Observable<PageResponse<PoolListItem>> {
    let p = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);
    if (params.q)       p = p.set('q', params.q);
    if (params.sortBy)  p = p.set('sortBy', params.sortBy);
    if (params.sortDir) p = p.set('sortDir', params.sortDir);
    params.rangos?.forEach(r => { p = p.append('rangos', r); });
    params.usos?.forEach(u => { p = p.append('usos', u); });
    return this.http.get<PageResponse<PoolListItem>>(this.base, { params: p });
  }

  findById(id: number): Observable<PoolResponse> {
    return this.http.get<PoolResponse>(`${this.base}/${id}`);
  }

  create(dto: PoolCreate): Observable<PoolResponse> {
    return this.http.post<PoolResponse>(this.base, dto);
  }

  update(id: number, dto: PoolUpdate): Observable<PoolResponse> {
    return this.http.put<PoolResponse>(`${this.base}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
