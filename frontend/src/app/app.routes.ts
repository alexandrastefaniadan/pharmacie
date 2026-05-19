import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'medications' },
  {
    path: 'login',
    loadComponent: () =>
      import('@features/auth/login-page/login-page').then((m) => m.LoginPage),
    title: 'Connexion',
  },
  {
    path: 'medications',
    canActivate: [authGuard],
    loadComponent: () =>
      import('@features/medications/medications-list/medications-list').then(
        (m) => m.MedicationsListPage,
      ),
    title: 'Médicaments',
  },
  {
    path: 'categories',
    canActivate: [authGuard],
    loadComponent: () =>
      import('@features/reference-data/reference-data-page/reference-data-page').then(
        (m) => m.ReferenceDataPage,
      ),
    title: 'Catégories',
  },
  { path: '**', redirectTo: 'medications' },
];
