import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { Role, ROLE_LABELS } from '../../core/models/profesional.model';

interface NavItem {
  label: string;
  path: string;
  allowedRoles: Role[];
}

const NAV_ITEMS: NavItem[] = [
  {
    label: 'Profesionales',
    path: '/internal/profesionales',
    allowedRoles: ['INVESTIGADOR_PRINCIPAL'],
  },
  {
    label: 'Bandeja',
    path: '/internal/bandeja',
    allowedRoles: ['SECRETARIA', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL'],
  },
  {
    label: 'Pacientes',
    path: '/internal/pacientes',
    allowedRoles: ['CUERPO_TECNICO', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'ROTANTE_BASICA', 'INVESTIGADOR_PRINCIPAL'],
  },
  {
    label: 'Sueros',
    path: '/internal/sueros',
    allowedRoles: ['CUERPO_TECNICO', 'ROTANTE_BASICA', 'INVESTIGADOR_PRINCIPAL'],
  },
  {
    label: 'Pools',
    path: '/internal/pools',
    allowedRoles: ['CUERPO_TECNICO', 'ROTANTE_BASICA', 'INVESTIGADOR_PRINCIPAL'],
  },
  {
    label: 'Modelos Animales',
    path: '/internal/modelos-animales',
    allowedRoles: ['CUERPO_TECNICO', 'ROTANTE_BASICA', 'INVESTIGADOR_PRINCIPAL'],
  },
  {
    label: 'Reportes',
    path: '/internal/reportes',
    allowedRoles: ['INVESTIGADOR_PRINCIPAL'],
  },
];

@Component({
  selector: 'app-internal-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './internal-layout.component.html',
})
export class InternalLayoutComponent {
  private readonly authService = inject(AuthService);

  user = this.authService.currentUser;

  roleLabel = computed(() => {
    const role = this.user()?.role;
    return role ? ROLE_LABELS[role] : '';
  });

  visibleNavItems = computed(() => {
    const role = this.user()?.role;
    if (!role) return [];
    return NAV_ITEMS.filter(item => item.allowedRoles.includes(role));
  });

  logout(): void {
    this.authService.logout();
  }
}
