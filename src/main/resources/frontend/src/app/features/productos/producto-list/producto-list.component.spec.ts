import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { PageResponse, Producto } from '../../../core/models/producto.model';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
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

describe('ProductoListComponent', () => {
  beforeEach(async () => {
    servicioMock.listar.mockReset().mockReturnValue(of(pagina));
    servicioMock.eliminar.mockReset().mockReturnValue(of(void 0));
    cartMock.agregar.mockReset().mockReturnValue(true);
    authMock.esAdmin.set(true);

    await TestBed.configureTestingModule({
      imports: [ProductoListComponent],
      providers: [
        provideRouter([]),
        { provide: ProductoService, useValue: servicioMock },
        { provide: AuthService, useValue: authMock },
        { provide: CartService, useValue: cartMock },
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

  it('agrega un producto al carrito y muestra confirmacion', () => {
    const fixture = TestBed.createComponent(ProductoListComponent);
    fixture.detectChanges();

    fixture.componentInstance.agregar(pagina.content[0]);
    fixture.detectChanges();

    expect(cartMock.agregar).toHaveBeenCalledWith(pagina.content[0]);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'se agrego al carrito',
    );
  });
});
