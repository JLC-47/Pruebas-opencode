import { TestBed } from '@angular/core/testing';
import {
  ActivatedRoute,
  Router,
  convertToParamMap,
  provideRouter,
} from '@angular/router';
import { of } from 'rxjs';
import { Producto } from '../../../core/models/producto.model';
import { ProductoService } from '../../../core/services/producto.service';
import { ProductoFormComponent } from './producto-form.component';

const producto: Producto = {
  id: 5,
  nombre: 'Laptop',
  precio: 2500000,
  stock: 10,
  categoria: 'Tecnologia',
  descripcion: 'Laptop de alto rendimiento',
};

async function configurar(id?: string) {
  const servicioMock = {
    obtener: vi.fn(() => of(producto)),
    crear: vi.fn(() => of(producto)),
    actualizar: vi.fn(() => of(producto)),
  };

  await TestBed.configureTestingModule({
    imports: [ProductoFormComponent],
    providers: [
      provideRouter([]),
      { provide: ProductoService, useValue: servicioMock },
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { paramMap: convertToParamMap(id ? { id } : {}) } },
      },
    ],
  }).compileComponents();

  const fixture = TestBed.createComponent(ProductoFormComponent);
  return {
    fixture,
    componente: fixture.componentInstance,
    servicioMock,
    router: TestBed.inject(Router),
  };
}

describe('ProductoFormComponent', () => {
  it('no envia el formulario cuando es invalido', async () => {
    const { fixture, componente, servicioMock } = await configurar();
    fixture.detectChanges();

    componente.form.setValue({
      nombre: '',
      precio: -5,
      stock: 1.5,
      categoria: '',
      descripcion: '',
    });
    componente.guardar();
    fixture.detectChanges();

    expect(componente.form.invalid).toBe(true);
    expect(servicioMock.crear).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'El nombre es obligatorio',
    );
  });

  it('crea un producto valido y navega al listado', async () => {
    const { fixture, componente, servicioMock, router } = await configurar();
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    componente.form.setValue({
      nombre: 'Laptop',
      precio: 2500000,
      stock: 10,
      categoria: 'Tecnologia',
      descripcion: 'Laptop de alto rendimiento',
    });
    componente.guardar();

    expect(servicioMock.crear).toHaveBeenCalledWith({
      nombre: 'Laptop',
      precio: 2500000,
      stock: 10,
      categoria: 'Tecnologia',
      descripcion: 'Laptop de alto rendimiento',
    });
    expect(navegar).toHaveBeenCalledWith(['/productos']);
  });

  it('carga el producto cuando esta en modo edicion', async () => {
    const { fixture, componente, servicioMock } = await configurar('5');
    fixture.detectChanges();

    expect(componente.editando()).toBe(true);
    expect(servicioMock.obtener).toHaveBeenCalledWith(5);
    expect(componente.form.getRawValue()).toEqual({
      nombre: 'Laptop',
      precio: 2500000,
      stock: 10,
      categoria: 'Tecnologia',
      descripcion: 'Laptop de alto rendimiento',
    });
  });

  it('actualiza el producto en modo edicion', async () => {
    const { fixture, componente, servicioMock, router } = await configurar('5');
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    componente.form.setValue({
      nombre: 'Laptop Gamer',
      precio: 2300000,
      stock: 8,
      categoria: 'Tecnologia',
      descripcion: 'Version gamer',
    });
    componente.guardar();

    expect(servicioMock.actualizar).toHaveBeenCalledWith(5, {
      nombre: 'Laptop Gamer',
      precio: 2300000,
      stock: 8,
      categoria: 'Tecnologia',
      descripcion: 'Version gamer',
    });
    expect(navegar).toHaveBeenCalledWith(['/productos']);
  });
});
