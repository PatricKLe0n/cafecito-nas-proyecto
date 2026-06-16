import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { Shell } from './layout/shell';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard) },
      { path: 'fincas', loadComponent: () => import('./features/fincas/fincas').then((m) => m.Fincas) },
      { path: 'lotes-verdes', loadComponent: () => import('./features/lotes-verdes/lotes-verdes').then((m) => m.LotesVerdes) },
      { path: 'lotes-tostados', loadComponent: () => import('./features/lotes-tostados/lotes-tostados').then((m) => m.LotesTostados) },
    ],
  },
  { path: '**', redirectTo: '' },
];
