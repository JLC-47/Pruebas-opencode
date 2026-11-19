import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AdminService } from './admin.service';

describe('AdminService', () => {
  let service: AdminService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AdminService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista usuarios y roles', () => {
    service.listarUsuarios().subscribe();
    service.listarRoles().subscribe();

    expect(http.expectOne('/api/admin/usuarios').request.method).toBe('GET');
    expect(http.expectOne('/api/admin/roles').request.method).toBe('GET');
    http.match(() => true).forEach((peticion) => peticion.flush([]));
  });

  it('crea, actualiza y elimina usuarios', () => {
    const request = { username: 'vendedor', rol: 'VENDEDOR' as const, enabled: true };

    service.crearUsuario(request).subscribe();
    service.actualizarUsuario(2, request).subscribe();
    service.eliminarUsuario(2).subscribe();

    const creacion = http.expectOne('/api/admin/usuarios');
    expect(creacion.request.method).toBe('POST');
    creacion.flush(null);

    const actualizacion = http.expectOne(
      (request) => request.url === '/api/admin/usuarios/2' && request.method === 'PUT',
    );
    actualizacion.flush(null);

    const eliminacion = http.expectOne(
      (request) => request.url === '/api/admin/usuarios/2' && request.method === 'DELETE',
    );
    eliminacion.flush(null);
  });

  it('cambia el rol y el estado', () => {
    service.cambiarRol(3, 'USER').subscribe();
    service.cambiarEstado(3, false).subscribe();

    const rol = http.expectOne('/api/admin/usuarios/3/rol');
    expect(rol.request.body).toEqual({ rol: 'USER' });
    rol.flush(null);

    const estado = http.expectOne((request) => request.url === '/api/admin/usuarios/3/estado');
    expect(estado.request.params.get('enabled')).toBe('false');
    estado.flush(null);
  });
});
