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
import { Subject, debounceTime, distinctUntilChanged, finalize } from 'rxjs';
import { Producto } from '../../../core/models/producto.model';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { ConfirmDialogService } from '../../../core/services/confirm-dialog.service';
import { HistorialBusquedaService } from '../../../core/services/historial-busqueda.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ProductoService } from '../../../core/services/producto.service';

@Component({
  selector: 'app-producto-list',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './producto-list.component.html',
  styleUrl: './producto-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductoListComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly auth = inject(AuthService);
  private readonly cart = inject(CartService);
  private readonly notification = inject(NotificationService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly historial = inject(HistorialBusquedaService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly busqueda$ = new Subject<string>();

  readonly esAdmin = this.auth.esAdmin;
  readonly busquedasRecientes = this.historial.terminos;

  readonly productos = signal<Producto[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal(0);
  readonly size = signal(12);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly first = signal(true);
  readonly last = signal(true);
  readonly sort = signal('id');
  readonly direction = signal<'asc' | 'desc'>('asc');
  readonly busqueda = signal('');

  ngOnInit(): void {
    this.busqueda$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((texto) => {
        if (texto === this.busqueda()) {
          return;
        }
        this.busqueda.set(texto);
        this.page.set(0);
        this.cargar();
      });

    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);

    this.productoService
      .listar({
        page: this.page(),
        size: this.size(),
        sort: this.sort(),
        direction: this.direction(),
        nombre: this.busqueda(),
      })
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (respuesta) => {
          this.productos.set(respuesta.content);
          this.totalElements.set(respuesta.totalElements);
          this.totalPages.set(respuesta.totalPages);
          this.first.set(respuesta.first);
          this.last.set(respuesta.last);
        },
        error: (error: Error) => {
          this.productos.set([]);
          this.error.set(error.message);
        },
      });
  }

  onBusqueda(texto: string): void {
    this.busqueda$.next(texto);
  }

  recordarBusqueda(texto: string): void {
    this.historial.registrar(texto);
  }

  usarBusqueda(termino: string): void {
    this.busqueda$.next(termino);
    this.busqueda.set(termino);
    this.historial.registrar(termino);
    this.page.set(0);
    this.cargar();
  }

  olvidarBusqueda(termino: string, event: Event): void {
    event.stopPropagation();
    this.historial.quitar(termino);
  }

  limpiarHistorial(): void {
    if (this.busquedasRecientes().length === 0) {
      return;
    }
    this.historial.limpiar();
    this.notification.info('Historial limpio', 'Ya no veras busquedas recientes en el catalogo.');
  }

  cambiarTamano(valor: string): void {
    this.size.set(Number(valor));
    this.page.set(0);
    this.cargar();
  }

  ordenar(campo: string): void {
    if (this.sort() === campo) {
      this.direction.set(this.direction() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sort.set(campo);
      this.direction.set('asc');
    }
    this.page.set(0);
    this.cargar();
  }

  indicador(campo: string): string {
    if (this.sort() !== campo) {
      return '';
    }
    return this.direction() === 'asc' ? '↑' : '↓';
  }

  cambiarPagina(delta: number): void {
    this.page.update((page) => page + delta);
    this.cargar();
  }

  agregar(producto: Producto): void {
    if (producto.stock <= 0) {
      this.notification.error(
        'Producto agotado',
        'Este producto no tiene unidades disponibles por ahora.',
      );
      return;
    }

    if (this.cart.agregar(producto)) {
      this.notification.exito('Agregado al carrito', `${producto.nombre} te espera en tu pedido.`);
      return;
    }

    this.notification.error(
      'Limite alcanzado',
      `No puedes agregar mas unidades de ${producto.nombre}.`,
    );
  }

  async eliminar(producto: Producto): Promise<void> {
    const confirmado = await this.confirmDialog.confirmar({
      titulo: 'Eliminar producto',
      mensaje: `Vas a eliminar "${producto.nombre}" del catalogo.`,
      detalle: 'Esta accion no se puede deshacer y el producto dejara de estar disponible.',
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
          if (this.productos().length === 1 && this.page() > 0) {
            this.page.update((page) => page - 1);
          }
          this.notification.exito(
            'Producto eliminado',
            `"${producto.nombre}" ya no esta disponible en el catalogo.`,
          );
          this.cargar();
        },
        error: (error: Error) => {
          this.error.set(error.message);
          this.notification.error('No se pudo eliminar el producto', error.message);
        },
      });
  }

  imagen(producto: Producto): string {
    return producto.imagenUrl?.trim() || `/img/productos/${this.categoriaSlug(producto)}.svg`;
  }

  imagenFallback(event: Event): void {
    const imagen = event.target as HTMLImageElement;
    if (!imagen.src.endsWith('/img/productos/general.svg')) {
      imagen.src = '/img/productos/general.svg';
    }
  }

  categoria(producto: Producto): string {
    return producto.categoria ?? 'General';
  }

  categoriaSlug(producto: Producto): string {
    return this.categoria(producto)
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-');
  }

  stockTexto(producto: Producto): string {
    if (producto.stock <= 0) {
      return 'Agotado';
    }
    if (producto.stock <= 5) {
      return `Ultimas ${producto.stock} unidades`;
    }
    return `${producto.stock} disponibles`;
  }
}
