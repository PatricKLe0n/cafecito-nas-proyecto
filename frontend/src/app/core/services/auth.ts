import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models';

const TOKEN = 'rl_token';
const ROL = 'rl_rol';
const USER = 'rl_user';

/**
 * Punto único de gestión de la sesión. Persiste el JWT y los datos de usuario
 * en `localStorage` y expone su estado mediante signals reactivas (rol, usuario,
 * si es administrador y si hay sesión activa) para que el resto de la aplicación
 * reaccione a los cambios de autenticación.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  private readonly _rol = signal<string | null>(localStorage.getItem(ROL));
  private readonly _user = signal<string | null>(localStorage.getItem(USER));

  /** Rol del usuario en sesión (`ADMIN` / `USER`), o `null` si no hay sesión. */
  readonly rol = this._rol.asReadonly();
  /** Nombre del usuario en sesión, o `null` si no hay sesión. */
  readonly user = this._user.asReadonly();
  /** `true` cuando el rol es `ADMIN`; habilita las acciones de escritura. */
  readonly isAdmin = computed(() => this._rol() === 'ADMIN');
  /** `true` mientras exista un usuario en sesión. */
  readonly isAuthenticated = computed(() => !!this._user());

  /**
   * Autentica contra el backend y, si la respuesta es correcta, persiste el token,
   * el rol y el usuario en `localStorage` y actualiza las signals de estado.
   *
   * @param req Credenciales de acceso (usuario y contraseña).
   * @returns Observable con la respuesta del login (token, usuario, rol y caducidad).
   */
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

  /** JWT almacenado para esta sesión, o `null` si no hay token. */
  get token(): string | null {
    return localStorage.getItem(TOKEN);
  }

  /** Cierra la sesión: limpia `localStorage` y restablece las signals de estado. */
  logout(): void {
    localStorage.clear();
    this._rol.set(null);
    this._user.set(null);
  }
}
