import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  Compra,
  CompraItem,
  EstadoCompra,
  ItemDevolucionRequest,
} from '../../core/models/compra.model';
import { CompraService } from '../../core/services/compra.service';

@Component({
  selector: 'app-mis-compras',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './mis-compras.component.html',
  styleUrl: './mis-compras.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MisComprasComponent implements OnInit {
  private readonly compraService = inject(CompraService);
  private readonly route = inject(ActivatedRoute);

  readonly compras = signal<Compra[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);
  readonly compraConfirmada = signal<number | null>(null);
  readonly devolviendo = signal<number | null>(null);

  ngOnInit(): void {
    const compra = Number(this.route.snapshot.queryParamMap.get('compra'));
    this.compraConfirmada.set(Number.isFinite(compra) && compra > 0 ? compra : null);
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.compraService
      .listar()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (compras) => this.compras.set(compras),
        error: (error: Error) => this.error.set(error.message),
      });
  }

  unidades(items: CompraItem[]): number {
    return items.reduce((total, item) => total + item.cantidad, 0);
  }

  unidadesDevueltas(items: CompraItem[]): number {
    return items.reduce(
      (total, item) => total + (item.cantidadDevuelta ?? 0),
      0,
    );
  }

  pendientes(item: CompraItem): number {
    return Math.max(0, item.cantidad - (item.cantidadDevuelta ?? 0));
  }

  puedeDevolver(compra: Compra): boolean {
    return compra.items.some((item) => this.pendientes(item) > 0);
  }

  estadoTexto(estado: EstadoCompra): string {
    switch (estado) {
      case 'DEVUELTA_TOTAL':
        return 'Devuelta por completo';
      case 'DEVUELTA_PARCIAL':
        return 'Devolucion parcial';
      default:
        return 'Completada';
    }
  }

  devolverItem(compra: Compra, item: CompraItem): void {
    const cantidad = this.pendientes(item);
    if (cantidad <= 0) {
      return;
    }

    if (
      !window.confirm(
        `Devolver ${cantidad} unidad(es) de "${item.productoNombre}"? El inventario se actualizara.`,
      )
    ) {
      return;
    }

    this.enviarDevolucion(compra.id, [
      { productoId: item.productoId, cantidad },
    ]);
  }

  devolverTodo(compra: Compra): void {
    const items: ItemDevolucionRequest[] = compra.items
      .filter((item) => this.pendientes(item) > 0)
      .map((item) => ({
        productoId: item.productoId,
        cantidad: this.pendientes(item),
      }));

    if (items.length === 0) {
      return;
    }

    if (
      !window.confirm(
        `Devolver el pedido #${compra.id} completo? El inventario se actualizara.`,
      )
    ) {
      return;
    }

    this.enviarDevolucion(compra.id, items);
  }

  private enviarDevolucion(
    compraId: number,
    items: ItemDevolucionRequest[],
  ): void {
    if (this.devolviendo() !== null) {
      return;
    }

    this.devolviendo.set(compraId);
    this.error.set(null);
    this.mensaje.set(null);

    this.compraService
      .devolver(compraId, { items })
      .pipe(finalize(() => this.devolviendo.set(null)))
      .subscribe({
        next: (actualizada) => {
          this.compras.update((compras) =>
            compras.map((compra) =>
              compra.id === actualizada.id ? actualizada : compra,
            ),
          );
          this.mensaje.set(
            `Devolucion del pedido #${actualizada.id} registrada. El inventario fue repuesto.`,
          );
        },
        error: (error: Error) => this.error.set(error.message),
      });
  }
}
