import { CurrencyPipe } from '@angular/common';
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
import { Producto } from '../../core/models/producto.model';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { NotificationService } from '../../core/services/notification.service';
import { ProductoService } from '../../core/services/producto.service';

@Component({
  selector: 'app-vendedor-productos',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './vendedor-productos.component.html',
  styleUrl: './vendedor-productos.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VendedorProductosComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly notification = inject(NotificationService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly destroyRef = inject(DestroyRef);

  readonly productos = signal<Producto[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly total = signal(0);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.productoService
      .listarMisProductos({ page: 0, size: 50, sort: 'id', direction: 'asc' })
      .pipe(
        finalize(() => this.cargando.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (respuesta) => {
          this.productos.set(respuesta.content);
          this.total.set(respuesta.totalElements);
        },
        error: (error: Error) => this.error.set(error.message),
      });
  }

  async eliminar(producto: Producto): Promise<void> {
    const confirmado = await this.confirmDialog.confirmar({
      titulo: 'Eliminar producto',
      mensaje: `Vas a retirar "${producto.nombre}" de tu catalogo.`,
      detalle: 'El producto desaparecera del catalogo y de tus ventas futuras.',
      etiquetaConfirmar: 'Eliminar producto',
      etiquetaCancelar: 'Conservar',
      peligro: true,
    });

    if (!confirmado) {
      return;
    }

    this.productoService
      .eliminar(producto.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notification.exito('Producto eliminado', `${producto.nombre} fue retirado.`);
          this.cargar();
        },
        error: (error: Error) => {
          this.error.set(error.message);
          this.notification.error('No se pudo eliminar el producto', error.message);
        },
      });
  }

  estadoStock(producto: Producto): string {
    if (producto.stock <= 0) {
      return 'Agotado';
    }
    return producto.stock <= 5 ? `Ultimas ${producto.stock}` : `${producto.stock} unidades`;
  }
}
