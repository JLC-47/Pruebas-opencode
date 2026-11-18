import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Producto } from '../../../core/models/producto.model';
import { NotificationService } from '../../../core/services/notification.service';
import { ProductoService } from '../../../core/services/producto.service';
import { ProductoFormComponent } from './producto-form.component';

const producto: Producto = {
  id: 5,
  nombre: 'Laptop',
  precio: 2500000,
  stock: 10,
  categoria: 'Tecnologia',
  descripcion: 'Laptop de alto rendimiento',
  imagenUrl: '',
};

async function configurar(id?: string) {
  const servicioMock = {
    obtener: vi.fn(() => of(producto)),
    crear: vi.fn(() => of(producto)),
    actualizar: vi.fn(() => of(producto)),
  };
  const notificationMock = {
    exito: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
  };

  await TestBed.configureTestingModule({
    imports: [ProductoFormComponent],
    providers: [
      provideRouter([]),
      { provide: ProductoService, useValue: servicioMock },
      { provide: NotificationService, useValue: notificationMock },
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
    notificationMock,
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
      imagenUrl: '',
    });
    componente.guardar();
    fixture.detectChanges();

    expect(componente.form.invalid).toBe(true);
    expect(servicioMock.crear).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'El nombre es obligatorio',
    );
  });

  it('crea un producto valido, notifica y navega al listado', async () => {
    const { fixture, componente, servicioMock, notificationMock, router } = await configurar();
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    componente.form.setValue({
      nombre: 'Laptop',
      precio: 2500000,
      stock: 10,
      categoria: 'Tecnologia',
      descripcion: 'Laptop de alto rendimiento',
      imagenUrl: '',
    });
    componente.guardar();

    expect(servicioMock.crear).toHaveBeenCalledWith({
      nombre: 'Laptop',
      precio: 2500000,
      stock: 10,
      categoria: 'Tecnologia',
      descripcion: 'Laptop de alto rendimiento',
      imagenUrl: '',
    });
    expect(notificationMock.exito).toHaveBeenCalledWith(
      'Producto creado',
      expect.stringContaining('Laptop'),
    );
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
      imagenUrl: '',
    });
  });

  it('actualiza el producto en modo edicion y notifica', async () => {
    const { fixture, componente, servicioMock, notificationMock, router } = await configurar('5');
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    componente.form.setValue({
      nombre: 'Laptop Gamer',
      precio: 2300000,
      stock: 8,
      categoria: 'Tecnologia',
      descripcion: 'Version gamer',
      imagenUrl: '',
    });
    componente.guardar();

    expect(servicioMock.actualizar).toHaveBeenCalledWith(5, {
      nombre: 'Laptop Gamer',
      precio: 2300000,
      stock: 8,
      categoria: 'Tecnologia',
      descripcion: 'Version gamer',
      imagenUrl: '',
    });
    expect(notificationMock.exito).toHaveBeenCalledWith(
      'Producto actualizado',
      expect.stringContaining('Laptop Gamer'),
    );
    expect(navegar).toHaveBeenCalledWith(['/productos']);
  });
});
