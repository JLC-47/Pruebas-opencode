import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PerfilService } from './perfil.service';

describe('PerfilService', () => {
  let service: PerfilService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PerfilService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('obtiene el perfil autenticado', () => {
    service.obtener().subscribe();
    http.expectOne('/api/perfil').flush({ username: 'user' });
  });

  it('actualiza las preferencias del perfil', () => {
    const request = {
      nombre: 'Ana',
      bio: '',
      avatarUrl: '',
      tema: 'oscuro',
      idioma: 'es',
      notificacionesActivas: true,
    };

    service.actualizar(request).subscribe();

    const peticion = http.expectOne('/api/perfil');
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual(request);
    peticion.flush({});
  });

  it('cambia la contrasena', () => {
    const request = { contrasenaActual: 'clave1234', contrasenaNueva: 'clave5678' };

    service.cambiarContrasena(request).subscribe();

    const peticion = http.expectOne('/api/perfil/password');
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual(request);
    peticion.flush(null);
  });
});
