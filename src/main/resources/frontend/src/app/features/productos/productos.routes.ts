import { Routes } from '@angular/router';
import { authGuard, vendedorGuard } from '../../core/guards/auth.guard';

export const PRODUCTOS_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./producto-list/producto-list.component').then((m) => m.ProductoListComponent),
  },
  {
    path: 'nuevo',
    canActivate: [vendedorGuard],
    loadComponent: () =>
      import('./producto-form/producto-form.component').then((m) => m.ProductoFormComponent),
  },
  {
    path: ':id/editar',
    canActivate: [vendedorGuard],
    loadComponent: () =>
      import('./producto-form/producto-form.component').then((m) => m.ProductoFormComponent),
  },
];
