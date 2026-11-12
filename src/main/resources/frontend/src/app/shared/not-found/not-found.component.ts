import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <section class="page not-found">
      <span class="not-found-code">404</span>
      <h1>Pagina no encontrada</h1>
      <p class="muted">La ruta que buscas no existe o fue movida.</p>
      <a class="btn btn-primary" routerLink="/productos">Volver a productos</a>
    </section>
  `,
  styles: `
    .not-found {
      display: grid;
      place-items: center;
      gap: 0.75rem;
      padding: 4rem 1rem;
      text-align: center;
    }

    .not-found-code {
      color: var(--color-primary);
      font-size: 4rem;
      font-weight: 800;
      line-height: 1;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotFoundComponent {}
