import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PageParams } from '../models/page-response.model';
import { CamadaListItem, CamadaResponse, CamadaCreate, CamadaUpdate } from '../models/camada.model';

@Injectable({ providedIn: 'root' })
export class CamadaService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/camadas';

  findAll(params: PageParams = {}): Observable<PageResponse<CamadaListItem>> {
    let p = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 50);
    if (params.q)       p = p.set('q', params.q);
    if (params.sortBy)  p = p.set('sortBy', params.sortBy);
    if (params.sortDir) p = p.set('sortDir', params.sortDir);
    return this.http.get<PageResponse<CamadaListItem>>(this.base, { params: p });
  }

  findById(id: number): Observable<CamadaResponse> {
    return this.http.get<CamadaResponse>(`${this.base}/${id}`);
  }

  create(dto: CamadaCreate): Observable<CamadaResponse> {
    return this.http.post<CamadaResponse>(this.base, dto);
  }

  update(id: number, dto: CamadaUpdate): Observable<CamadaResponse> {
    return this.http.put<CamadaResponse>(`${this.base}/${id}`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
