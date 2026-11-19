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
import { finalize } from 'rxjs';
import { Perfil, PerfilRequest } from '../../core/models/perfil.model';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { PerfilService } from '../../core/services/perfil.service';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly perfilService = inject(PerfilService);
  private readonly auth = inject(AuthService);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  readonly temas = ['claro', 'oscuro', 'sistema'];
  readonly idiomas = ['es', 'en'];

  readonly form = this.fb.group({
    nombre: ['', [Validators.maxLength(80)]],
    bio: ['', [Validators.maxLength(300)]],
    avatarUrl: ['', [Validators.maxLength(500)]],
    tema: ['claro', [Validators.required, Validators.pattern(/^(claro|oscuro|sistema)$/)]],
    idioma: ['es', [Validators.required, Validators.pattern(/^(es|en)$/)]],
    notificacionesActivas: [true],
  });

  readonly passwordForm = this.fb.group({
    contrasenaActual: ['', [Validators.required]],
    contrasenaNueva: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly guardandoPassword = signal(false);
  readonly error = signal<string | null>(null);
  readonly perfil = signal<Perfil | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.perfilService
      .obtener()
      .pipe(
        finalize(() => this.cargando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (perfil) => this.aplicarPerfil(perfil),
        error: (error: Error) => this.error.set(error.message),
      });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);
    const request: PerfilRequest = this.form.getRawValue();

    this.perfilService
      .actualizar(request)
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (perfil) => {
          this.aplicarPerfil(perfil);
          const usuario = this.auth.usuario();
          if (usuario) {
            this.auth.actualizarUsuario({
              ...usuario,
              nombre: perfil.nombre ?? undefined,
              bio: perfil.bio ?? undefined,
              avatarUrl: perfil.avatarUrl ?? undefined,
              tema: perfil.tema,
              idioma: perfil.idioma,
              notificacionesActivas: perfil.notificacionesActivas,
            });
          }
          this.notification.exito(
            'Perfil actualizado',
            'Tus preferencias se guardaron correctamente.',
          );
        },
        error: (error: Error) => {
          this.error.set(error.message);
          this.notification.error('No se pudo guardar el perfil', error.message);
        },
      });
  }

  cambiarContrasena(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.guardandoPassword.set(true);
    this.error.set(null);

    this.perfilService
      .cambiarContrasena(this.passwordForm.getRawValue())
      .pipe(
        finalize(() => this.guardandoPassword.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.passwordForm.reset({
            contrasenaActual: '',
            contrasenaNueva: '',
          });
          this.notification.exito(
            'Contrasena actualizada',
            'Ya puedes iniciar sesion con la nueva clave.',
          );
        },
        error: (error: Error) => {
          this.error.set(error.message);
          this.notification.error('No se pudo cambiar la contrasena', error.message);
        },
      });
  }

  avatarTexto(): string {
    const nombre = this.form.controls.nombre.value.trim();
    const username = this.perfil()?.username ?? this.auth.usuario()?.username ?? '?';
    return (nombre || username).charAt(0).toUpperCase();
  }

  private aplicarPerfil(perfil: Perfil): void {
    this.perfil.set(perfil);
    this.form.setValue({
      nombre: perfil.nombre ?? '',
      bio: perfil.bio ?? '',
      avatarUrl: perfil.avatarUrl ?? '',
      tema: perfil.tema,
      idioma: perfil.idioma,
      notificacionesActivas: perfil.notificacionesActivas,
    });
  }
}
