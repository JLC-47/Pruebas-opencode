import { TestBed } from '@angular/core/testing';
import { LoadingService } from '../../core/services/loading.service';
import { LoadingOverlayComponent } from './loading-overlay.component';

describe('LoadingOverlayComponent', () => {
  let service: LoadingService;

  beforeEach(async () => {
    vi.useFakeTimers();
    await TestBed.configureTestingModule({
      imports: [LoadingOverlayComponent],
    }).compileComponents();

    service = TestBed.inject(LoadingService);
  });

  afterEach(() => {
    vi.clearAllTimers();
    vi.useRealTimers();
  });

  it('no muestra el overlay cuando no hay peticiones activas', () => {
    const fixture = TestBed.createComponent(LoadingOverlayComponent);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('.loading-overlay')).toBeNull();
  });

  it('muestra y oculta la animacion con el estado del servicio', () => {
    const fixture = TestBed.createComponent(LoadingOverlayComponent);
    service.mostrar();
    fixture.detectChanges();

    const overlay = (fixture.nativeElement as HTMLElement).querySelector('.loading-overlay');

    expect(overlay?.textContent).toContain('Preparando tu experiencia');

    vi.advanceTimersByTime(3_000);
    service.ocultar();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('.loading-overlay')).toBeNull();
  });
});
