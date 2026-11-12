import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { App } from './app';
import { AuthService } from './core/services/auth.service';
import { CartService } from './core/services/cart.service';

describe('App', () => {
  const authMock = {
    autenticado: signal(true),
    usuario: signal({ id: 1, username: 'admin', roles: ['ADMIN'] }),
    roles: signal(['ADMIN']),
    cerrarSesion: vi.fn(),
  };

  const cartMock = {
    cantidadTotal: signal(2),
    limpiar: vi.fn(),
  };

  beforeEach(async () => {
    authMock.autenticado.set(true);
    authMock.cerrarSesion.mockReset();
    cartMock.cantidadTotal.set(2);
    cartMock.limpiar.mockReset();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authMock },
        { provide: CartService, useValue: cartMock },
      ],
    }).compileComponents();
  });

  it('deberia crear la aplicacion', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('deberia mostrar la marca, la navegacion principal, el carrito y el usuario', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.brand')?.textContent).toContain('Productos API');
    expect(compiled.querySelectorAll('.app-nav a').length).toBe(4);
    expect(compiled.querySelector('.cart-count')?.textContent).toContain('2');
    expect(compiled.querySelector('.session')?.textContent).toContain('admin');
    expect(compiled.querySelector('.session')?.textContent).toContain('ADMIN');
  });

  it('cierra la sesion, limpia el carrito y navega al login', () => {
    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    const boton = (fixture.nativeElement as HTMLElement).querySelector(
      '.session-logout',
    ) as HTMLButtonElement;
    boton.click();

    expect(authMock.cerrarSesion).toHaveBeenCalled();
    expect(cartMock.limpiar).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });

  it('muestra el enlace de login cuando no hay sesion', () => {
    authMock.autenticado.set(false);

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.session-login')?.textContent).toContain(
      'Iniciar sesion',
    );
    expect(compiled.querySelectorAll('.app-nav a').length).toBe(2);
  });
});
