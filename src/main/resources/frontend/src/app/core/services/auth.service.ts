import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import {
  Observable,
  catchError,
  finalize,
  map,
  shareReplay,
  throwError,
} from 'rxjs';
import {
  LoginRequest,
  Sesion,
  TokenResponse,
  UsuarioSesion,
} from '../models/auth.model';

const STORAGE_KEY = 'productos.auth.session';
const RENOVACION_ANTICIPADA_MS = 60_000;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly sesion = signal<Sesion | null>(this.leerSesion());
  private readonly refrescando = signal<Observable<Sesion> | null>(null);
  private temporizador?: ReturnType<typeof setTimeout>;

  readonly usuario = computed<UsuarioSesion | null>(() => this.sesion()?.usuario ?? null);
  readonly roles = computed(() =>
    (this.sesion()?.usuario.roles ?? []).map((rol) => rol.replace(/^ROLE_/, '')),
  );
  readonly autenticado = computed(() => this.sesion() !== null);
  readonly esAdmin = computed(() => this.roles().includes('ADMIN'));
  readonly accessToken = computed(() => this.sesion()?.accessToken ?? null);
  readonly refreshToken = computed(() => this.sesion()?.refreshToken ?? null);

  constructor() {
    this.programarRenovacion();
  }

  iniciarSesion(request: LoginRequest): Observable<Sesion> {
    return this.http
      .post<TokenResponse>('/api/auth/login', request)
      .pipe(map((respuesta) => this.guardarSesion(respuesta)));
  }

  refrescar(): Observable<Sesion> {
    const sesionActual = this.sesion();
    if (!sesionActual?.refreshToken) {
      return throwError(() => new Error('No hay una sesion activa'));
    }

    const refrescoEnCurso = this.refrescando();
    if (refrescoEnCurso) {
      return refrescoEnCurso;
    }

    const solicitud = this.http
      .post<TokenResponse>('/api/auth/refresh', {
        refreshToken: sesionActual.refreshToken,
      })
      .pipe(
        map((respuesta) => this.guardarSesion(respuesta)),
        catchError((error) => {
          this.limpiarSesion();
          return throwError(() => error);
        }),
        finalize(() => this.refrescando.set(null)),
        shareReplay({ bufferSize: 1, refCount: false }),
      );

    this.refrescando.set(solicitud);
    return solicitud;
  }

  cerrarSesion(): void {
    const refreshToken = this.refreshToken();
    this.limpiarSesion();

    if (refreshToken) {
      this.http
        .post<void>('/api/auth/logout', { refreshToken })
        .subscribe({ error: () => undefined });
    }
  }

  limpiarSesion(): void {
    if (this.temporizador) {
      clearTimeout(this.temporizador);
      this.temporizador = undefined;
    }

    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(STORAGE_KEY);
    }
    this.sesion.set(null);
  }

  private guardarSesion(respuesta: TokenResponse): Sesion {
    const ahora = Date.now();
    const sesion: Sesion = {
      accessToken: respuesta.accessToken,
      tokenType: respuesta.tokenType,
      expiresAt: ahora + respuesta.expiresIn * 1000,
      refreshToken: respuesta.refreshToken,
      refreshExpiresAt: ahora + respuesta.refreshExpiresIn * 1000,
      usuario: respuesta.usuario,
    };

    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(sesion));
    }
    this.sesion.set(sesion);
    this.programarRenovacion();
    return sesion;
  }

  private programarRenovacion(): void {
    if (this.temporizador) {
      clearTimeout(this.temporizador);
      this.temporizador = undefined;
    }

    const sesionActual = this.sesion();
    if (!sesionActual) {
      return;
    }

    const espera = sesionActual.expiresAt - Date.now() - RENOVACION_ANTICIPADA_MS;
    this.temporizador = setTimeout(() => {
      this.refrescar().subscribe({ error: () => undefined });
    }, Math.max(espera, 0));
  }

  private leerSesion(): Sesion | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    try {
      const valor = localStorage.getItem(STORAGE_KEY);
      if (!valor) {
        return null;
      }

      const sesion = JSON.parse(valor) as Sesion;
      if (!sesion.accessToken || !sesion.refreshToken || !sesion.usuario) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return sesion;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}
