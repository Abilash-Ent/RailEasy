import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  CurrentUser,
  LoginRequest,
  RegisterRequest,
} from '../models/auth.model';

const TOKEN_KEY = 'raileasy_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  private readonly _token = signal<string | null>(
    localStorage.getItem(TOKEN_KEY)
  );

  readonly currentUser = computed<CurrentUser | null>(() =>
    this.decodeUser(this._token())
  );
  readonly isLoggedIn = computed(() => this.currentUser() !== null);
  readonly isAdmin = computed(() => this.currentUser()?.isAdmin ?? false);

  get token(): string | null {
    return this._token();
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, payload)
      .pipe(tap((res) => this.setToken(res.token)));
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, payload)
      .pipe(tap((res) => this.setToken(res.token)));
  }

  logout(): void {
    this.http.post(`${this.baseUrl}/logout`, {}).subscribe({
      next: () => {},
      error: () => {},
    });
    this.clearToken();
    this.router.navigate(['/login']);
  }

  private setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this._token.set(token);
  }

  private clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
    this._token.set(null);
  }

  private decodeUser(token: string | null): CurrentUser | null {
    if (!token) {
      return null;
    }
    try {
      const payloadPart = token.split('.')[1];
      const json = JSON.parse(
        atob(payloadPart.replace(/-/g, '+').replace(/_/g, '/'))
      );

      // Token expiry check
      if (json.exp && Date.now() >= json.exp * 1000) {
        localStorage.removeItem(TOKEN_KEY);
        return null;
      }

      const roles: string[] = this.extractRoles(json);
      const isAdmin = roles.some((r) => r.toUpperCase().includes('ADMIN'));

      return {
        email: json.sub ?? json.email ?? '',
        fullName: json.fullName ?? json.name,
        roles,
        isAdmin,
      };
    } catch {
      return null;
    }
  }

  private extractRoles(json: Record<string, unknown>): string[] {
    const raw =
      json['roles'] ?? json['authorities'] ?? json['role'] ?? json['scope'];
    if (Array.isArray(raw)) {
      return raw.map((r) => String(r));
    }
    if (typeof raw === 'string') {
      return raw.split(/[\s,]+/).filter(Boolean);
    }
    return [];
  }
}
