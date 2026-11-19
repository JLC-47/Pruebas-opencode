import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, finalize } from 'rxjs';
import { RolNombre, RolResponse, UsuarioAdmin } from '../../core/models/admin.model';
import { AdminService } from '../../core/services/admin.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-admin-usuarios',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-usuarios.component.html',
  styleUrl: './admin-usuarios.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminUsuariosComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly adminService = inject(AdminService);
  private readonly notification = inject(NotificationService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly destroyRef = inject(DestroyRef);

  readonly rolesDisponibles: RolNombre[] = ['USER', 'VENDEDOR', 'ADMIN'];

  readonly crearForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(60)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    rol: ['USER' as RolNombre, [Validators.required]],
    enabled: [true],
  });

  readonly editarForm = this.fb.group({
    rol: ['USER' as RolNombre, [Validators.required]],
    enabled: [true],
  });

  readonly usuarios = signal<UsuarioAdmin[]>([]);
  readonly roles = signal<RolResponse[]>([]);
  readonly cargando = signal(true);
  readonly creando = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);
  readonly editandoId = signal<number | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    forkJoin({
      usuarios: this.adminService.listarUsuarios(),
      roles: this.adminService.listarRoles(),
    })
      .pipe(
        finalize(() => this.cargando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ usuarios, roles }) => {
          this.usuarios.set(usuarios);
          this.roles.set(roles);
        },
        error: (error: Error) => this.error.set(error.message),
      });
  }

  crear(): void {
    if (this.crearForm.invalid) {
      this.crearForm.markAllAsTouched();
      return;
    }

    this.creando.set(true);
    this.error.set(null);

    this.adminService
      .crearUsuario(this.crearForm.getRawValue())
      .pipe(
        finalize(() => this.creando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (usuario) => {
          this.notification.exito('Usuario creado', `${usuario.username} ya puede iniciar sesion.`);
          this.crearForm.reset({ username: '', password: '', rol: 'USER', enabled: true });
          this.cargar();
        },
        error: (error: Error) => {
          this.error.set(error.message);
          this.notification.error('No se pudo crear el usuario', error.message);
        },
      });
  }

  seleccionar(usuario: UsuarioAdmin): void {
    this.editandoId.set(usuario.id);
    this.editarForm.setValue({ rol: usuario.rol, enabled: usuario.enabled });
  }

  cancelarEdicion(): void {
    this.editandoId.set(null);
  }

  guardarEdicion(usuario: UsuarioAdmin): void {
    if (this.editarForm.invalid || this.editandoId() !== usuario.id) {
      return;
    }

    this.guardando.set(true);
    this.error.set(null);
    const values = this.editarForm.getRawValue();

    this.adminService
      .actualizarUsuario(usuario.id, {
        username: usuario.username,
        rol: values.rol,
        enabled: values.enabled,
      })
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.editandoId.set(null);
          this.notification.exito(
            'Usuario actualizado',
            `Permisos de ${usuario.username} actualizados.`,
          );
          this.cargar();
        },
        error: (error: Error) => {
          this.error.set(error.message);
          this.notification.error('No se pudo actualizar el usuario', error.message);
        },
      });
  }

  async eliminar(usuario: UsuarioAdmin): Promise<void> {
    const confirmado = await this.confirmDialog.confirmar({
      titulo: 'Eliminar usuario',
      mensaje: `Vas a eliminar la cuenta de ${usuario.username}.`,
      detalle: 'El usuario perdera el acceso y no podra recuperarse.',
      etiquetaConfirmar: 'Eliminar usuario',
      etiquetaCancelar: 'Conservar',
      peligro: true,
    });

    if (!confirmado) {
      return;
    }

    this.adminService
      .eliminarUsuario(usuario.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          if (this.editandoId() === usuario.id) {
            this.cancelarEdicion();
          }
          this.notification.exito('Usuario eliminado', `${usuario.username} fue eliminado.`);
          this.cargar();
        },
        error: (error: Error) => {
          this.error.set(error.message);
          this.notification.error('No se pudo eliminar el usuario', error.message);
        },
      });
  }

  descripcionRol(rol: RolNombre): string {
    return this.roles().find((item) => item.nombre === rol)?.descripcion ?? rol;
  }
}
