import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { RolResponse, UsuarioAdmin } from '../../core/models/admin.model';
import { AdminService } from '../../core/services/admin.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { NotificationService } from '../../core/services/notification.service';
import { AdminUsuariosComponent } from './admin-usuarios.component';

const usuarios: UsuarioAdmin[] = [
  { id: 1, username: 'admin', rol: 'ADMIN', enabled: true, nombre: null, avatarUrl: null },
  { id: 2, username: 'vendedor', rol: 'VENDEDOR', enabled: true, nombre: null, avatarUrl: null },
];

const roles: RolResponse[] = [
  { nombre: 'ADMIN', descripcion: 'Acceso total', permisos: ['productos:write'] },
  { nombre: 'VENDEDOR', descripcion: 'Gestiona sus productos', permisos: ['productos:own'] },
];

describe('AdminUsuariosComponent', () => {
  let adminMock: Record<string, ReturnType<typeof vi.fn>>;
  let notificationMock: Record<string, ReturnType<typeof vi.fn>>;
  let confirmMock: { confirmar: ReturnType<typeof vi.fn> };

  async function configurar() {
    adminMock = {
      listarUsuarios: vi.fn(() => of(usuarios)),
      listarRoles: vi.fn(() => of(roles)),
      crearUsuario: vi.fn(() => of(usuarios[1])),
      actualizarUsuario: vi.fn(() => of(usuarios[1])),
      eliminarUsuario: vi.fn(() => of(void 0)),
    };
    notificationMock = { exito: vi.fn(), error: vi.fn() };
    confirmMock = { confirmar: vi.fn(() => Promise.resolve(true)) };

    await TestBed.configureTestingModule({
      imports: [AdminUsuariosComponent],
      providers: [
        provideRouter([]),
        { provide: AdminService, useValue: adminMock },
        { provide: NotificationService, useValue: notificationMock },
        { provide: ConfirmDialogService, useValue: confirmMock },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(AdminUsuariosComponent);
    return { fixture, componente: fixture.componentInstance };
  }

  it('carga usuarios y roles', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    expect(componente.usuarios()).toEqual(usuarios);
    expect(componente.roles()).toEqual(roles);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('vendedor');
  });

  it('crea un usuario con el rol indicado', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    componente.crearForm.setValue({
      username: 'vendedor2',
      password: 'claveSegura123',
      rol: 'VENDEDOR',
      enabled: true,
    });
    componente.crear();

    expect(adminMock['crearUsuario']).toHaveBeenCalledWith({
      username: 'vendedor2',
      password: 'claveSegura123',
      rol: 'VENDEDOR',
      enabled: true,
    });
    expect(notificationMock['exito']).toHaveBeenCalled();
  });

  it('actualiza el rol y el estado de un usuario', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    componente.seleccionar(usuarios[1]);
    componente.editarForm.setValue({ rol: 'USER', enabled: false });
    componente.guardarEdicion(usuarios[1]);

    expect(adminMock['actualizarUsuario']).toHaveBeenCalledWith(2, {
      username: 'vendedor',
      rol: 'USER',
      enabled: false,
    });
    expect(componente.editandoId()).toBeNull();
  });

  it('elimina un usuario tras confirmarlo', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    await componente.eliminar(usuarios[1]);

    expect(confirmMock.confirmar).toHaveBeenCalled();
    expect(adminMock['eliminarUsuario']).toHaveBeenCalledWith(2);
  });
});
