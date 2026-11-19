import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { VendedorService } from './vendedor.service';

describe('VendedorService', () => {
  let service: VendedorService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(VendedorService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('obtiene el resumen de ventas', () => {
    service.obtenerVentas().subscribe();

    const peticion = http.expectOne('/api/vendedores/ventas');
    expect(peticion.request.method).toBe('GET');
    peticion.flush({ totalVendido: 0, unidadesVendidas: 0, pedidos: 0, ventas: [] });
  });
});
