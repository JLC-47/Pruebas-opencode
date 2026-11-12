import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap, throwError } from 'rxjs';
import { Compra, CompraRequest } from '../models/compra.model';
import { Producto } from '../models/producto.model';

const STORAGE_KEY = 'productos.cart';

export interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly http = inject(HttpClient);
  private readonly itemsSignal = signal<ItemCarrito[]>(this.leer());

  readonly items = this.itemsSignal.asReadonly();
  readonly cantidadTotal = computed(() =>
    this.itemsSignal().reduce((total, item) => total + item.cantidad, 0),
  );
  readonly total = computed(() =>
    this.itemsSignal().reduce(
      (total, item) => total + item.producto.precio * item.cantidad,
      0,
    ),
  );
  readonly vacio = computed(() => this.itemsSignal().length === 0);

  agregar(producto: Producto, cantidad = 1): boolean {
    if (producto.stock <= 0 || cantidad <= 0) {
      return false;
    }

    const existente = this.itemsSignal().find((item) => item.producto.id === producto.id);
    if (existente) {
      if (existente.cantidad >= producto.stock) {
        return false;
      }
      this.actualizar(
        existente.producto.id,
        Math.min(existente.cantidad + cantidad, producto.stock),
      );
      return true;
    }

    this.itemsSignal.update((items) => [
      ...items,
      { producto, cantidad: Math.min(cantidad, producto.stock) },
    ]);
    this.guardar();
    return true;
  }

  cambiarCantidad(productoId: number, cantidad: number): void {
    const item = this.itemsSignal().find((actual) => actual.producto.id === productoId);
    if (!item) {
      return;
    }
    const cantidadValida = Math.min(Math.max(cantidad, 1), item.producto.stock);
    this.actualizar(productoId, cantidadValida);
  }

  quitar(productoId: number): void {
    this.itemsSignal.update((items) =>
      items.filter((item) => item.producto.id !== productoId),
    );
    this.guardar();
  }

  limpiar(): void {
    this.itemsSignal.set([]);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  comprar(): Observable<Compra> {
    if (this.vacio()) {
      return throwError(() => new Error('El carrito esta vacio'));
    }

    const request: CompraRequest = {
      items: this.itemsSignal().map((item) => ({
        productoId: item.producto.id,
        cantidad: item.cantidad,
      })),
    };

    return this.http
      .post<Compra>('/api/compras', request)
      .pipe(tap(() => this.limpiar()));
  }

  private actualizar(productoId: number, cantidad: number): void {
    this.itemsSignal.update((items) =>
      items.map((item) =>
        item.producto.id === productoId ? { ...item, cantidad } : item,
      ),
    );
    this.guardar();
  }

  private guardar(): void {
    if (typeof localStorage === 'undefined') {
      return;
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.itemsSignal()));
  }

  private leer(): ItemCarrito[] {
    if (typeof localStorage === 'undefined') {
      return [];
    }

    try {
      const valor = localStorage.getItem(STORAGE_KEY);
      if (!valor) {
        return [];
      }
      const items = JSON.parse(valor) as ItemCarrito[];
      if (!Array.isArray(items)) {
        localStorage.removeItem(STORAGE_KEY);
        return [];
      }
      return items.filter(
        (item) =>
          item?.producto?.id !== undefined &&
          item?.producto?.precio !== undefined &&
          Number.isFinite(item.cantidad) &&
          item.cantidad > 0,
      );
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return [];
    }
  }
}
