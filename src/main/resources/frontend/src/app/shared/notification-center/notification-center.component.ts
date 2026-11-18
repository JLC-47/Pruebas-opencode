import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-notification-center',
  template: `
    <div class="notification-stack" aria-label="Notificaciones" aria-live="polite">
      @for (notificacion of notification.notificaciones(); track notificacion.id) {
        <article
          class="notification"
          [class.notification-success]="notificacion.tipo === 'success'"
          [class.notification-error]="notificacion.tipo === 'error'"
          [class.notification-info]="notificacion.tipo === 'info'"
          [attr.role]="notificacion.tipo === 'error' ? 'alert' : 'status'"
        >
          <span class="notification-icon" aria-hidden="true">
            @switch (notificacion.tipo) {
              @case ('success') {
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2.2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="m5 12 4.2 4.2L19 6.5" />
                </svg>
              }
              @case ('error') {
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <circle cx="12" cy="12" r="9" />
                  <path d="M12 7.5v5.5" />
                  <path d="M12 16.5h.01" />
                </svg>
              }
              @default {
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <circle cx="12" cy="12" r="9" />
                  <path d="M12 11v5" />
                  <path d="M12 7.5h.01" />
                </svg>
              }
            }
          </span>

          <span class="notification-body">
            <strong>{{ notificacion.titulo }}</strong>
            @if (notificacion.mensaje) {
              <p>{{ notificacion.mensaje }}</p>
            }
          </span>

          <button
            class="notification-close"
            type="button"
            aria-label="Cerrar notificacion"
            (click)="notification.cerrar(notificacion.id)"
          >
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              aria-hidden="true"
            >
              <path d="m6 6 12 12M18 6 6 18" />
            </svg>
          </button>

          <span
            class="notification-progress"
            aria-hidden="true"
            [style.animation-duration.ms]="notificacion.duracion"
          ></span>
        </article>
      }
    </div>
  `,
  styles: `
    .notification-stack {
      position: fixed;
      z-index: 10000;
      top: 5.2rem;
      right: 1rem;
      display: grid;
      width: min(23rem, calc(100vw - 2rem));
      gap: 0.65rem;
      pointer-events: none;
    }

    .notification {
      --tono: #0284c7;
      --tono-suave: #eff6ff;
      position: relative;
      display: grid;
      grid-template-columns: auto 1fr auto;
      align-items: start;
      gap: 0.7rem;
      overflow: hidden;
      border: 1px solid #e2e8f0;
      border-left: 4px solid var(--tono);
      border-radius: 0.95rem;
      background: rgb(255 255 255 / 96%);
      box-shadow: 0 18px 45px rgb(15 23 42 / 16%);
      padding: 0.8rem 0.8rem 0.9rem;
      pointer-events: auto;
      animation: notification-in 320ms cubic-bezier(0.2, 0.9, 0.25, 1.1);
      backdrop-filter: blur(12px);
    }

    .notification-success {
      --tono: #059669;
      --tono-suave: #ecfdf5;
    }

    .notification-error {
      --tono: #dc2626;
      --tono-suave: #fef2f2;
    }

    .notification-info {
      --tono: #0284c7;
      --tono-suave: #eff6ff;
    }

    .notification-icon {
      display: grid;
      width: 2rem;
      height: 2rem;
      place-items: center;
      border-radius: 0.65rem;
      background: var(--tono-suave);
      color: var(--tono);
    }

    .notification-icon svg {
      width: 1.1rem;
      height: 1.1rem;
    }

    .notification-body {
      display: grid;
      gap: 0.12rem;
      padding-top: 0.1rem;
    }

    .notification-body strong {
      color: #0f172a;
      font-size: 0.88rem;
      line-height: 1.25;
    }

    .notification-body p {
      color: #64748b;
      font-size: 0.78rem;
      line-height: 1.4;
    }

    .notification-close {
      display: grid;
      width: 1.75rem;
      height: 1.75rem;
      place-items: center;
      border: 0;
      border-radius: 0.5rem;
      background: transparent;
      color: #94a3b8;
      cursor: pointer;
      padding: 0;
    }

    .notification-close:hover {
      background: #f1f5f9;
      color: #334155;
    }

    .notification-close svg {
      width: 0.9rem;
      height: 0.9rem;
    }

    .notification-progress {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 3px;
      background: var(--tono);
      opacity: 0.55;
      transform-origin: left;
      animation: notification-progress linear forwards;
    }

    @keyframes notification-in {
      from {
        opacity: 0;
        transform: translateX(1.4rem) scale(0.97);
      }
    }

    @keyframes notification-progress {
      to {
        transform: scaleX(0);
      }
    }

    @media (max-width: 520px) {
      .notification-stack {
        top: auto;
        right: 0.75rem;
        bottom: 0.75rem;
        left: 0.75rem;
        width: auto;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .notification,
      .notification-progress {
        animation-duration: 1ms;
        animation-iteration-count: 1;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationCenterComponent {
  readonly notification = inject(NotificationService);
}
