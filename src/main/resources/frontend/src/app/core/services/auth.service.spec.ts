import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TokenResponse } from '../models/auth.model';
import { AuthService } from './auth.service';

const STORAGE_KEY = 'productos.auth.session';

const respuesta: TokenResponse = {
  accessToken: 'access-1',
  tokenType: 'Bearer',
  expiresIn: 1200,
  refreshToken: 'refresh-1',
  refreshExpiresIn: 604800,
  usuario: { id: 1, username: 'admin', roles: ['ADMIN'] },
};

const respuestaRenovada: TokenResponse = {
  ...respuesta,
  accessToken: 'access-2',
  refreshToken: 'refresh-2',
};

describe('AuthService', () => {
  let servicio: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    servicio = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('inicia sesion, guarda la sesion y expone usuario y roles', () => {
    let resultado: unknown;

    servicio.iniciarSesion({ username: 'admin', password: 'admin123' }).subscribe(
      (sesion) => (resultado = sesion),
    );

    const peticion = http.expectOne('/api/auth/login');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({
      username: 'admin',
      password: 'admin123',
    });
    peticion.flush(respuesta);

    expect(servicio.autenticado()).toBe(true);
    expect(servicio.esAdmin()).toBe(true);
    expect(servicio.roles()).toEqual(['ADMIN']);
    expect(servicio.usuario()?.username).toBe('admin');
    expect(servicio.accessToken()).toBe('access-1');
    expect(resultado).toBeTruthy();
    expect(localStorage.getItem(STORAGE_KEY)).toContain('access-1');
  });

  it('recupera la sesion guardada en localStorage', () => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        accessToken: 'guardado',
        tokenType: 'Bearer',
        expiresAt: Date.now() + 600_000,
        refreshToken: 'refresh-guardado',
        refreshExpiresAt: Date.now() + 600_000,
        usuario: { id: 2, username: 'user', roles: ['USER'] },
      }),
    );

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const nuevoServicio = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);

    expect(nuevoServicio.autenticado()).toBe(true);
    expect(nuevoServicio.esAdmin()).toBe(false);
    expect(nuevoServicio.accessToken()).toBe('guardado');
  });

  it('renueva la sesion y comparte una sola peticion concurrente', () => {
    servicio.iniciarSesion({ username: 'admin', password: 'admin123' }).subscribe();
    http.expectOne('/api/auth/login').flush(respuesta);

    let primera: unknown;
    let segunda: unknown;
    servicio.refrescar().subscribe((sesion) => (primera = sesion));
    servicio.refrescar().subscribe((sesion) => (segunda = sesion));

    const peticion = http.expectOne('/api/auth/refresh');
    expect(peticion.request.body).toEqual({ refreshToken: 'refresh-1' });
    peticion.flush(respuestaRenovada);

    expect(primera).toBeTruthy();
    expect(segunda).toBeTruthy();
    expect(servicio.accessToken()).toBe('access-2');
    expect(servicio.refreshToken()).toBe('refresh-2');
  });

  it('limpia la sesion cuando la renovacion falla', () => {
    servicio.iniciarSesion({ username: 'admin', password: 'admin123' }).subscribe();
    http.expectOne('/api/auth/login').flush(respuesta);

    servicio.refrescar().subscribe({ error: () => undefined });
    http
      .expectOne('/api/auth/refresh')
      .flush({ detail: 'Token invalido' }, { status: 401, statusText: 'Unauthorized' });

    expect(servicio.autenticado()).toBe(false);
    expect(localStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it('cierra la sesion y notifica al backend', () => {
    servicio.iniciarSesion({ username: 'admin', password: 'admin123' }).subscribe();
    http.expectOne('/api/auth/login').flush(respuesta);

    servicio.cerrarSesion();

    const peticion = http.expectOne('/api/auth/logout');
    expect(peticion.request.body).toEqual({ refreshToken: 'refresh-1' });
    peticion.flush({});

    expect(servicio.autenticado()).toBe(false);
    expect(servicio.usuario()).toBeNull();
  });
});
