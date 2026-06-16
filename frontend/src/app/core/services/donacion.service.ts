import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DonacionCreate, DonacionInitPoint } from '../models/donacion.model';

@Injectable({ providedIn: 'root' })
export class DonacionService {
  private readonly http = inject(HttpClient);

  iniciar(dto: DonacionCreate): Observable<DonacionInitPoint> {
    return this.http.post<DonacionInitPoint>('/api/v1/public/donaciones', dto);
  }
}
