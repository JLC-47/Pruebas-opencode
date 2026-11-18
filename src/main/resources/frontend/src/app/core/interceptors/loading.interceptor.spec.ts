import { HttpHandlerFn, HttpRequest, HttpResponse, provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { LoadingService } from '../services/loading.service';
import { loadingInterceptor } from './loading.interceptor';

describe('loadingInterceptor', () => {
  const loadingMock = {
    mostrar: vi.fn(),
    ocultar: vi.fn(),
  };

  const request = new HttpRequest('GET', '/api/productos');

  beforeEach(() => {
    loadingMock.mostrar.mockReset();
    loadingMock.ocultar.mockReset();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), { provide: LoadingService, useValue: loadingMock }],
    });
  });

  it('muestra la animacion y la oculta al completar la peticion', () => {
    const next = vi.fn<HttpHandlerFn>(() => of(new HttpResponse({ status: 200 })));

    TestBed.runInInjectionContext(() => {
      loadingInterceptor(request, next).subscribe();
    });

    expect(loadingMock.mostrar).toHaveBeenCalledTimes(1);
    expect(loadingMock.ocultar).toHaveBeenCalledTimes(1);
    expect(loadingMock.mostrar.mock.invocationCallOrder[0]).toBeLessThan(
      next.mock.invocationCallOrder[0],
    );
  });

  it('oculta la animacion cuando la peticion falla', () => {
    const next = vi.fn<HttpHandlerFn>(() => throwError(() => new Error('fallo')));

    TestBed.runInInjectionContext(() => {
      loadingInterceptor(request, next).subscribe({ error: () => undefined });
    });

    expect(loadingMock.mostrar).toHaveBeenCalledTimes(1);
    expect(loadingMock.ocultar).toHaveBeenCalledTimes(1);
  });
});
