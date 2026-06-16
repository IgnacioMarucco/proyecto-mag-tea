import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, PageParams } from '../models/page-response.model';
import {
  PacienteCreate,
  PacienteUpdate,
  PacienteResponse,
  PacienteListItem,
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

  findById(id: number): Observable<PacienteResponse> {
    return this.http.get<PacienteResponse>(`${this.base}/${id}`);
  }

  create(dto: PacienteCreate): Observable<PacienteResponse> {
    return this.http.post<PacienteResponse>(this.base, dto);
  }

  update(id: number, dto: PacienteUpdate): Observable<PacienteResponse> {
    return this.http.put<PacienteResponse>(`${this.base}/${id}`, dto);
  }

  patchPrimeraVisita(id: number, dto: PacientePrimeraVisita): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${id}/primera-visita`, dto);
  }

  patchConsentimiento(id: number, dto: PacienteConsentimiento): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${id}/consentimiento`, dto);
  }

  patchCriterios(id: number, dto: PacienteCriterios): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${id}/criterios`, dto);
  }

  patchMchatSeguimiento(id: number, dto: PacienteMchatSeguimiento): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${id}/mchat-seguimiento`, dto);
  }

  patchCars(id: number, dto: PacienteCars): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${id}/cars`, dto);
  }

  patchVineland(id: number, dto: PacienteVineland): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${id}/vineland`, dto);
  }

  patchSegundaVisita(id: number, dto: PacienteSegundaVisita): Observable<PacienteResponse> {
    return this.http.patch<PacienteResponse>(`${this.base}/${id}/segunda-visita`, dto);
  }

  reenviarMchat(id: number): Observable<PacienteResponse> {
    return this.http.post<PacienteResponse>(`${this.base}/${id}/reenviar-mchat`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
