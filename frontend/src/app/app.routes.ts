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
    path: 'treatments',
    canActivate: [authGuard],
    loadComponent: () =>
      import('@features/treatments/treatments-list/treatments-list').then(
        (m) => m.TreatmentsListPage,
      ),
    data: { usageType: 'HUMAN' },
    title: 'Traitements',
  },
  {
    path: 'veterinary-treatments',
    canActivate: [authGuard],
    loadComponent: () =>
      import('@features/treatments/treatments-list/treatments-list').then(
        (m) => m.TreatmentsListPage,
      ),
    data: { usageType: 'VETERINARY' },
    title: 'Traitements vétérinaires',
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


