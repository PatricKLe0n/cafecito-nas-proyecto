import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models';

const TOKEN = 'rl_token';
const ROL = 'rl_rol';
const USER = 'rl_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  private readonly _rol = signal<string | null>(localStorage.getItem(ROL));
  private readonly _user = signal<string | null>(localStorage.getItem(USER));

  readonly rol = this._rol.asReadonly();
  readonly user = this._user.asReadonly();
  readonly isAdmin = computed(() => this._rol() === 'ADMIN');
  readonly isAuthenticated = computed(() => !!this._user());

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, req).pipe(
      tap((res) => {
        localStorage.setItem(TOKEN, res.token);
        localStorage.setItem(ROL, res.rol);
        localStorage.setItem(USER, res.username);
        this._rol.set(res.rol);
        this._user.set(res.username);
      }),
    );
  }

  get token(): string | null {
    return localStorage.getItem(TOKEN);
  }

  logout(): void {
    localStorage.clear();
    this._rol.set(null);
    this._user.set(null);
  }
}
