import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Sesion } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { RegistroComponent } from './registro.component';

const sesion: Sesion = {
  accessToken: 'access',
  tokenType: 'Bearer',
  expiresAt: Date.now() + 600_000,
  refreshToken: 'refresh',
  refreshExpiresAt: Date.now() + 600_000,
  usuario: { id: 3, username: 'nuevo.cliente', roles: ['USER'] },
};

async function configurar() {
  const authMock = {
    registrar: vi.fn(() => of(sesion)),
  };

  const cartMock = {
    limpiar: vi.fn(),
  };

  await TestBed.configureTestingModule({
    imports: [RegistroComponent],
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: authMock },
      { provide: CartService, useValue: cartMock },
    ],
  }).compileComponents();

  const fixture = TestBed.createComponent(RegistroComponent);
  return {
    fixture,
    componente: fixture.componentInstance,
    authMock,
    cartMock,
    router: TestBed.inject(Router),
  };
}

describe('RegistroComponent', () => {
  it('no envia el formulario cuando faltan datos', async () => {
    const { fixture, componente, authMock } = await configurar();
    fixture.detectChanges();

    componente.registrar();
    fixture.detectChanges();

    expect(componente.form.invalid).toBe(true);
    expect(authMock.registrar).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'El usuario es obligatorio',
    );
  });

  it('no registra cuando las contrasenas no coinciden', async () => {
    const { fixture, componente, authMock } = await configurar();
    fixture.detectChanges();

    componente.form.setValue({
      username: 'nuevo.cliente',
      password: 'claveSegura123',
      confirmPassword: 'otraClave123',
    });
    componente.registrar();
    fixture.detectChanges();

    expect(componente.form.hasError('contrasenasDistintas')).toBe(true);
    expect(authMock.registrar).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Las contrasenas no coinciden',
    );
  });

  it('registra al cliente, limpia el carrito y navega al catalogo', async () => {
    const { fixture, componente, authMock, cartMock, router } = await configurar();
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.detectChanges();

    componente.form.setValue({
      username: 'nuevo.cliente',
      password: 'claveSegura123',
      confirmPassword: 'claveSegura123',
    });
    componente.registrar();

    expect(authMock.registrar).toHaveBeenCalledWith({
      username: 'nuevo.cliente',
      password: 'claveSegura123',
    });
    expect(cartMock.limpiar).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith('/productos');
  });

  it('muestra el error cuando el usuario ya existe', async () => {
    const { fixture, componente, authMock } = await configurar();
    authMock.registrar.mockReturnValue(
      throwError(() => new Error("El usuario 'nuevo.cliente' ya esta registrado")),
    );
    fixture.detectChanges();

    componente.form.setValue({
      username: 'nuevo.cliente',
      password: 'claveSegura123',
      confirmPassword: 'claveSegura123',
    });
    componente.registrar();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      "El usuario 'nuevo.cliente' ya esta registrado",
    );
  });

  it('alterna la visibilidad de ambas contrasenas de forma independiente', async () => {
    const { fixture } = await configurar();
    fixture.detectChanges();

    const raiz = fixture.nativeElement as HTMLElement;
    const password = raiz.querySelector<HTMLInputElement>('#registro-password');
    const confirmPassword = raiz.querySelector<HTMLInputElement>('#registro-confirm-password');
    const botones = raiz.querySelectorAll<HTMLButtonElement>('.password-toggle');

    expect(password?.type).toBe('password');
    expect(confirmPassword?.type).toBe('password');

    botones[0].click();
    fixture.detectChanges();

    expect(password?.type).toBe('text');
    expect(confirmPassword?.type).toBe('password');

    botones[1].click();
    fixture.detectChanges();

    expect(password?.type).toBe('text');
    expect(confirmPassword?.type).toBe('text');

    botones[0].click();
    fixture.detectChanges();

    expect(password?.type).toBe('password');
    expect(confirmPassword?.type).toBe('text');
  });
});
