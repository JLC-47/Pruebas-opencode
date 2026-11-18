import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { ConfirmDialogComponent } from './confirm-dialog.component';

describe('ConfirmDialogComponent', () => {
  let fixture: ComponentFixture<ConfirmDialogComponent>;
  let dialogo: ConfirmDialogService;
  let elemento: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfirmDialogComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmDialogComponent);
    dialogo = TestBed.inject(ConfirmDialogService);
    elemento = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();
  });

  it('permanece oculto mientras no hay una confirmacion activa', () => {
    expect(elemento.querySelector('.confirm-backdrop')).toBeNull();
  });

  it('muestra el dialogo y confirma al pulsar el boton principal', async () => {
    const respuesta = dialogo.confirmar({
      titulo: 'Eliminar producto',
      mensaje: 'Vas a eliminar la laptop del catalogo.',
      detalle: 'Esta accion no se puede deshacer.',
      etiquetaConfirmar: 'Eliminar producto',
      etiquetaCancelar: 'Conservar',
      peligro: true,
    });
    fixture.detectChanges();

    const tarjeta = elemento.querySelector('.confirm-card') as HTMLElement;
    expect(tarjeta).toBeTruthy();
    expect(tarjeta.classList.contains('confirm-danger')).toBe(true);
    expect(tarjeta.textContent).toContain('Eliminar producto');
    expect(tarjeta.textContent).toContain('Esta accion no se puede deshacer.');

    const botones = tarjeta.querySelectorAll<HTMLButtonElement>('footer button');
    expect(botones).toHaveLength(2);
    botones[1].click();

    await expect(respuesta).resolves.toBe(true);
    fixture.detectChanges();
    expect(elemento.querySelector('.confirm-backdrop')).toBeNull();
  });

  it('cancela la confirmacion con Escape', async () => {
    const respuesta = dialogo.confirmar({
      titulo: 'Vaciar carrito',
      mensaje: 'Quitaremos todos los productos.',
    });
    fixture.detectChanges();

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));

    await expect(respuesta).resolves.toBe(false);
    fixture.detectChanges();
    expect(elemento.querySelector('.confirm-backdrop')).toBeNull();
  });
});
