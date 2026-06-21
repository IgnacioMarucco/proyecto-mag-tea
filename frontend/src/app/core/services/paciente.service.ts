import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PageParams } from '../models/page-response.model';
import {
  PacienteCreate,
  PacienteUpdate,
  PacienteResponse,
  PacienteListItem,
  PacientePorCodigo,
  PacienteCriterios,
  PacienteMchatSeguimiento,
  PacienteCars,
  PacienteVineland,
  PacienteSegundaVisita,
  PacienteConsentimiento,
  PacientePrimeraVisita,
} from '../models/paciente.model';

export interface PacienteListParams extends PageParams {
  estados?: string[];
  tipos?: string[];
}

@Injectable({ providedIn: 'root' })
export class PacienteService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/pacientes';

  findAll(params: PacienteListParams = {}): Observable<PageResponse<PacienteListItem>> {
    let p = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20);
    if (params.q)       p = p.set('q', params.q);
    if (params.sortBy)  p = p.set('sortBy', params.sortBy);
    if (params.sortDir) p = p.set('sortDir', params.sortDir);
    params.estados?.forEach(e => { p = p.append('estados', e); });
    params.tipos?.forEach(t  => { p = p.append('tipos',   t); });
    return this.http.get<PageResponse<PacienteListItem>>(this.base, { params: p });
  }

  findDetail(codigo: string): Observable<PacienteResponse> {
    return this.http.get<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}`);
  }

  findByCodigo(codigo: string): Observable<PacientePorCodigo> {
    return this.http.get<PacientePorCodigo>(`${this.base}/by-codigo/${encodeURIComponent(codigo)}`);
  }

  create(dto: PacienteCreate): Observable<PacienteResponse> {
    return this.http.post<PacienteResponse>(this.base, dto);
  }

  update(codigo: string, dto: PacienteUpdate): Observable<PacienteResponse> {
    return this.http.put<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}`, dto);
  }

  patchPrimeraVisita(codigo: string, dto: PacientePrimeraVisita): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}/primera-visita`, dto);
  }

  patchConsentimiento(codigo: string, dto: PacienteConsentimiento): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}/consentimiento`, dto);
  }

  patchCriterios(codigo: string, dto: PacienteCriterios): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}/criterios`, dto);
  }

  patchMchatSeguimiento(codigo: string, dto: PacienteMchatSeguimiento): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}/mchat-seguimiento`, dto);
  }

  patchCars(codigo: string, dto: PacienteCars): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}/cars`, dto);
  }

  patchVineland(codigo: string, dto: PacienteVineland): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}/vineland`, dto);
  }

  patchSegundaVisita(codigo: string, dto: PacienteSegundaVisita): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}/segunda-visita`, dto);
  }

  reenviarMchat(codigo: string): Observable<PacienteResponse> {
    return this.http.post<PacienteResponse>(`${this.base}/${encodeURIComponent(codigo)}/reenviar-mchat`, {});
  }

  delete(codigo: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${encodeURIComponent(codigo)}`);
  }
}
