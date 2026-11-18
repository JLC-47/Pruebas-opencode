import { ChangeDetectionStrategy, Component, HostListener, inject } from '@angular/core';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  template: `
    @if (dialogo.confirmacion(); as confirmacion) {
      <div class="confirm-backdrop" (click)="dialogo.cancelar()">
        <section
          class="confirm-card"
          [class.confirm-danger]="confirmacion.peligro"
          role="dialog"
          aria-modal="true"
          aria-labelledby="confirm-title"
          aria-describedby="confirm-message"
          (click)="$event.stopPropagation()"
        >
          <span class="confirm-accent" aria-hidden="true"></span>

          <header>
            <span class="confirm-icon" aria-hidden="true">
              @if (confirmacion.peligro) {
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.9"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M4 7h16" />
                  <path d="M6 7l1 13h10l1-13" />
                  <path d="M9 7V4h6v3" />
                  <path d="M10 11v5M14 11v5" />
                </svg>
              } @else {
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.9"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <circle cx="12" cy="12" r="9" />
                  <path d="M12 11v5" />
                  <path d="M12 7.5h.01" />
                </svg>
              }
            </span>
            <span class="confirm-eyebrow">
              {{ confirmacion.peligro ? 'Accion delicada' : 'Confirmacion' }}
            </span>
            <h2 id="confirm-title">{{ confirmacion.titulo }}</h2>
          </header>

          <p id="confirm-message" class="confirm-message">{{ confirmacion.mensaje }}</p>

          @if (confirmacion.detalle) {
            <p class="confirm-detail">{{ confirmacion.detalle }}</p>
          }

          <footer>
            <button class="btn" type="button" (click)="dialogo.cancelar()">
              {{ confirmacion.etiquetaCancelar }}
            </button>
            <button
              class="btn"
              [class.btn-primary]="!confirmacion.peligro"
              [class.confirm-danger-button]="confirmacion.peligro"
              type="button"
              (click)="dialogo.aceptar()"
            >
              {{ confirmacion.etiquetaConfirmar }}
            </button>
          </footer>
        </section>
      </div>
    }
  `,
  styles: `
    :host {
      display: contents;
    }

    .confirm-backdrop {
      position: fixed;
      z-index: 9800;
      inset: 0;
      display: grid;
      place-items: center;
      background: rgb(2 6 23 / 64%);
      padding: 1.5rem;
      animation: confirm-fade 180ms ease-out;
      backdrop-filter: blur(8px);
    }

    .confirm-card {
      position: relative;
      width: min(27rem, 100%);
      overflow: hidden;
      border: 1px solid #e2e8f0;
      border-radius: 1.35rem;
      background: #ffffff;
      box-shadow: 0 35px 90px rgb(2 6 23 / 45%);
      padding: 1.55rem;
      animation: confirm-in 280ms cubic-bezier(0.2, 0.9, 0.25, 1.1);
    }

    .confirm-accent {
      position: absolute;
      top: 0;
      right: 0;
      left: 0;
      height: 4px;
      background: linear-gradient(90deg, #38bdf8, #2563eb);
    }

    .confirm-danger .confirm-accent {
      background: linear-gradient(90deg, #f87171, #dc2626);
    }

    header {
      display: grid;
      grid-template-columns: auto 1fr;
      align-items: center;
      gap: 0.2rem 0.8rem;
      margin-bottom: 0.9rem;
    }

    .confirm-icon {
      display: grid;
      grid-row: span 2;
      width: 2.8rem;
      height: 2.8rem;
      place-items: center;
      border-radius: 0.85rem;
      background: #eff6ff;
      color: #0284c7;
    }

    .confirm-danger .confirm-icon {
      background: #fef2f2;
      color: #dc2626;
    }

    .confirm-icon svg {
      width: 1.35rem;
      height: 1.35rem;
    }

    .confirm-eyebrow {
      color: #0284c7;
      font-size: 0.65rem;
      font-weight: 800;
      letter-spacing: 0.14em;
      text-transform: uppercase;
    }

    .confirm-danger .confirm-eyebrow {
      color: #dc2626;
    }

    header h2 {
      color: #0f172a;
      font-size: 1.2rem;
      letter-spacing: -0.02em;
    }

    .confirm-message {
      color: #475569;
      font-size: 0.92rem;
      line-height: 1.55;
    }

    .confirm-detail {
      margin-top: 0.65rem;
      border: 1px solid #e2e8f0;
      border-radius: 0.65rem;
      background: #f8fafc;
      color: #64748b;
      font-size: 0.8rem;
      padding: 0.6rem 0.75rem;
    }

    footer {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-end;
      gap: 0.55rem;
      margin-top: 1.25rem;
    }

    .confirm-danger-button {
      border-color: transparent;
      background: linear-gradient(135deg, #ef4444, #b91c1c);
      color: #ffffff;
      box-shadow: 0 8px 18px rgb(220 38 38 / 24%);
    }

    .confirm-danger-button:hover {
      border-color: transparent;
      background: linear-gradient(135deg, #dc2626, #991b1b);
      color: #ffffff;
    }

    @keyframes confirm-fade {
      from {
        opacity: 0;
      }
    }

    @keyframes confirm-in {
      from {
        opacity: 0;
        transform: translateY(0.8rem) scale(0.96);
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .confirm-backdrop,
      .confirm-card {
        animation-duration: 1ms;
        animation-iteration-count: 1;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDialogComponent {
  readonly dialogo = inject(ConfirmDialogService);

  @HostListener('document:keydown.escape')
  cerrarConEscape(): void {
    this.dialogo.cancelar();
  }
}
