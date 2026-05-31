import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProfesionalCreate, ProfesionalResponse, ProfesionalUpdate } from '../models/profesional.model';

@Injectable({ providedIn: 'root' })
export class ProfesionalService {
  private readonly http = inject(HttpClient);
  private readonly BASE = '/api/profesionales';

  findAll(): Observable<ProfesionalResponse[]> {
    return this.http.get<ProfesionalResponse[]>(this.BASE).pipe(catchError(this.handleError));
  }

  findById(id: number): Observable<ProfesionalResponse> {
    return this.http.get<ProfesionalResponse>(`${this.BASE}/${id}`).pipe(catchError(this.handleError));
  }

  create(dto: ProfesionalCreate): Observable<ProfesionalResponse> {
    return this.http.post<ProfesionalResponse>(this.BASE, dto).pipe(catchError(this.handleError));
  }

  update(id: number, dto: ProfesionalUpdate): Observable<ProfesionalResponse> {
    return this.http.put<ProfesionalResponse>(`${this.BASE}/${id}`, dto).pipe(catchError(this.handleError));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${id}`).pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    return throwError(() => error);
  }
}
