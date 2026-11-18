import { DestroyRef, Injectable, inject, signal } from '@angular/core';

export type TipoNotificacion = 'success' | 'error' | 'info';

export interface Notificacion {
  id: number;
  tipo: TipoNotificacion;
  titulo: string;
  mensaje?: string;
  duracion: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly destroyRef = inject(DestroyRef);
  private readonly _notificaciones = signal<Notificacion[]>([]);
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private siguienteId = 1;

  readonly notificaciones = this._notificaciones.asReadonly();

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.timers.forEach((timer) => clearTimeout(timer));
      this.timers.clear();
    });
  }

  exito(titulo: string, mensaje?: string, duracion = 4_500): void {
    this.agregar('success', titulo, mensaje, duracion);
  }

  error(titulo: string, mensaje?: string, duracion = 6_000): void {
    this.agregar('error', titulo, mensaje, duracion);
  }

  info(titulo: string, mensaje?: string, duracion = 4_500): void {
    this.agregar('info', titulo, mensaje, duracion);
  }

  cerrar(id: number): void {
    const timer = this.timers.get(id);
    if (timer !== undefined) {
      clearTimeout(timer);
      this.timers.delete(id);
    }

    this._notificaciones.update((notificaciones) =>
      notificaciones.filter((notificacion) => notificacion.id !== id),
    );
  }

  private agregar(
    tipo: TipoNotificacion,
    titulo: string,
    mensaje: string | undefined,
    duracion: number,
  ): void {
    const notificacion: Notificacion = {
      id: this.siguienteId++,
      tipo,
      titulo,
      mensaje,
      duracion,
    };

    this._notificaciones.update((notificaciones) => [...notificaciones, notificacion]);
    this.timers.set(
      notificacion.id,
      setTimeout(() => this.cerrar(notificacion.id), duracion),
    );
  }
}
