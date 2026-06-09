import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./public/landing/landing.component').then(m => m.LandingComponent),
  },
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
      { path: '', redirectTo: 'bandeja', pathMatch: 'full' },
      {
        path: 'bandeja',
        loadComponent: () =>
          import('./internal/formularios-interes/bandeja/bandeja.component').then(
            m => m.BandejaComponent
          ),
      },
      {
        path: 'pacientes',
        loadComponent: () =>
          import('./internal/pacientes/paciente-list/paciente-list.component').then(
            m => m.PacienteListComponent
          ),
      },
      {
        path: 'pacientes/nuevo',
        loadComponent: () =>
          import('./internal/pacientes/paciente-form/paciente-form.component').then(
            m => m.PacienteFormComponent
          ),
      },
      {
        path: 'pacientes/:id/editar',
        loadComponent: () =>
          import('./internal/pacientes/paciente-form/paciente-form.component').then(
            m => m.PacienteFormComponent
          ),
      },
      {
        path: 'pacientes/:id',
        loadComponent: () =>
          import('./internal/pacientes/paciente-detail/paciente-detail.component').then(
            m => m.PacienteDetailComponent
          ),
      },
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
  {
    path: 'mchat/:token',
    loadComponent: () =>
      import('./public/mchat/mchat-form.component').then(m => m.MchatFormComponent),
  },
  { path: '**', redirectTo: 'login' },
];
