import { CurrencyPipe, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ResumenVentas } from '../../core/models/vendedor.model';
import { VendedorService } from '../../core/services/vendedor.service';

@Component({
  selector: 'app-vendedor-ventas',
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './vendedor-ventas.component.html',
  styleUrl: './vendedor-ventas.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VendedorVentasComponent implements OnInit {
  private readonly vendedorService = inject(VendedorService);
  private readonly destroyRef = inject(DestroyRef);

  readonly resumen = signal<ResumenVentas | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.vendedorService
      .obtenerVentas()
      .pipe(
        finalize(() => this.cargando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (resumen) => this.resumen.set(resumen),
        error: (error: Error) => this.error.set(error.message),
      });
  }
}
