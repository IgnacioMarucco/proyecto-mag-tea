import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VaciarTuboPayload } from '../models/suero.model';

@Injectable({ providedIn: 'root' })
export class TuboService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/tubos';

  vaciar(tuboId: number, payload: VaciarTuboPayload): Observable<void> {
    return this.http.post<void>(`${this.base}/${tuboId}/vaciar`, payload);
  }
}
