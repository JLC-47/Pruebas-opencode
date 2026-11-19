import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { PageResponse, Producto } from '../../core/models/producto.model';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { NotificationService } from '../../core/services/notification.service';
import { ProductoService } from '../../core/services/producto.service';
import { VendedorProductosComponent } from './vendedor-productos.component';

const producto: Producto = {
  id: 7,
  nombre: 'Laptop gamer',
  precio: 2500000,
  stock: 3,
  categoria: 'Tecnologia',
};

const pagina: PageResponse<Producto> = {
  content: [producto],
  page: 0,
  size: 50,
  totalElements: 1,
  totalPages: 1,
  first: true,
  last: true,
};

describe('VendedorProductosComponent', () => {
  let productoMock: {
    listarMisProductos: ReturnType<typeof vi.fn>;
    eliminar: ReturnType<typeof vi.fn>;
  };
  let notificationMock: Record<string, ReturnType<typeof vi.fn>>;
  let confirmMock: { confirmar: ReturnType<typeof vi.fn> };

  async function configurar() {
    productoMock = {
      listarMisProductos: vi.fn(() => of(pagina)),
      eliminar: vi.fn(() => of(void 0)),
    };
    notificationMock = { exito: vi.fn(), error: vi.fn() };
    confirmMock = { confirmar: vi.fn(() => Promise.resolve(true)) };

    await TestBed.configureTestingModule({
      imports: [VendedorProductosComponent],
      providers: [
        provideRouter([]),
        { provide: ProductoService, useValue: productoMock },
        { provide: NotificationService, useValue: notificationMock },
        { provide: ConfirmDialogService, useValue: confirmMock },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(VendedorProductosComponent);
    return { fixture, componente: fixture.componentInstance };
  }

  it('carga solo los productos del vendedor', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    expect(productoMock.listarMisProductos).toHaveBeenCalledWith({
      page: 0,
      size: 50,
      sort: 'id',
      direction: 'asc',
    });
    expect(componente.productos()).toEqual([producto]);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Laptop gamer');
  });

  it('elimina un producto propio tras confirmacion', async () => {
    const { fixture, componente } = await configurar();
    fixture.detectChanges();

    await componente.eliminar(producto);

    expect(confirmMock.confirmar).toHaveBeenCalled();
    expect(productoMock.eliminar).toHaveBeenCalledWith(7);
    expect(notificationMock['exito']).toHaveBeenCalled();
  });
});
