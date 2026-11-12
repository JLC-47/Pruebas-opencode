import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PageResponse, Producto } from '../models/producto.model';
import { ProductoService } from './producto.service';

describe('ProductoService', () => {
  let service: ProductoService;
  let http: HttpTestingController;

  const producto: Producto = {
    id: 1,
    nombre: 'Laptop',
    precio: 2500000,
    stock: 10,
  };

  const paginaVacia: PageResponse<Producto> = {
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ProductoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista productos con paginacion, orden y busqueda', () => {
    let resultado: PageResponse<Producto> | undefined;

    service
      .listar({
        page: 1,
        size: 5,
        sort: 'nombre',
        direction: 'desc',
        nombre: '  lap  ',
      })
      .subscribe((respuesta) => (resultado = respuesta));

    const peticion = http.expectOne((request) => request.url === '/api/productos');
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.get('page')).toBe('1');
    expect(peticion.request.params.get('size')).toBe('5');
    expect(peticion.request.params.get('sort')).toBe('nombre');
    expect(peticion.request.params.get('direction')).toBe('desc');
    expect(peticion.request.params.get('nombre')).toBe('lap');

    peticion.flush({ ...paginaVacia, page: 1, size: 5 });

    expect(resultado?.page).toBe(1);
  });

  it('no envia el parametro nombre cuando esta vacio', () => {
    service
      .listar({ page: 0, size: 10, sort: 'id', direction: 'asc', nombre: '   ' })
      .subscribe();

    const peticion = http.expectOne((request) => request.url === '/api/productos');
    expect(peticion.request.params.has('nombre')).toBe(false);

    peticion.flush(paginaVacia);
  });

  it('obtiene un producto por id', () => {
    let resultado: Producto | undefined;

    service.obtener(7).subscribe((respuesta) => (resultado = respuesta));

    const peticion = http.expectOne('/api/productos/7');
    expect(peticion.request.method).toBe('GET');
    peticion.flush(producto);

    expect(resultado?.nombre).toBe('Laptop');
  });

  it('crea un producto', () => {
    const request = { nombre: 'Laptop', precio: 2500000, stock: 10 };

    service.crear(request).subscribe();

    const peticion = http.expectOne((request) => request.url === '/api/productos');
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(request);
    peticion.flush(producto);
  });

  it('actualiza un producto', () => {
    const request = { nombre: 'Laptop Gamer', precio: 2300000, stock: 8 };

    service.actualizar(1, request).subscribe();

    const peticion = http.expectOne('/api/productos/1');
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual(request);
    peticion.flush({ ...producto, ...request });
  });

  it('elimina un producto', () => {
    service.eliminar(1).subscribe();

    const peticion = http.expectOne('/api/productos/1');
    expect(peticion.request.method).toBe('DELETE');
    peticion.flush(null);
  });
});

