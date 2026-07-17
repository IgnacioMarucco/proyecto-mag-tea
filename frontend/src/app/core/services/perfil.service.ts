import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProfesionalResponse } from '../models/profesional.model';
import { CambiarPasswordRequest, PerfilUpdate } from '../models/perfil.model';

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private readonly http = inject(HttpClient);
  private readonly BASE = '/api/v1/perfil';

  me(): Observable<ProfesionalResponse> {
    return this.http.get<ProfesionalResponse>(this.BASE);
  }

  update(dto: PerfilUpdate): Observable<ProfesionalResponse> {
    return this.http.put<ProfesionalResponse>(this.BASE, dto);
  }

  changePassword(dto: CambiarPasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.BASE}/password`, dto);
  }
}
