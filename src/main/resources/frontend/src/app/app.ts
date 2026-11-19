import { ChangeDetectionStrategy, Component, effect, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CartService } from './core/services/cart.service';
import { AuthService } from './core/services/auth.service';
import { ConfirmDialogComponent } from './shared/confirm-dialog/confirm-dialog.component';
import { LoadingOverlayComponent } from './shared/loading-overlay/loading-overlay.component';
import { NotificationCenterComponent } from './shared/notification-center/notification-center.component';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    LoadingOverlayComponent,
    NotificationCenterComponent,
    ConfirmDialogComponent,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  readonly auth = inject(AuthService);
  readonly cart = inject(CartService);
  private readonly router = inject(Router);

  constructor() {
    effect(() => {
      if (typeof document === 'undefined') {
        return;
      }
      const preferencia = this.auth.usuario()?.tema ?? 'claro';
      const oscuro =
        preferencia === 'oscuro' ||
        (preferencia === 'sistema' &&
          typeof window !== 'undefined' &&
          window.matchMedia('(prefers-color-scheme: dark)').matches);
      document.documentElement.dataset['tema'] = oscuro ? 'oscuro' : 'claro';
    });
  }

  cerrarSesion(): void {
    this.auth.cerrarSesion();
    this.cart.limpiar();
    void this.router.navigate(['/login']);
  }
}
