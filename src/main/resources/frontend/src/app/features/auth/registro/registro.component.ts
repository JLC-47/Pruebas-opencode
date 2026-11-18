import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';

function contrasenasCoinciden(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;

  if (!password || !confirmPassword || password === confirmPassword) {
    return null;
  }

  return { contrasenasDistintas: true };
}

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.component.html',
  styleUrl: '../login/login.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegistroComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly cart = inject(CartService);
  private readonly router = inject(Router);

  readonly form = this.fb.group(
    {
      username: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(60),
          Validators.pattern(/^[a-zA-Z0-9._-]+$/),
        ],
      ],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: contrasenasCoinciden },
  );

  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly mostrarPassword = signal(false);
  readonly mostrarConfirmPassword = signal(false);

  alternarPassword(): void {
    this.mostrarPassword.update((visible) => !visible);
  }

  alternarConfirmPassword(): void {
    this.mostrarConfirmPassword.update((visible) => !visible);
  }

  registrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    const { username, password } = this.form.getRawValue();

    this.auth
      .registrar({ username: username.trim(), password })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: () => {
          this.cart.limpiar();
          this.router.navigateByUrl('/productos');
        },
        error: (error: Error) => this.error.set(error.message),
      });
  }
}
