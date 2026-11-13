import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { Compra } from '../../core/models/compra.model';
import { CartService, ItemCarrito } from '../../core/services/cart.service';
import { CarritoComponent } from './carrito.component';

const items: ItemCarrito[] = [
  {
    producto: {
      id: 1,
      nombre: 'Laptop',
      precio: 2500000,
      stock: 3,
      categoria: 'Tecnologia',
    },
    cantidad: 2,
  },
];

const compra: Compra = {
  id: 7,
  fecha: '2026-11-12T10:00:00Z',
  total: 5000000,
  totalDevuelto: 0,
  estado: 'COMPLETADA',
  items: [],
};

function crearCartMock() {
  const itemsSignal = signal(items);
  return {
    items: itemsSignal.asReadonly(),
    cantidadTotal: signal(2),
    total: signal(5000000),
    vacio: signal(false),
    cambiarCantidad: vi.fn(),
    quitar: vi.fn(),
    comprar: vi.fn(() => of(compra)),
  };
}

describe('CarritoComponent', () => {
  it('muestra los productos y permite ajustar cantidades', async () => {
    const cartMock = crearCartMock();
    await TestBed.configureTestingModule({
      imports: [CarritoComponent],
      providers: [provideRouter([]), { provide: CartService, useValue: cartMock }],
    }).compileComponents();

    const fixture = TestBed.createComponent(CarritoComponent);
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Laptop');
    expect(texto).toContain('Tecnologia');
    expect(texto).toContain('Finalizar compra');

    fixture.componentInstance.incrementar(items[0]);
    expect(cartMock.cambiarCantidad).toHaveBeenCalledWith(1, 3);

    fixture.componentInstance.decrementar(items[0]);
    expect(cartMock.cambiarCantidad).toHaveBeenCalledWith(1, 1);

    fixture.componentInstance.quitar(items[0]);
    expect(cartMock.quitar).toHaveBeenCalledWith(1);
  });

  it('finaliza la compra y navega al historial', async () => {
    const cartMock = crearCartMock();
    await TestBed.configureTestingModule({
      imports: [CarritoComponent],
      providers: [provideRouter([]), { provide: CartService, useValue: cartMock }],
    }).compileComponents();

    const fixture = TestBed.createComponent(CarritoComponent);
    const router = TestBed.inject(Router);
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    fixture.componentInstance.comprar();

    expect(cartMock.comprar).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith(['/mis-compras'], {
      queryParams: { compra: 7 },
    });
  });

  it('muestra el error cuando el checkout falla', async () => {
    const cartMock = crearCartMock();
    cartMock.comprar.mockReturnValue(throwError(() => new Error('Stock insuficiente')));
    await TestBed.configureTestingModule({
      imports: [CarritoComponent],
      providers: [provideRouter([]), { provide: CartService, useValue: cartMock }],
    }).compileComponents();

    const fixture = TestBed.createComponent(CarritoComponent);
    fixture.detectChanges();

    fixture.componentInstance.comprar();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Stock insuficiente',
    );
  });

  it('muestra el estado vacio', async () => {
    const cartMock = crearCartMock();
    cartMock.vacio.set(true);
    await TestBed.configureTestingModule({
      imports: [CarritoComponent],
      providers: [provideRouter([]), { provide: CartService, useValue: cartMock }],
    }).compileComponents();

    const fixture = TestBed.createComponent(CarritoComponent);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Tu carrito esta vacio',
    );
  });
});
