import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { PageResponse, Producto } from '../../../core/models/producto.model';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { ConfirmDialogService } from '../../../core/services/confirm-dialog.service';
import { HistorialBusquedaService } from '../../../core/services/historial-busqueda.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ProductoService } from '../../../core/services/producto.service';
import { ProductoListComponent } from './producto-list.component';

const pagina: PageResponse<Producto> = {
  content: [
    {
      id: 1,
      nombre: 'Laptop',
      precio: 2500000,
      stock: 10,
      categoria: 'Tecnologia',
      descripcion: 'Laptop de alto rendimiento',
    },
    {
      id: 2,
      nombre: 'Mouse',
      precio: 45000,
      stock: 50,
      categoria: 'Accesorios',
      descripcion: 'Mouse inalambrico',
    },
  ],
  page: 0,
  size: 12,
  totalElements: 2,
  totalPages: 1,
  first: true,
  last: true,
};

const servicioMock = {
  listar: vi.fn(),
  eliminar: vi.fn(),
};

const authMock = {
  esAdmin: signal(true),
};

const cartMock = {
  agregar: vi.fn(),
};

const notificationMock = {
  exito: vi.fn(),
  error: vi.fn(),
  info: vi.fn(),
};

const confirmDialogMock = {
  confirmar: vi.fn(() => Promise.resolve(true)),
};

const historialMock = {
  terminos: signal<string[]>([]),
  registrar: vi.fn(),
  quitar: vi.fn(),
  limpiar: vi.fn(),
};

describe('ProductoListComponent', () => {
  beforeEach(async () => {
    servicioMock.listar.mockReset().mockReturnValue(of(pagina));
    servicioMock.eliminar.mockReset().mockReturnValue(of(void 0));
    cartMock.agregar.mockReset().mockReturnValue(true);
    notificationMock.exito.mockReset();
    notificationMock.error.mockReset();
    notificationMock.info.mockReset();
    confirmDialogMock.confirmar.mockReset().mockResolvedValue(true);
    historialMock.terminos.set([]);
    historialMock.registrar.mockReset();
    historialMock.quitar.mockReset();
    historialMock.limpiar.mockReset();
    authMock.esAdmin.set(true);

    await TestBed.configureTestingModule({
      imports: [ProductoListComponent],
      providers: [
        provideRouter([]),
        { provide: ProductoService, useValue: servicioMock },
        { provide: AuthService, useValue: authMock },
        { provide: CartService, useValue: cartMock },
        { provide: NotificationService, useValue: notificationMock },
        { provide: ConfirmDialogService, useValue: confirmDialogMock },
        { provide: HistorialBusquedaService, useValue: historialMock },
      ],
    }).compileComponents();
  });

  it('carga y muestra los productos con la paginacion', () => {
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(servicioMock.listar).toHaveBeenCalledWith({
      page: 0,
      size: 12,
      sort: 'id',
      direction: 'asc',
      nombre: '',
    });
    expect(texto).toContain('Laptop');
    expect(texto).toContain('Mouse');
    expect(texto).toContain('2 producto(s) registrados');
    expect(texto).toContain('Pagina 1 de 1');
    expect(texto).toContain('Nuevo producto');
    expect(texto).toContain('Editar');
    expect(texto).toContain('Agregar al carrito');
  });

  it('oculta las acciones de escritura para usuarios sin rol admin', () => {
    authMock.esAdmin.set(false);

    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).not.toContain('Nuevo producto');
    expect(texto).not.toContain('Editar');
    expect(texto).not.toContain('Eliminar');
  });

  it('muestra el error cuando falla la carga', () => {
    servicioMock.listar.mockReturnValue(throwError(() => new Error('Fallo de red')));

    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Fallo de red');
  });

  it('cambia de pagina y vuelve a consultar', () => {
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    fixture.componentInstance.cambiarPagina(1);

    expect(servicioMock.listar).toHaveBeenLastCalledWith({
      page: 1,
      size: 12,
      sort: 'id',
      direction: 'asc',
      nombre: '',
    });
  });

  it('ordena por un campo y alterna la direccion', () => {
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    fixture.componentInstance.ordenar('precio');
    expect(servicioMock.listar).toHaveBeenLastCalledWith({
      page: 0,
      size: 12,
      sort: 'precio',
      direction: 'asc',
      nombre: '',
    });

    fixture.componentInstance.ordenar('precio');
    expect(servicioMock.listar).toHaveBeenLastCalledWith({
      page: 0,
      size: 12,
      sort: 'precio',
      direction: 'desc',
      nombre: '',
    });
  });

  it('agrega un producto al carrito y muestra una notificacion', () => {
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    fixture.componentInstance.agregar(pagina.content[0]);
    fixture.detectChanges();

    expect(cartMock.agregar).toHaveBeenCalledWith(pagina.content[0]);
    expect(notificationMock.exito).toHaveBeenCalledWith(
      'Agregado al carrito',
      expect.stringContaining('Laptop'),
    );
  });

  it('notifica cuando el producto esta agotado', () => {
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    fixture.componentInstance.agregar({ ...pagina.content[0], stock: 0 });

    expect(cartMock.agregar).not.toHaveBeenCalled();
    expect(notificationMock.error).toHaveBeenCalledWith(
      'Producto agotado',
      expect.stringContaining('no tiene unidades'),
    );
  });

  it('elimina un producto cuando se confirma el dialogo', async () => {
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    await fixture.componentInstance.eliminar(pagina.content[0]);

    expect(confirmDialogMock.confirmar).toHaveBeenCalledWith(
      expect.objectContaining({
        titulo: 'Eliminar producto',
        etiquetaConfirmar: 'Eliminar producto',
        peligro: true,
      }),
    );
    expect(servicioMock.eliminar).toHaveBeenCalledWith(1);
    expect(notificationMock.exito).toHaveBeenCalledWith(
      'Producto eliminado',
      expect.stringContaining('Laptop'),
    );
  });

  it('conserva el producto cuando se cancela el dialogo', async () => {
    confirmDialogMock.confirmar.mockResolvedValue(false);
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    await fixture.componentInstance.eliminar(pagina.content[0]);

    expect(servicioMock.eliminar).not.toHaveBeenCalled();
    expect(notificationMock.exito).not.toHaveBeenCalled();
  });

  it('muestra las busquedas recientes y permite reutilizarlas', () => {
    historialMock.terminos.set(['Laptop', 'Mouse']);
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    const elemento = fixture.nativeElement as HTMLElement;
    expect(elemento.textContent).toContain('Recientes');
    const terminos = elemento.querySelectorAll<HTMLButtonElement>('.history-term');
    expect(terminos).toHaveLength(2);

    terminos[0].click();

    expect(historialMock.registrar).toHaveBeenCalledWith('Laptop');
    expect(servicioMock.listar).toHaveBeenLastCalledWith(
      expect.objectContaining({ nombre: 'Laptop', page: 0 }),
    );
  });

  it('guarda la busqueda cuando se confirma el campo', () => {
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    const buscador = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>(
      '.search',
    ) as HTMLInputElement;
    buscador.value = 'Laptop';
    buscador.dispatchEvent(new Event('change'));

    expect(historialMock.registrar).toHaveBeenCalledWith('Laptop');
  });

  it('limpia el historial y notifica al usuario', () => {
    historialMock.terminos.set(['Laptop']);
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>('.history-clear')
      ?.click();

    expect(historialMock.limpiar).toHaveBeenCalled();
    expect(notificationMock.info).toHaveBeenCalledWith(
      'Historial limpio',
      expect.stringContaining('busquedas recientes'),
    );
  });
});
