import { Routes } from '@angular/router';
import { adminGuard, authGuard } from '../../core/guards/auth.guard';

export const PRODUCTOS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./producto-list/producto-list.component').then(
        (m) => m.ProductoListComponent,
      ),
  },
  {
    path: 'nuevo',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./producto-form/producto-form.component').then(
        (m) => m.ProductoFormComponent,
      ),
  },
  {
    path: ':id/editar',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./producto-form/producto-form.component').then(
        (m) => m.ProductoFormComponent,
      ),
  },
];
