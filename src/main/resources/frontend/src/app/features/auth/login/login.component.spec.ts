import { TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  Router,
  convertToParamMap,
  provideRouter,
} from '@angular/router';
import { of, throwError } from 'rxjs';
import { Sesion } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { LoginComponent } from './login.component';

const sesion: Sesion = {
  accessToken: 'access',
  tokenType: 'Bearer',
  expiresAt: Date.now() + 600_000,
  refreshToken: 'refresh',
  refreshExpiresAt: Date.now() + 600_000,
  usuario: { id: 1, username: 'admin', roles: ['ADMIN'] },
};

async function configurar(returnUrl?: string) {
  const authMock = {
    iniciarSesion: vi.fn(() => of(sesion)),
  };

  const cartMock = {
    limpiar: vi.fn(),
  };

  await TestBed.configureTestingModule({
    imports: [LoginComponent],
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: authMock },
      { provide: CartService, useValue: cartMock },
      {
        provide: ActivatedRoute,
        useValue: {
          snapshot: {
            queryParamMap: convertToParamMap(returnUrl ? { returnUrl } : {}),
          },
        },
      },
    ],
  }).compileComponents();

  const fixture = TestBed.createComponent(LoginComponent);
  return {
    fixture,
    componente: fixture.componentInstance,
    authMock,
    cartMock,
    router: TestBed.inject(Router),
  };
}

describe('LoginComponent', () => {
  it('no envia el formulario cuando faltan credenciales', async () => {
    const { fixture, componente, authMock } = await configurar();
    fixture.detectChanges();

    componente.iniciarSesion();
    fixture.detectChanges();

    expect(componente.form.invalid).toBe(true);
    expect(authMock.iniciarSesion).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'El usuario es obligatorio',
    );
  });

  it('inicia sesion, limpia el carrito y navega a la url de retorno', async () => {
    const { fixture, componente, authMock, cartMock, router } =
      await configurar('/productos/5/editar');
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.detectChanges();

    componente.form.setValue({ username: 'admin', password: 'admin123' });
    componente.iniciarSesion();

    expect(authMock.iniciarSesion).toHaveBeenCalledWith({
      username: 'admin',
      password: 'admin123',
    });
    expect(cartMock.limpiar).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith('/productos/5/editar');
  });

  it('navega al listado cuando no hay url de retorno', async () => {
    const { fixture, componente, router } = await configurar();
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.detectChanges();

    componente.form.setValue({ username: 'user', password: 'user123' });
    componente.iniciarSesion();

    expect(navegar).toHaveBeenCalledWith('/productos');
  });

  it('muestra el error cuando las credenciales son invalidas', async () => {
    const { fixture, componente, authMock } = await configurar();
    authMock.iniciarSesion.mockReturnValue(
      throwError(() => new Error('Credenciales invalidas')),
    );
    fixture.detectChanges();

    componente.form.setValue({ username: 'admin', password: 'incorrecta' });
    componente.iniciarSesion();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Credenciales invalidas',
    );
  });

  it('completa el formulario con las credenciales de demostracion', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    componente.usarCredenciales('user', 'user123');

    expect(componente.form.getRawValue()).toEqual({
      username: 'user',
      password: 'user123',
    });
  });
});
