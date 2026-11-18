import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ItemCarrito, CartService } from '../../core/services/cart.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-carrito',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CarritoComponent {
  readonly cart = inject(CartService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);

  readonly enviando = signal(false);
  readonly error = signal<string | null>(null);

  incrementar(item: ItemCarrito): void {
    this.cart.cambiarCantidad(item.producto.id, item.cantidad + 1);
  }

  decrementar(item: ItemCarrito): void {
    this.cart.cambiarCantidad(item.producto.id, item.cantidad - 1);
  }

  quitar(item: ItemCarrito): void {
    this.cart.quitar(item.producto.id);
  }

  async vaciar(): Promise<void> {
    if (this.cart.vacio()) {
      return;
    }

    const confirmado = await this.confirmDialog.confirmar({
      titulo: 'Vaciar carrito',
      mensaje: 'Quitaremos todos los productos de tu seleccion.',
      detalle: 'Esta accion no afecta tu historial de compras.',
      etiquetaConfirmar: 'Vaciar carrito',
      etiquetaCancelar: 'Mantener productos',
      peligro: true,
    });

    if (!confirmado) {
      return;
    }

    this.cart.limpiar();
    this.notification.info(
      'Carrito vaciado',
      'Tu seleccion se limpio. Puedes seguir explorando el catalogo.',
    );
  }

  comprar(): void {
    if (this.cart.vacio() || this.enviando()) {
      return;
    }

    this.enviando.set(true);
    this.error.set(null);

    this.cart
      .comprar()
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (compra) => {
          this.notification.exito(
            'Compra confirmada',
            `Tu pedido #${compra.id} se registro correctamente.`,
          );
          this.router.navigate(['/mis-compras'], {
            queryParams: { compra: compra.id },
          });
        },
        error: (error: Error) => this.error.set(error.message),
      });
  }

  inicial(item: ItemCarrito): string {
    return item.producto.nombre.charAt(0).toUpperCase();
  }

  categoria(item: ItemCarrito): string {
    return item.producto.categoria ?? 'General';
  }
}
