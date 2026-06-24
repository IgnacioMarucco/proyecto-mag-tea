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
        path: 'pacientes/:codigo/editar',
        data: { crumbs: [{ label: 'Etapa Clínica' }, { label: 'Pacientes', path: '/internal/pacientes' }, { label: 'Editar paciente' }] },
        loadComponent: () =>
          import('./internal/pacientes/paciente-form/paciente-form.component').then(
            m => m.PacienteFormComponent
          ),
      },
      {
        path: 'pacientes/:codigo',
        data: { crumbs: [{ label: 'Etapa Clínica' }, { label: 'Pacientes', path: '/internal/pacientes' }, { label: '' }] },
        loadComponent: () =>
          import('./internal/pacientes/paciente-detail/paciente-detail.component').then(
            m => m.PacienteDetailComponent
          ),
      },
      {
        path: 'sueros',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Sueros' }] },
        loadComponent: () =>
          import('./internal/sueros/suero-list/suero-list.component').then(
            m => m.SueroListComponent
          ),
      },
      {
        path: 'sueros/nuevo',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Sueros', path: '/internal/sueros' }, { label: 'Registrar suero' }] },
        loadComponent: () =>
          import('./internal/sueros/suero-form/suero-form.component').then(
            m => m.SueroFormComponent
          ),
      },
      {
        path: 'sueros/:codigo/editar',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Sueros', path: '/internal/sueros' }, { label: 'Editar suero' }] },
        loadComponent: () =>
          import('./internal/sueros/suero-form/suero-form.component').then(
            m => m.SueroFormComponent
          ),
      },
      {
        path: 'sueros/:codigo',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Sueros', path: '/internal/sueros' }, { label: '' }] },
        loadComponent: () =>
          import('./internal/sueros/suero-detail/suero-detail.component').then(
            m => m.SueroDetailComponent
          ),
      },
      {
        path: 'pools',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Pools' }] },
        loadComponent: () =>
          import('./internal/pools/pool-list/pool-list.component').then(
            m => m.PoolListComponent
          ),
      },
      {
        path: 'pools/nuevo',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Pools', path: '/internal/pools' }, { label: 'Registrar pool' }] },
        loadComponent: () =>
          import('./internal/pools/pool-form/pool-form.component').then(
            m => m.PoolFormComponent
          ),
      },
      {
        path: 'pools/:codigo/editar',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Pools', path: '/internal/pools' }, { label: 'Editar pool' }] },
        loadComponent: () =>
          import('./internal/pools/pool-form/pool-form.component').then(
            m => m.PoolFormComponent
          ),
      },
      {
        path: 'pools/:codigo',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Pools', path: '/internal/pools' }, { label: '' }] },
        loadComponent: () =>
          import('./internal/pools/pool-detail/pool-detail.component').then(
            m => m.PoolDetailComponent
          ),
      },
      {
        path: 'modelos-animales',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Modelos Animales' }] },
        loadComponent: () =>
          import('./internal/modelos-animales/modelo-animal-list/modelo-animal-list.component').then(
            m => m.ModeloAnimalListComponent
          ),
      },
      {
        path: 'modelos-animales/nuevo',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Modelos Animales', path: '/internal/modelos-animales' }, { label: 'Registrar ratón' }] },
        loadComponent: () =>
          import('./internal/modelos-animales/modelo-animal-form/modelo-animal-form.component').then(
            m => m.ModeloAnimalFormComponent
          ),
      },
      {
        path: 'modelos-animales/:identificador/editar',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Modelos Animales', path: '/internal/modelos-animales' }, { label: 'Editar' }] },
        loadComponent: () =>
          import('./internal/modelos-animales/modelo-animal-form/modelo-animal-form.component').then(
            m => m.ModeloAnimalFormComponent
          ),
      },
      {
        path: 'modelos-animales/:identificador',
        data: { crumbs: [{ label: 'Etapa Básica' }, { label: 'Modelos Animales', path: '/internal/modelos-animales' }, { label: '' }] },
        loadComponent: () =>
          import('./internal/modelos-animales/modelo-animal-detail/modelo-animal-detail.component').then(
            m => m.ModeloAnimalDetailComponent
          ),
      },
      {
        path: 'cajas',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Cajas' }] },
        loadComponent: () =>
          import('./internal/administracion/cajas/caja-list/caja-list.component').then(
            m => m.CajaListComponent
          ),
      },
      {
        path: 'cajas/nuevo',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Cajas', path: '/internal/cajas' }, { label: 'Registrar caja' }] },
        loadComponent: () =>
          import('./internal/administracion/cajas/caja-form/caja-form.component').then(
            m => m.CajaFormComponent
          ),
      },
      {
        path: 'cajas/:id/editar',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Cajas', path: '/internal/cajas' }, { label: 'Editar caja' }] },
        loadComponent: () =>
          import('./internal/administracion/cajas/caja-form/caja-form.component').then(
            m => m.CajaFormComponent
          ),
      },
      {
        path: 'camadas',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Camadas' }] },
        loadComponent: () =>
          import('./internal/administracion/camadas/camada-list/camada-list.component').then(
            m => m.CamadaListComponent
          ),
      },
      {
        path: 'camadas/nuevo',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Camadas', path: '/internal/camadas' }, { label: 'Registrar camada' }] },
        loadComponent: () =>
          import('./internal/administracion/camadas/camada-form/camada-form.component').then(
            m => m.CamadaFormComponent
          ),
      },
      {
        path: 'camadas/:id/editar',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Camadas', path: '/internal/camadas' }, { label: 'Editar camada' }] },
        loadComponent: () =>
          import('./internal/administracion/camadas/camada-form/camada-form.component').then(
            m => m.CamadaFormComponent
          ),
      },
      {
        path: 'profesionales',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Profesionales' }] },
        loadComponent: () =>
          import('./internal/profesionales/profesional-list/profesional-list.component').then(
            m => m.ProfesionalListComponent
          ),
      },
      {
        path: 'profesionales/nuevo',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Profesionales', path: '/internal/profesionales' }, { label: 'Registrar profesional' }] },
        loadComponent: () =>
          import('./internal/profesionales/profesional-form/profesional-form.component').then(
            m => m.ProfesionalFormComponent
          ),
      },
      {
        path: 'profesionales/:id/editar',
        data: { crumbs: [{ label: 'Administración' }, { label: 'Profesionales', path: '/internal/profesionales' }, { label: 'Editar profesional' }] },
        loadComponent: () =>
          import('./internal/profesionales/profesional-form/profesional-form.component').then(
            m => m.ProfesionalFormComponent
          ),
      },
      {
        path: 'reportes',
        data: { crumbs: [{ label: 'Reportes' }] },
        loadComponent: () =>
          import('./internal/reportes/dashboard/reportes-dashboard.component').then(
            m => m.ReportesDashboardComponent
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
  { path: '**', redirectTo: '' },
];
