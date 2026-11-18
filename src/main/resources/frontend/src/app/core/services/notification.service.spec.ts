import { TestBed } from '@angular/core/testing';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationService);
  });

  afterEach(() => {
    vi.clearAllTimers();
    vi.useRealTimers();
  });

  it('agrega una notificacion y la elimina al terminar su duracion', () => {
    service.exito('Producto creado', 'La laptop ya forma parte del catalogo.', 4_500);

    expect(service.notificaciones()).toHaveLength(1);
    expect(service.notificaciones()[0]).toMatchObject({
      tipo: 'success',
      titulo: 'Producto creado',
    });

    vi.advanceTimersByTime(4_499);
    expect(service.notificaciones()).toHaveLength(1);

    vi.advanceTimersByTime(1);
    expect(service.notificaciones()).toHaveLength(0);
  });

  it('permite cerrar una notificacion manualmente', () => {
    service.error('No se pudo eliminar', 'El servidor no respondio.');

    const id = service.notificaciones()[0].id;
    service.cerrar(id);

    expect(service.notificaciones()).toHaveLength(0);
  });

  it('cierra varias notificaciones de forma independiente', () => {
    service.info('Carrito vaciado', 'Tu seleccion se limpio.', 1_000);
    service.error('Error de red', 'Revisa tu conexion.', 6_000);

    vi.advanceTimersByTime(1_000);

    expect(service.notificaciones()).toHaveLength(1);
    expect(service.notificaciones()[0].tipo).toBe('error');
  });
});
