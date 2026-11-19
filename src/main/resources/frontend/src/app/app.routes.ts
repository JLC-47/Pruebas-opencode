import { Routes } from '@angular/router';
import { adminGuard, authGuard, vendedorGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'productos',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'registro',
    loadComponent: () =>
      import('./features/auth/registro/registro.component').then((m) => m.RegistroComponent),
  },
  {
    path: 'productos',
    loadChildren: () =>
      import('./features/productos/productos.routes').then((m) => m.PRODUCTOS_ROUTES),
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/perfil/perfil.component').then((m) => m.PerfilComponent),
  },
  {
    path: 'vendedor',
    canActivate: [vendedorGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'productos' },
      {
        path: 'productos',
        loadComponent: () =>
          import('./features/vendedor/vendedor-productos.component').then(
            (m) => m.VendedorProductosComponent,
          ),
      },
      {
        path: 'ventas',
        loadComponent: () =>
          import('./features/vendedor/vendedor-ventas.component').then(
            (m) => m.VendedorVentasComponent,
          ),
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'usuarios' },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./features/admin/admin-usuarios.component').then((m) => m.AdminUsuariosComponent),
      },
    ],
  },
  {
    path: 'carrito',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/carrito/carrito.component').then((m) => m.CarritoComponent),
  },
  {
    path: 'mis-compras',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/compras/mis-compras.component').then((m) => m.MisComprasComponent),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./shared/not-found/not-found.component').then((m) => m.NotFoundComponent),
  },
];
