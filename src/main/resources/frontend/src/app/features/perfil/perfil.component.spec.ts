import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { Perfil } from '../../core/models/perfil.model';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { PerfilService } from '../../core/services/perfil.service';
import { PerfilComponent } from './perfil.component';

const perfil: Perfil = {
  id: 1,
  username: 'cliente',
  roles: ['USER'],
  nombre: 'Cliente Ejemplo',
  bio: 'Me gusta comprar tecnologia',
  avatarUrl: '',
  tema: 'claro',
  idioma: 'es',
  notificacionesActivas: true,
  enabled: true,
};

describe('PerfilComponent', () => {
  let perfilMock: {
    obtener: ReturnType<typeof vi.fn>;
    actualizar: ReturnType<typeof vi.fn>;
    cambiarContrasena: ReturnType<typeof vi.fn>;
  };
  let authMock: {
    usuario: ReturnType<typeof signal>;
    actualizarUsuario: ReturnType<typeof vi.fn>;
  };
  let notificationMock: {
    exito: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
  };

  async function configurar() {
    perfilMock = {
      obtener: vi.fn(() => of(perfil)),
      actualizar: vi.fn(() => of(perfil)),
      cambiarContrasena: vi.fn(() => of(void 0)),
    };
    authMock = {
      usuario: signal({ id: 1, username: 'cliente', roles: ['USER'] }),
      actualizarUsuario: vi.fn(),
    };
    notificationMock = { exito: vi.fn(), error: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [PerfilComponent],
      providers: [
        provideRouter([]),
        { provide: PerfilService, useValue: perfilMock },
        { provide: AuthService, useValue: authMock },
        { provide: NotificationService, useValue: notificationMock },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(PerfilComponent);
    return { fixture, componente: fixture.componentInstance };
  }

  it('carga el perfil y rellena el formulario', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    expect(perfilMock.obtener).toHaveBeenCalled();
    expect(componente.form.getRawValue()).toEqual({
      nombre: 'Cliente Ejemplo',
      bio: 'Me gusta comprar tecnologia',
      avatarUrl: '',
      tema: 'claro',
      idioma: 'es',
      notificacionesActivas: true,
    });
  });

  it('guarda las preferencias y actualiza la sesion', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    componente.form.patchValue({
      nombre: 'Ana Perez',
      tema: 'oscuro',
      idioma: 'en',
      notificacionesActivas: false,
    });
    componente.guardar();

    expect(perfilMock.actualizar).toHaveBeenCalledWith(
      expect.objectContaining({ nombre: 'Ana Perez', tema: 'oscuro', idioma: 'en' }),
    );
    expect(authMock.actualizarUsuario).toHaveBeenCalledWith(
      expect.objectContaining({ tema: 'claro', nombre: 'Cliente Ejemplo' }),
    );
    expect(notificationMock.exito).toHaveBeenCalled();
  });

  it('no guarda cuando el formulario es invalido', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    componente.form.controls.tema.setValue('rosa');
    componente.guardar();

    expect(perfilMock.actualizar).not.toHaveBeenCalled();
    expect(componente.form.controls.tema.touched).toBe(true);
  });

  it('cambia la contrasena y limpia el formulario', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    componente.passwordForm.setValue({
      contrasenaActual: 'claveActual123',
      contrasenaNueva: 'claveNueva123',
    });
    componente.cambiarContrasena();

    expect(perfilMock.cambiarContrasena).toHaveBeenCalledWith({
      contrasenaActual: 'claveActual123',
      contrasenaNueva: 'claveNueva123',
    });
    expect(componente.passwordForm.controls.contrasenaNueva.value).toBe('');
    expect(notificationMock.exito).toHaveBeenCalled();
  });

  it('muestra el error cuando no se puede cargar el perfil', async () => {
    const { fixture, componente } = await configurar();
    perfilMock.obtener.mockReturnValue(throwError(() => new Error('Fallo de red')));
    fixture.detectChanges();

    expect(componente.error()).toBe('Fallo de red');
  });
});
