import { Injectable, signal } from '@angular/core';

export interface ConfirmacionOpciones {
  titulo: string;
  mensaje: string;
  detalle?: string;
  etiquetaConfirmar?: string;
  etiquetaCancelar?: string;
  peligro?: boolean;
}

export interface ConfirmacionActiva extends ConfirmacionOpciones {
  id: number;
  resolver: (confirmado: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private readonly _confirmacion = signal<ConfirmacionActiva | null>(null);
  private siguienteId = 1;

  readonly confirmacion = this._confirmacion.asReadonly();

  confirmar(opciones: ConfirmacionOpciones): Promise<boolean> {
    const anterior = this._confirmacion();
    if (anterior) {
      this._confirmacion.set(null);
      anterior.resolver(false);
    }

    return new Promise((resolver) => {
      this._confirmacion.set({
        ...opciones,
        id: this.siguienteId++,
        etiquetaConfirmar: opciones.etiquetaConfirmar ?? 'Confirmar',
        etiquetaCancelar: opciones.etiquetaCancelar ?? 'Cancelar',
        peligro: opciones.peligro ?? false,
        resolver,
      });
    });
  }

  aceptar(): void {
    const confirmacion = this._confirmacion();
    if (!confirmacion) {
      return;
    }

    this._confirmacion.set(null);
    confirmacion.resolver(true);
  }

  cancelar(): void {
    const confirmacion = this._confirmacion();
    if (!confirmacion) {
      return;
    }

    this._confirmacion.set(null);
    confirmacion.resolver(false);
  }
}
