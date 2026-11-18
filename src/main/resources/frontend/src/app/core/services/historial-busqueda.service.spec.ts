import { TestBed } from '@angular/core/testing';
import { HistorialBusquedaService } from './historial-busqueda.service';

describe('HistorialBusquedaService', () => {
  let service: HistorialBusquedaService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(HistorialBusquedaService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('registra las busquedas mas recientes y elimina duplicados sin distinguir mayusculas', () => {
    service.registrar('  Laptop  ');
    service.registrar('Mouse');
    service.registrar('laptop');

    expect(service.terminos()).toEqual(['laptop', 'Mouse']);
    expect(localStorage.getItem('productos.busquedas')).toContain('laptop');
  });

  it('ignora busquedas vacias o demasiado cortas', () => {
    service.registrar('');
    service.registrar('   ');
    service.registrar('a');

    expect(service.terminos()).toEqual([]);
    expect(localStorage.getItem('productos.busquedas')).toBeNull();
  });

  it('conserva como maximo ocho busquedas', () => {
    for (let indice = 1; indice <= 10; indice++) {
      service.registrar(`Busqueda ${indice}`);
    }

    expect(service.terminos()).toHaveLength(8);
    expect(service.terminos()[0]).toBe('Busqueda 10');
    expect(service.terminos()[7]).toBe('Busqueda 3');
  });

  it('recupera el historial guardado al crear el servicio', () => {
    localStorage.setItem(
      'productos.busquedas',
      JSON.stringify(['Laptop', ' laptop ', 42, '', 'Mouse']),
    );

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    const nuevo = TestBed.inject(HistorialBusquedaService);

    expect(nuevo.terminos()).toEqual(['Laptop', 'Mouse']);
  });

  it('quita una busqueda sin distinguir mayusculas', () => {
    service.registrar('Laptop');
    service.registrar('Mouse');

    service.quitar('LAPTOP');

    expect(service.terminos()).toEqual(['Mouse']);
  });

  it('limpia el historial y el almacenamiento', () => {
    service.registrar('Laptop');
    service.limpiar();

    expect(service.terminos()).toEqual([]);
    expect(service.hayBusquedas()).toBe(false);
    expect(localStorage.getItem('productos.busquedas')).toBeNull();
  });

  it('descarta un historial corrupto', () => {
    localStorage.setItem('productos.busquedas', '{no-es-json');

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    const nuevo = TestBed.inject(HistorialBusquedaService);

    expect(nuevo.terminos()).toEqual([]);
    expect(localStorage.getItem('productos.busquedas')).toBeNull();
  });
});
