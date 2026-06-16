import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { Role } from '../models/profesional.model';

export interface CurrentUser {
  email: string;
  role: Role;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly TOKEN_KEY = 'magtea_token';

  currentUser = signal<CurrentUser | null>(this.restoreUser());

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/v1/auth/login', request).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        this.currentUser.set(this.decodeUser(response.token));
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = this.decodePayload(token);
      const exp = payload['exp'];
      return typeof exp === 'number' && exp > Date.now() / 1000;
    } catch {
      return false;
    }
  }

  private restoreUser(): CurrentUser | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return null;
    try {
      return this.decodeUser(token);
    } catch {
      return null;
    }
  }

  private decodeUser(token: string): CurrentUser {
    const payload = this.decodePayload(token);
    return {
      email: payload['sub'] as string,
      role: payload['role'] as Role,
    };
  }

  private decodePayload(token: string): Record<string, unknown> {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64)) as Record<string, unknown>;
  }
}
