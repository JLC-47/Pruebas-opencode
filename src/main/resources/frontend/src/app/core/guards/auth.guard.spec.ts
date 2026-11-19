import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { AuthService } from '../services/auth.service';
import { adminGuard, authGuard, vendedorGuard } from './auth.guard';

describe('auth guards', () => {
  const authMock = {
    autenticado: vi.fn(() => false),
    esAdmin: vi.fn(() => false),
    esVendedor: vi.fn(() => false),
  };

  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/productos/5/editar' } as RouterStateSnapshot;

  beforeEach(() => {
    authMock.autenticado.mockReset().mockReturnValue(false);
    authMock.esAdmin.mockReset().mockReturnValue(false);
    authMock.esVendedor.mockReset().mockReturnValue(false);

    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: authMock }],
    });
  });

  it('authGuard permite el acceso con sesion activa', () => {
    authMock.autenticado.mockReturnValue(true);

    const resultado = TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(resultado).toBe(true);
  });

  it('authGuard redirige al login guardando la url de retorno', () => {
    const resultado = TestBed.runInInjectionContext(() => authGuard(route, state));

    expect(resultado).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(resultado as UrlTree)).toBe(
      '/login?returnUrl=%2Fproductos%2F5%2Feditar',
    );
  });

  it('adminGuard permite el acceso a un administrador', () => {
    authMock.autenticado.mockReturnValue(true);
    authMock.esAdmin.mockReturnValue(true);

    const resultado = TestBed.runInInjectionContext(() => adminGuard(route, state));
    expect(resultado).toBe(true);
  });

  it('adminGuard redirige al listado cuando el usuario no es administrador', () => {
    authMock.autenticado.mockReturnValue(true);

    const resultado = TestBed.runInInjectionContext(() => adminGuard(route, state));
    expect(resultado).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(resultado as UrlTree)).toBe('/productos');
  });

  it('vendedorGuard permite el acceso a vendedores y administradores', () => {
    authMock.autenticado.mockReturnValue(true);
    authMock.esVendedor.mockReturnValue(true);

    expect(TestBed.runInInjectionContext(() => vendedorGuard(route, state))).toBe(true);

    authMock.esVendedor.mockReturnValue(false);
    authMock.esAdmin.mockReturnValue(true);
    expect(TestBed.runInInjectionContext(() => vendedorGuard(route, state))).toBe(true);
  });

  it('vendedorGuard redirige a los clientes al catalogo', () => {
    authMock.autenticado.mockReturnValue(true);

    const resultado = TestBed.runInInjectionContext(() => vendedorGuard(route, state));
    expect(TestBed.inject(Router).serializeUrl(resultado as UrlTree)).toBe('/productos');
  });
});
