import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'medications' },
  {
    path: 'medications',
    loadComponent: () =>
      import('@features/medications/medications-list/medications-list').then(
        (m) => m.MedicationsListPage,
      ),
    title: 'Médicaments',
  },
  { path: '**', redirectTo: 'medications' },
];
