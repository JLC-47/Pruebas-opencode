import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { Compra } from '../../core/models/compra.model';
import { CartService, ItemCarrito } from '../../core/services/cart.service';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { NotificationService } from '../../core/services/notification.service';
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
    limpiar: vi.fn(),
    comprar: vi.fn(() => of(compra)),
  };
}

async function configurar(cartMock = crearCartMock()) {
  const confirmDialogMock = {
    confirmar: vi.fn(() => Promise.resolve(true)),
  };
  const notificationMock = {
    exito: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
  };

  await TestBed.configureTestingModule({
    imports: [CarritoComponent],
    providers: [
      provideRouter([]),
      { provide: CartService, useValue: cartMock },
      { provide: ConfirmDialogService, useValue: confirmDialogMock },
      { provide: NotificationService, useValue: notificationMock },
    ],
  }).compileComponents();

  return {
    fixture: TestBed.createComponent(CarritoComponent),
    cartMock,
    confirmDialogMock,
    notificationMock,
    router: TestBed.inject(Router),
  };
}

describe('CarritoComponent', () => {
  it('muestra los productos y permite ajustar cantidades', async () => {
    const { fixture, cartMock } = await configurar();
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

  it('finaliza la compra, notifica y navega al historial', async () => {
    const { fixture, cartMock, notificationMock, router } = await configurar();
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    fixture.componentInstance.comprar();

    expect(cartMock.comprar).toHaveBeenCalled();
    expect(notificationMock.exito).toHaveBeenCalledWith(
      'Compra confirmada',
      expect.stringContaining('#7'),
    );
    expect(navegar).toHaveBeenCalledWith(['/mis-compras'], {
      queryParams: { compra: 7 },
    });
  });

  it('muestra el error cuando el checkout falla', async () => {
    const cartMock = crearCartMock();
    cartMock.comprar.mockReturnValue(throwError(() => new Error('Stock insuficiente')));
    const { fixture } = await configurar(cartMock);
    fixture.detectChanges();

    fixture.componentInstance.comprar();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Stock insuficiente');
  });

  it('muestra el estado vacio', async () => {
    const cartMock = crearCartMock();
    cartMock.vacio.set(true);
    const { fixture } = await configurar(cartMock);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Tu carrito esta vacio');
  });

  it('vacia el carrito cuando se confirma el dialogo', async () => {
    const { fixture, cartMock, confirmDialogMock, notificationMock } = await configurar();
    fixture.detectChanges();

    await fixture.componentInstance.vaciar();

    expect(confirmDialogMock.confirmar).toHaveBeenCalledWith(
      expect.objectContaining({
        titulo: 'Vaciar carrito',
        etiquetaConfirmar: 'Vaciar carrito',
        peligro: true,
      }),
    );
    expect(cartMock.limpiar).toHaveBeenCalled();
    expect(notificationMock.info).toHaveBeenCalledWith(
      'Carrito vaciado',
      expect.stringContaining('se limpio'),
    );
  });

  it('mantiene el carrito cuando se cancela el dialogo', async () => {
    const { fixture, cartMock, confirmDialogMock, notificationMock } = await configurar();
    confirmDialogMock.confirmar.mockResolvedValue(false);
    fixture.detectChanges();

    await fixture.componentInstance.vaciar();

    expect(cartMock.limpiar).not.toHaveBeenCalled();
    expect(notificationMock.info).not.toHaveBeenCalled();
  });
});
