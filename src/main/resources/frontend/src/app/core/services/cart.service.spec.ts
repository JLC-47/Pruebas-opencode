import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Compra } from '../models/compra.model';
import { Producto } from '../models/producto.model';
import { CartService } from './cart.service';

const laptop: Producto = {
  id: 1,
  nombre: 'Laptop',
  precio: 2500000,
  stock: 3,
  categoria: 'Tecnologia',
};

const mouse: Producto = {
  id: 2,
  nombre: 'Mouse',
  precio: 45000,
  stock: 1,
};

const compra: Compra = {
  id: 9,
  fecha: '2026-11-12T10:00:00Z',
  total: 2500000,
  totalDevuelto: 0,
  estado: 'COMPLETADA',
  items: [
    {
      productoId: 1,
      productoNombre: 'Laptop',
      cantidad: 1,
      cantidadDevuelta: 0,
      precioUnitario: 2500000,
      subtotal: 2500000,
    },
  ],
};

describe('CartService', () => {
  let service: CartService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CartService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('agrega productos y calcula cantidades y total', () => {
    expect(service.agregar(laptop)).toBe(true);
    expect(service.agregar(laptop)).toBe(true);
    expect(service.agregar(mouse)).toBe(true);

    expect(service.cantidadTotal()).toBe(3);
    expect(service.total()).toBe(5045000);
    expect(service.vacio()).toBe(false);
  });

  it('no permite superar el stock disponible', () => {
    service.agregar(laptop);
    service.cambiarCantidad(laptop.id, 3);
    expect(service.agregar(laptop)).toBe(false);

    service.agregar(mouse);
    expect(service.agregar(mouse)).toBe(false);
    expect(service.items()[1].cantidad).toBe(1);
  });

  it('no agrega productos agotados', () => {
    expect(service.agregar({ ...laptop, stock: 0 })).toBe(false);
    expect(service.vacio()).toBe(true);
  });

  it('ajusta la cantidad dentro del stock y quita productos', () => {
    service.agregar(laptop);

    service.cambiarCantidad(laptop.id, 99);
    expect(service.items()[0].cantidad).toBe(3);

    service.cambiarCantidad(laptop.id, -4);
    expect(service.items()[0].cantidad).toBe(1);

    service.quitar(laptop.id);
    expect(service.vacio()).toBe(true);
  });

  it('persiste y limpia el carrito', () => {
    service.agregar(laptop);

    expect(localStorage.getItem('productos.cart')).toContain('Laptop');

    service.limpiar();

    expect(service.vacio()).toBe(true);
    expect(localStorage.getItem('productos.cart')).toBeNull();
    expect(service.cantidadTotal()).toBe(0);
  });

  it('registra la compra y vacia el carrito al completar', () => {
    service.agregar(laptop);
    let resultado: Compra | undefined;

    service.comprar().subscribe((respuesta) => (resultado = respuesta));

    const peticion = http.expectOne('/api/compras');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({
      items: [{ productoId: 1, cantidad: 1 }],
    });
    peticion.flush(compra);

    expect(resultado?.id).toBe(9);
    expect(service.vacio()).toBe(true);
  });

  it('rechaza la compra cuando el carrito esta vacio', () => {
    let mensaje = '';

    service.comprar().subscribe({
      error: (error: Error) => (mensaje = error.message),
    });

    expect(mensaje).toContain('vacio');
  });
});
