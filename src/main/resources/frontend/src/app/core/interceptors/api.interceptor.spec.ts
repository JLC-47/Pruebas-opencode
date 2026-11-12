import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ApiError, apiInterceptor } from './api.interceptor';

describe('apiInterceptor', () => {
  let httpClient: HttpClient;
  let http: HttpTestingController;
  let router: Router;

  const authMock = {
    accessToken: vi.fn<() => string | null>(() => 'token-inicial'),
    refrescar: vi.fn(),
    limpiarSesion: vi.fn(),
  };

  beforeEach(() => {
    authMock.accessToken.mockReset().mockReturnValue('token-inicial');
    authMock.refrescar.mockReset();
    authMock.limpiarSesion.mockReset();

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([apiInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authMock },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => http.verify());

  it('agrega las cabeceras de la API y el token a la solicitud', () => {
    httpClient.get('/api/productos').subscribe();

    const peticion = http.expectOne('/api/productos');
    expect(peticion.request.headers.get('Accept')).toBe('application/json');
    expect(peticion.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
    expect(peticion.request.headers.get('Authorization')).toBe('Bearer token-inicial');

    peticion.flush([]);
  });

  it('no agrega el token a los endpoints de autenticacion', () => {
    httpClient.post('/api/auth/login', {}).subscribe();

    const peticion = http.expectOne('/api/auth/login');
    expect(peticion.request.headers.has('Authorization')).toBe(false);

    peticion.flush({});
  });

  it('renueva el token y reintenta la solicitud cuando recibe 401', () => {
    authMock.refrescar.mockImplementation(() => {
      authMock.accessToken.mockReturnValue('token-renovado');
      return of({});
    });
    let respuesta: unknown;

    httpClient.get('/api/productos').subscribe((valor) => (respuesta = valor));

    const primera = http.expectOne('/api/productos');
    expect(primera.request.headers.get('Authorization')).toBe('Bearer token-inicial');
    primera.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(authMock.refrescar).toHaveBeenCalledTimes(1);

    const reintento = http.expectOne('/api/productos');
    expect(reintento.request.headers.get('Authorization')).toBe('Bearer token-renovado');
    reintento.flush([{ id: 1 }]);

    expect(respuesta).toEqual([{ id: 1 }]);
  });

  it('limpia la sesion y navega al login cuando falla la renovacion', () => {
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    authMock.refrescar.mockReturnValue(throwError(() => new Error('refresh invalido')));
    let capturado: unknown;

    httpClient.get('/api/productos').subscribe({
      error: (error) => (capturado = error),
    });

    const peticion = http.expectOne('/api/productos');
    peticion.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(authMock.limpiarSesion).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/' },
    });
    expect(capturado).toBeInstanceOf(ApiError);
    expect((capturado as ApiError).status).toBe(401);
  });

  it('convierte los errores de validacion en ApiError con detalle', () => {
    let capturado: unknown;

    httpClient.post('/api/productos', {}).subscribe({
      error: (error) => (capturado = error),
    });

    const peticion = http.expectOne('/api/productos');
    peticion.flush(
      {
        title: 'Error de validacion',
        detail: 'La solicitud contiene datos invalidos',
        errors: {
          nombre: 'El nombre es obligatorio',
          precio: 'El precio no puede ser negativo',
        },
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(capturado).toBeInstanceOf(ApiError);
    const apiError = capturado as ApiError;
    expect(apiError.status).toBe(400);
    expect(apiError.message).toContain('La solicitud contiene datos invalidos');
    expect(apiError.message).toContain('El nombre es obligatorio');
    expect(apiError.message).toContain('El precio no puede ser negativo');
  });

  it('informa cuando no hay conexion con el servidor', () => {
    let capturado: unknown;

    httpClient.get('/api/productos').subscribe({
      error: (error) => (capturado = error),
    });

    const peticion = http.expectOne('/api/productos');
    peticion.error(new ProgressEvent('error'));

    expect(capturado).toBeInstanceOf(ApiError);
    const apiError = capturado as ApiError;
    expect(apiError.status).toBe(0);
    expect(apiError.message).toContain('No se pudo conectar con el servidor');
  });
});
