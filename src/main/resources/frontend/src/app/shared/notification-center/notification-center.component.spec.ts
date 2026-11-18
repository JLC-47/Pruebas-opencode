import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationCenterComponent } from './notification-center.component';

describe('NotificationCenterComponent', () => {
  let fixture: ComponentFixture<NotificationCenterComponent>;
  let notification: NotificationService;
  let elemento: HTMLElement;

  beforeEach(async () => {
    vi.useFakeTimers();
    await TestBed.configureTestingModule({
      imports: [NotificationCenterComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationCenterComponent);
    notification = TestBed.inject(NotificationService);
    elemento = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
    vi.clearAllTimers();
    vi.useRealTimers();
  });

  it('no muestra notificaciones cuando la pila esta vacia', () => {
    expect(elemento.querySelectorAll('.notification')).toHaveLength(0);
  });

  it('muestra una notificacion de exito y permite cerrarla', () => {
    notification.exito('Producto creado', 'La laptop ya forma parte del catalogo.');
    fixture.detectChanges();

    const notificacion = elemento.querySelector('.notification') as HTMLElement;
    expect(notificacion).toBeTruthy();
    expect(notificacion.classList.contains('notification-success')).toBe(true);
    expect(notificacion.textContent).toContain('Producto creado');
    expect(notificacion.textContent).toContain('La laptop ya forma parte del catalogo.');

    notificacion.querySelector<HTMLButtonElement>('.notification-close')?.click();
    fixture.detectChanges();

    expect(elemento.querySelectorAll('.notification')).toHaveLength(0);
  });

  it('marca los errores como alertas accesibles', () => {
    notification.error('No se pudo eliminar', 'El servidor no respondio.');
    fixture.detectChanges();

    const notificacion = elemento.querySelector('.notification') as HTMLElement;
    expect(notificacion.classList.contains('notification-error')).toBe(true);
    expect(notificacion.getAttribute('role')).toBe('alert');
  });
});
