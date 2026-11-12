import {
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { ProblemDetail } from '../models/producto.model';
import { AuthService } from '../services/auth.service';

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly problem?: ProblemDetail,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export const apiInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const esEndpointAuth = request.url.includes('/api/auth/');

  const preparar = (token: string | null): HttpRequest<unknown> => {
    const headers: Record<string, string> = {
      Accept: 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
    };

    if (token && !esEndpointAuth) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return request.clone({ setHeaders: headers });
  };

  return next(preparar(auth.accessToken())).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !esEndpointAuth
      ) {
        return auth.refrescar().pipe(
          catchError((errorRefresco: unknown) => {
            const returnUrl = router.url;
            auth.limpiarSesion();
            void router.navigate(['/login'], {
              queryParams:
                returnUrl && returnUrl !== '/login' ? { returnUrl } : {},
            });

            return throwError(() =>
              convertirError(
                errorRefresco instanceof HttpErrorResponse &&
                  errorRefresco.status === 401
                  ? errorRefresco
                  : error,
                'Tu sesion expiro. Inicia sesion nuevamente.',
              ),
            );
          }),
          switchMap(() =>
            next(preparar(auth.accessToken())).pipe(
              catchError((errorReintento: unknown) =>
                throwError(() => convertirError(errorReintento)),
              ),
            ),
          ),
        );
      }

      return throwError(() => convertirError(error));
    }),
  );
};

function convertirError(error: unknown, mensajePorDefecto?: string): ApiError {
  if (error instanceof ApiError) {
    return error;
  }

  if (error instanceof HttpErrorResponse) {
    return new ApiError(
      extraerMensaje(error) || mensajePorDefecto || 'Error HTTP',
      error.status,
      error.error,
    );
  }

  const mensaje =
    error instanceof Error ? error.message : mensajePorDefecto ?? 'Error inesperado';
  return new ApiError(mensaje, 0);
}

function extraerMensaje(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'No se pudo conectar con el servidor. Verifica que la API este en ejecucion.';
  }

  const problem = error.error as ProblemDetail | null;
  if (problem?.errors && Object.keys(problem.errors).length > 0) {
    const detalles = Object.values(problem.errors).join('. ');
    return problem.detail ? `${problem.detail}. ${detalles}` : detalles;
  }

  return problem?.detail ?? problem?.title ?? `Error HTTP ${error.status}`;
}
