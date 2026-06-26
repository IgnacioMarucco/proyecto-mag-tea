import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InicioResponse } from '../models/inicio.model';

@Injectable({ providedIn: 'root' })
export class InicioService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/inicio';

  get(): Observable<InicioResponse> {
    return this.http.get<InicioResponse>(this.base);
  }
}
