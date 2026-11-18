import { Injectable, computed, signal } from '@angular/core';

const STORAGE_KEY = 'productos.busquedas';
const MAXIMO = 8;

@Injectable({ providedIn: 'root' })
export class HistorialBusquedaService {
  private readonly terminosSignal = signal<string[]>(this.leer());

  readonly terminos = this.terminosSignal.asReadonly();
  readonly hayBusquedas = computed(() => this.terminosSignal().length > 0);

  registrar(termino: string): void {
    const limpio = this.normalizar(termino);
    if (limpio.length < 2) {
      return;
    }

    this.terminosSignal.update((terminos) => {
      const clave = limpio.toLocaleLowerCase('es');
      return [
        limpio,
        ...terminos.filter((actual) => actual.toLocaleLowerCase('es') !== clave),
      ].slice(0, MAXIMO);
    });
    this.guardar();
  }

  quitar(termino: string): void {
    const clave = termino.toLocaleLowerCase('es');
    this.terminosSignal.update((terminos) =>
      terminos.filter((actual) => actual.toLocaleLowerCase('es') !== clave),
    );
    this.guardar();
  }

  limpiar(): void {
    this.terminosSignal.set([]);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  private normalizar(termino: string): string {
    return termino.trim().replace(/\s+/g, ' ');
  }

  private guardar(): void {
    if (typeof localStorage === 'undefined') {
      return;
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.terminosSignal()));
  }

  private leer(): string[] {
    if (typeof localStorage === 'undefined') {
      return [];
    }

    try {
      const valor = localStorage.getItem(STORAGE_KEY);
      if (!valor) {
        return [];
      }

      const terminos = JSON.parse(valor) as unknown;
      if (!Array.isArray(terminos)) {
        localStorage.removeItem(STORAGE_KEY);
        return [];
      }

      const vistos = new Set<string>();
      return terminos
        .filter((termino): termino is string => typeof termino === 'string')
        .map((termino) => this.normalizar(termino))
        .filter((termino) => {
          if (termino.length < 2) {
            return false;
          }
          const clave = termino.toLocaleLowerCase('es');
          if (vistos.has(clave)) {
            return false;
          }
          vistos.add(clave);
          return true;
        })
        .slice(0, MAXIMO);
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return [];
    }
  }
}
