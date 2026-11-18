import { TestBed } from '@angular/core/testing';
import { LoadingService } from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoadingService);
  });

  afterEach(() => {
    vi.clearAllTimers();
    vi.useRealTimers();
  });

  it('permanece visible al menos tres segundos', () => {
    service.mostrar();

    expect(service.cargando()).toBe(true);

    vi.advanceTimersByTime(2_000);
    service.ocultar();

    expect(service.cargando()).toBe(true);

    vi.advanceTimersByTime(999);
    expect(service.cargando()).toBe(true);

    vi.advanceTimersByTime(1);
    expect(service.cargando()).toBe(false);
  });

  it('oculta de inmediato cuando la peticion tarda mas de tres segundos', () => {
    service.mostrar();

    vi.advanceTimersByTime(4_000);
    service.ocultar();

    expect(service.cargando()).toBe(false);
  });

  it('espera a que terminen todas las peticiones concurrentes', () => {
    service.mostrar();
    service.mostrar();

    vi.advanceTimersByTime(3_000);
    service.ocultar();

    expect(service.cargando()).toBe(true);

    service.ocultar();

    expect(service.cargando()).toBe(false);
  });

  it('reinicia la espera cuando empieza otra peticion durante la cuenta atras', () => {
    service.mostrar();
    vi.advanceTimersByTime(1_000);
    service.ocultar();
    vi.advanceTimersByTime(1_000);

    service.mostrar();
    vi.advanceTimersByTime(2_500);

    expect(service.cargando()).toBe(true);

    service.ocultar();
    vi.advanceTimersByTime(499);

    expect(service.cargando()).toBe(true);

    vi.advanceTimersByTime(1);

    expect(service.cargando()).toBe(false);
  });
});
