import { Injectable, signal } from '@angular/core';

export const DURACION_MINIMA_CARGA_MS = 3_000;

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly visibles = signal(false);
  private solicitudesActivas = 0;
  private ocultarTimer: ReturnType<typeof setTimeout> | null = null;
  private mostradoEn = 0;

  readonly cargando = this.visibles.asReadonly();

  mostrar(): void {
    this.solicitudesActivas += 1;
    this.mostradoEn = Date.now();

    if (this.ocultarTimer !== null) {
      clearTimeout(this.ocultarTimer);
      this.ocultarTimer = null;
    }

    this.visibles.set(true);
  }

  ocultar(): void {
    if (this.solicitudesActivas === 0) {
      return;
    }

    this.solicitudesActivas -= 1;

    if (this.solicitudesActivas > 0) {
      return;
    }

    const transcurrido = Date.now() - this.mostradoEn;
    const restante = Math.max(0, DURACION_MINIMA_CARGA_MS - transcurrido);

    if (restante === 0) {
      this.visibles.set(false);
      return;
    }

    this.ocultarTimer = setTimeout(() => {
      this.ocultarTimer = null;
      this.visibles.set(false);
    }, restante);
  }
}
