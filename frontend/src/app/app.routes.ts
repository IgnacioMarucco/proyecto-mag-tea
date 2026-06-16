import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { appSubdomainGuard, publicSubdomainGuard } from './core/guards/subdomain.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [publicSubdomainGuard],
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
    canActivate: [appSubdomainGuard, authGuard],
    loadComponent: () =>
      import('./internal/layout/internal-layout.component').then(m => m.InternalLayoutComponent),
    children: [
      { path: '', redirectTo: 'bandeja', pathMatch: 'full' },
      {
        path: 'bandeja',
        data: { crumbs: [{ label: 'Etapa Clínica' }, { label: 'Bandeja de formularios' }] },
        loadComponent: () =>
          import('./internal/formularios-interes/bandeja/bandeja.component').then(
            m => m.BandejaComponent
          ),
      },
      {
        path: 'pacientes',
        data: { crumbs: [{ label: 'Etapa Clínica' }, { label: 'Pacientes' }] },
        loadComponent: () =>
          import('./internal/pacientes/paciente-list/paciente-list.component').then(
            m => m.PacienteListComponent
          ),
      },
      {
        path: 'pacientes/nuevo',
        data: { crumbs: [{ label: 'Etapa Clínica' }, { label: 'Pacientes', path: '/internal/pacientes' }, { label: 'Registrar paciente' }] },
        loadComponent: () =>
          import('./internal/pacientes/paciente-form/paciente-form.component').then(
            m => m.PacienteFormComponent
          ),
      },
      {
        path: 'pacientes/:id/editar',
        data: { crumbs: [{ label: 'Etapa Clínica' }, { label: 'Pacientes', path: '/internal/pacientes' }, { label: 'Editar paciente' }] },
        loadComponent: () =>
          import('./internal/pacientes/paciente-form/paciente-form.component').then(
            m => m.PacienteFormComponent
          ),
      },
      {
        path: 'pacientes/:id',
        data: { crumbs: [{ label: 'Etapa Clínica' }, { label: 'Pacientes', path: '/internal/pacientes' }, { label: '' }] },
        loadComponent: () =>
          import('./internal/pacientes/paciente-detail/paciente-detail.component').then(
            m => m.PacienteDetailComponent
          ),
      },
      {
        path: 'profesionales',
        data: { crumbs: [{ label: 'Gestión' }, { label: 'Profesionales' }] },
        loadComponent: () =>
          import('./internal/profesionales/profesional-list/profesional-list.component').then(
            m => m.ProfesionalListComponent
          ),
      },
      {
        path: 'profesionales/nuevo',
        data: { crumbs: [{ label: 'Gestión' }, { label: 'Profesionales', path: '/internal/profesionales' }, { label: 'Nuevo profesional' }] },
        loadComponent: () =>
          import('./internal/profesionales/profesional-form/profesional-form.component').then(
            m => m.ProfesionalFormComponent
          ),
      },
      {
        path: 'profesionales/:id/editar',
        data: { crumbs: [{ label: 'Gestión' }, { label: 'Profesionales', path: '/internal/profesionales' }, { label: 'Editar profesional' }] },
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
  {
    path: 'donacion/resultado',
    loadComponent: () =>
      import('./public/donacion/donacion-resultado/donacion-resultado.component').then(
        m => m.DonacionResultadoComponent
      ),
  },
  { path: '**', redirectTo: 'login' },
];
