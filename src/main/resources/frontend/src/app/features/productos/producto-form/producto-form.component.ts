import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ProductoService } from '../../../core/services/producto.service';

const precioValido: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value;
  if (valor === null || valor === undefined || valor === '') {
    return null;
  }

  const numero = Number(valor);
  if (Number.isNaN(numero) || numero < 0) {
    return { precioInvalido: true };
  }

  const decimales = String(numero).split('.')[1]?.length ?? 0;
  return decimales > 2 ? { decimalesInvalidos: true } : null;
};

const stockEntero: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value;
  if (valor === null || valor === undefined || valor === '') {
    return null;
  }
  return Number.isInteger(Number(valor)) ? null : { stockInvalido: true };
};

@Component({
  selector: 'app-producto-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './producto-form.component.html',
  styleUrl: './producto-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductoFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly productoService = inject(ProductoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly categorias = ['Tecnologia', 'Hogar', 'Oficina', 'Accesorios'];

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    precio: [
      0,
      [
        Validators.required,
        Validators.min(0),
        Validators.max(9999999999.99),
        precioValido,
      ],
    ],
    stock: [0, [Validators.required, Validators.min(0), stockEntero]],
    categoria: ['', [Validators.maxLength(60)]],
    descripcion: ['', [Validators.maxLength(300)]],
  });

  readonly editando = signal(false);
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly error = signal<string | null>(null);

  private productoId?: number;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    const numeroId = Number(id);
    if (!Number.isFinite(numeroId)) {
      this.error.set('El id del producto no es valido');
      return;
    }

    this.productoId = numeroId;
    this.editando.set(true);
    this.cargarProducto(numeroId);
  }

  cargarProducto(id: number): void {
    this.cargando.set(true);
    this.error.set(null);

    this.productoService
      .obtener(id)
      .pipe(
        finalize(() => this.cargando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (producto) => {
          this.form.patchValue({
            nombre: producto.nombre,
            precio: producto.precio,
            stock: producto.stock,
            categoria: producto.categoria ?? '',
            descripcion: producto.descripcion ?? '',
          });
        },
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

    const request = this.form.getRawValue();
    const operacion = this.editando() && this.productoId !== undefined
      ? this.productoService.actualizar(this.productoId, request)
      : this.productoService.crear(request);

    operacion
      .pipe(
        finalize(() => this.guardando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => this.router.navigate(['/productos']),
        error: (error: Error) => this.error.set(error.message),
      });
  }
}
