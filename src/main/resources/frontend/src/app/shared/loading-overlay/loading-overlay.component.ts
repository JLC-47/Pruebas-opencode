import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { LoadingService } from '../../core/services/loading.service';

@Component({
  selector: 'app-loading-overlay',
  template: `
    @if (loading.cargando()) {
      <div
        class="loading-overlay"
        role="status"
        aria-live="polite"
        aria-label="Cargando informacion"
      >
        <div class="loading-card">
          <div class="loader-orbit" aria-hidden="true">
            <span class="orbit orbit-one"></span>
            <span class="orbit orbit-two"></span>
            <span class="orbit-dot dot-one"></span>
            <span class="orbit-dot dot-two"></span>
            <span class="loader-core">P</span>
          </div>

          <div class="loading-copy">
            <span class="loading-eyebrow">Productos API</span>
            <h2>Preparando tu experiencia</h2>
            <p>Estamos sincronizando la informacion con el servidor.</p>
          </div>

          <span class="loading-track" aria-hidden="true"><span></span></span>

          <span class="loading-dots" aria-hidden="true">
            <span></span>
            <span></span>
            <span></span>
          </span>
        </div>
      </div>
    }
  `,
  styles: `
    :host {
      display: contents;
    }

    .loading-overlay {
      position: fixed;
      z-index: 9000;
      inset: 0;
      display: grid;
      place-items: center;
      background:
        radial-gradient(circle at 50% 38%, rgb(37 99 235 / 24%), transparent 26rem),
        radial-gradient(circle at 12% 88%, rgb(56 189 248 / 14%), transparent 22rem),
        rgb(2 6 23 / 78%);
      padding: 1.5rem;
      cursor: progress;
      backdrop-filter: blur(14px);
      animation: overlay-in 220ms ease-out;
    }

    .loading-card {
      position: relative;
      display: grid;
      width: min(25rem, 100%);
      justify-items: center;
      gap: 1.2rem;
      overflow: hidden;
      border: 1px solid rgb(148 163 184 / 22%);
      border-radius: 1.6rem;
      background: linear-gradient(160deg, rgb(15 23 42 / 96%), rgb(2 6 23 / 98%));
      box-shadow:
        0 30px 90px rgb(2 6 23 / 70%),
        inset 0 1px 0 rgb(255 255 255 / 7%);
      padding: 2.2rem 2rem 2rem;
      text-align: center;
      animation: card-in 380ms cubic-bezier(0.2, 0.9, 0.25, 1.15);
    }

    .loading-card::before {
      position: absolute;
      top: -7rem;
      left: 50%;
      width: 16rem;
      height: 12rem;
      border-radius: 50%;
      background: rgb(56 189 248 / 18%);
      content: '';
      filter: blur(3rem);
      transform: translateX(-50%);
    }

    .loader-orbit {
      position: relative;
      display: grid;
      width: 9rem;
      height: 9rem;
      place-items: center;
    }

    .orbit {
      position: absolute;
      border: 2px solid transparent;
      border-radius: 50%;
    }

    .orbit-one {
      inset: 0;
      border-top-color: #38bdf8;
      border-right-color: rgb(56 189 248 / 35%);
      box-shadow: 0 0 1.2rem rgb(56 189 248 / 20%);
      animation: spin 2.4s linear infinite;
    }

    .orbit-two {
      inset: 0.85rem;
      border-bottom-color: #818cf8;
      border-left-color: rgb(129 140 248 / 30%);
      animation: spin-reverse 3.2s linear infinite;
    }

    .orbit-dot {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 0.5rem;
      height: 0.5rem;
      margin: -0.25rem;
      border-radius: 50%;
      background: #7dd3fc;
      box-shadow: 0 0 0.9rem #38bdf8;
      animation: orbit 2.8s linear infinite;
    }

    .dot-two {
      width: 0.4rem;
      height: 0.4rem;
      margin: -0.2rem;
      background: #c4b5fd;
      box-shadow: 0 0 0.9rem #818cf8;
      animation: orbit 4.1s linear infinite reverse;
      animation-delay: -1.4s;
    }

    .loader-core {
      display: grid;
      width: 4.4rem;
      height: 4.4rem;
      place-items: center;
      border: 1px solid rgb(255 255 255 / 24%);
      border-radius: 1.35rem;
      background: linear-gradient(135deg, #38bdf8, #2563eb 55%, #6d28d9);
      color: #ffffff;
      font-size: 1.7rem;
      font-weight: 800;
      box-shadow:
        0 0 1.8rem rgb(56 189 248 / 42%),
        inset 0 1px 0 rgb(255 255 255 / 35%);
      animation: core-pulse 1.8s ease-in-out infinite;
    }

    .loading-copy {
      display: grid;
      gap: 0.3rem;
      position: relative;
    }

    .loading-eyebrow {
      color: #7dd3fc;
      font-size: 0.66rem;
      font-weight: 800;
      letter-spacing: 0.2em;
      text-transform: uppercase;
    }

    .loading-copy h2 {
      color: #f8fafc;
      font-size: 1.3rem;
      letter-spacing: -0.025em;
    }

    .loading-copy p {
      color: #94a3b8;
      font-size: 0.84rem;
    }

    .loading-track {
      width: 100%;
      height: 0.36rem;
      overflow: hidden;
      border-radius: 999px;
      background: rgb(148 163 184 / 16%);
    }

    .loading-track span {
      display: block;
      width: 100%;
      height: 100%;
      border-radius: inherit;
      background: linear-gradient(90deg, #38bdf8, #6366f1 55%, #a855f7);
      box-shadow: 0 0 1rem rgb(56 189 248 / 65%);
      animation: progress 5s cubic-bezier(0.22, 1, 0.36, 1) forwards;
      transform-origin: left;
    }

    .loading-dots {
      display: inline-flex;
      gap: 0.35rem;
    }

    .loading-dots span {
      width: 0.34rem;
      height: 0.34rem;
      border-radius: 50%;
      background: #38bdf8;
      animation: dot-bounce 1.15s ease-in-out infinite;
    }

    .loading-dots span:nth-child(2) {
      animation-delay: 140ms;
    }

    .loading-dots span:nth-child(3) {
      animation-delay: 280ms;
    }

    @keyframes overlay-in {
      from {
        opacity: 0;
      }
    }

    @keyframes card-in {
      from {
        opacity: 0;
        transform: translateY(1rem) scale(0.94);
      }
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }

    @keyframes spin-reverse {
      to {
        transform: rotate(-360deg);
      }
    }

    @keyframes orbit {
      from {
        transform: rotate(0deg) translateX(3.8rem);
      }
      to {
        transform: rotate(360deg) translateX(3.8rem);
      }
    }

    @keyframes core-pulse {
      0%,
      100% {
        transform: scale(1);
      }
      50% {
        transform: scale(1.06);
      }
    }

    @keyframes progress {
      from {
        transform: scaleX(0);
      }
      45% {
        transform: scaleX(0.58);
      }
      to {
        transform: scaleX(1);
      }
    }

    @keyframes dot-bounce {
      0%,
      100% {
        opacity: 0.35;
        transform: translateY(0);
      }
      50% {
        opacity: 1;
        transform: translateY(-0.3rem);
      }
    }

    @media (max-width: 420px) {
      .loading-card {
        padding: 1.8rem 1.25rem 1.5rem;
      }

      .loader-orbit {
        width: 7.5rem;
        height: 7.5rem;
      }

      .loader-core {
        width: 3.8rem;
        height: 3.8rem;
        border-radius: 1.1rem;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .loading-overlay,
      .loading-card,
      .orbit,
      .orbit-dot,
      .loader-core,
      .loading-track span,
      .loading-dots span {
        animation-duration: 1ms;
        animation-iteration-count: 1;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoadingOverlayComponent {
  readonly loading = inject(LoadingService);
}
