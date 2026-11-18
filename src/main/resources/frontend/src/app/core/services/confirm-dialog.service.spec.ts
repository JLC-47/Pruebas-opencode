import { TestBed } from '@angular/core/testing';
import { ConfirmDialogService } from './confirm-dialog.service';

describe('ConfirmDialogService', () => {
  let service: ConfirmDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfirmDialogService);
  });

  it('resuelve verdadero cuando el usuario acepta', async () => {
    const respuesta = service.confirmar({
      titulo: 'Eliminar producto',
      mensaje: 'Vas a eliminar la laptop del catalogo.',
      peligro: true,
    });

    expect(service.confirmacion()?.titulo).toBe('Eliminar producto');
    expect(service.confirmacion()?.peligro).toBe(true);
    expect(service.confirmacion()?.etiquetaConfirmar).toBe('Confirmar');

    service.aceptar();

    await expect(respuesta).resolves.toBe(true);
    expect(service.confirmacion()).toBeNull();
  });

  it('resuelve falso cuando el usuario cancela', async () => {
    const respuesta = service.confirmar({
      titulo: 'Vaciar carrito',
      mensaje: 'Quitaremos todos los productos.',
      etiquetaCancelar: 'Mantener productos',
    });

    service.cancelar();

    await expect(respuesta).resolves.toBe(false);
    expect(service.confirmacion()).toBeNull();
  });

  it('cancela la confirmacion anterior si se abre otra', async () => {
    const primera = service.confirmar({
      titulo: 'Primera',
      mensaje: 'Mensaje inicial.',
    });
    const segunda = service.confirmar({
      titulo: 'Segunda',
      mensaje: 'Mensaje nuevo.',
    });

    await expect(primera).resolves.toBe(false);
    expect(service.confirmacion()?.titulo).toBe('Segunda');

    service.aceptar();

    await expect(segunda).resolves.toBe(true);
  });
});
