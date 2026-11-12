import { TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter,
} from '@angular/router';
import { of, throwError } from 'rxjs';
import { Compra } from '../../core/models/compra.model';
import { CompraService } from '../../core/services/compra.service';
import { MisComprasComponent } from './mis-compras.component';

const compra: Compra = {
  id: 15,
  fecha: '2026-11-12T10:30:00Z',
  total: 5045000,
  items: [
    {
      productoId: 1,
      productoNombre: 'Laptop',
      cantidad: 2,
      precioUnitario: 2500000,
      subtotal: 5000000,
    },
    {
      productoId: 2,
      productoNombre: 'Mouse',
      cantidad: 1,
      precioUnitario: 45000,
      subtotal: 45000,
    },
  ],
};

async function configurar(opciones: {
  compras?: Compra[];
  error?: Error;
  compraId?: string;
} = {}) {
  const compraMock = {
    listar: vi.fn(() =>
      opciones.error ? throwError(() => opciones.error) : of(opciones.compras ?? []),
    ),
  };

  await TestBed.configureTestingModule({
    imports: [MisComprasComponent],
    providers: [
      provideRouter([]),
      { provide: CompraService, useValue: compraMock },
      {
        provide: ActivatedRoute,
        useValue: {
          snapshot: {
            queryParamMap: convertToParamMap(
              opciones.compraId ? { compra: opciones.compraId } : {},
            ),
          },
        },
      },
    ],
  }).compileComponents();

  const fixture = TestBed.createComponent(MisComprasComponent);
  return { fixture, componente: fixture.componentInstance, compraMock };
}

describe('MisComprasComponent', () => {
  it('muestra las compras y confirma el pedido reciente', async () => {
    const { fixture } = await configurar({ compras: [compra], compraId: '15' });
    fixture.detectChanges();

    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(texto).toContain('Compra #15 confirmada');
    expect(texto).toContain('Pedido #15');
    expect(texto).toContain('Laptop');
    expect(texto).toContain('3 unidad(es)');
  });

  it('muestra el estado vacio', async () => {
    const { fixture } = await configurar({ compras: [] });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Aun no tienes compras registradas',
    );
  });

  it('muestra el error cuando falla la consulta', async () => {
    const { fixture } = await configurar({ error: new Error('Fallo de red') });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Fallo de red');
  });
});
