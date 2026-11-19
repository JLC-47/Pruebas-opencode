import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ResumenVentas } from '../../core/models/vendedor.model';
import { VendedorService } from '../../core/services/vendedor.service';
import { VendedorVentasComponent } from './vendedor-ventas.component';

const resumen: ResumenVentas = {
  totalVendido: 52000,
  unidadesVendidas: 2,
  pedidos: 1,
  ventas: [
    {
      compraId: 12,
      fecha: '2026-11-19T10:00:00Z',
      comprador: 'cliente',
      producto: 'Laptop gamer',
      cantidad: 2,
      subtotal: 52000,
    },
  ],
};

describe('VendedorVentasComponent', () => {
  async function configurar() {
    const vendedorMock = { obtenerVentas: vi.fn(() => of(resumen)) };

    await TestBed.configureTestingModule({
      imports: [VendedorVentasComponent],
      providers: [provideRouter([]), { provide: VendedorService, useValue: vendedorMock }],
    }).compileComponents();

    const fixture = TestBed.createComponent(VendedorVentasComponent);
    return { fixture, componente: fixture.componentInstance, vendedorMock };
  }

  it('muestra el resumen de ventas del vendedor', async () => {
    const { fixture, componente, vendedorMock } = await configurar();
    fixture.detectChanges();

    expect(vendedorMock.obtenerVentas).toHaveBeenCalled();
    expect(componente.resumen()).toEqual(resumen);
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Laptop gamer');
    expect(texto).toContain('Unidades');
  });

  it('muestra el error cuando falla la consulta', async () => {
    const { fixture, componente, vendedorMock } = await configurar();
    vendedorMock.obtenerVentas.mockReturnValue(throwError(() => new Error('Sin acceso')));
    fixture.detectChanges();

    expect(componente.error()).toBe('Sin acceso');
  });
});
