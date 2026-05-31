import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () =>
      import('./public/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'internal',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./internal/layout/internal-layout.component').then(m => m.InternalLayoutComponent),
    children: [
      { path: '', redirectTo: 'profesionales', pathMatch: 'full' },
      {
        path: 'profesionales',
        loadComponent: () =>
          import('./internal/profesionales/profesional-list/profesional-list.component').then(
            m => m.ProfesionalListComponent
          ),
      },
      {
        path: 'profesionales/nuevo',
        loadComponent: () =>
          import('./internal/profesionales/profesional-form/profesional-form.component').then(
            m => m.ProfesionalFormComponent
          ),
      },
      {
        path: 'profesionales/:id/editar',
        loadComponent: () =>
          import('./internal/profesionales/profesional-form/profesional-form.component').then(
            m => m.ProfesionalFormComponent
          ),
      },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
