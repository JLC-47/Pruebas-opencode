import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ItemCarrito, CartService } from '../../core/services/cart.service';

@Component({
  selector: 'app-carrito',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CarritoComponent {
  readonly cart = inject(CartService);
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

  vaciar(): void {
    if (this.cart.vacio()) {
      return;
    }
    if (window.confirm('Vaciar el carrito de compras?')) {
      this.cart.limpiar();
    }
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
        next: (compra) =>
          this.router.navigate(['/mis-compras'], {
            queryParams: { compra: compra.id },
          }),
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
