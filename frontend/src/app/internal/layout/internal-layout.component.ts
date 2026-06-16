import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  LucideAngularModule,
  type LucideIconData,
  Inbox, Users, Droplets, Layers, Rat, Briefcase, ChartBar,
  LogOut, PanelLeftClose, PanelLeftOpen,
} from 'lucide-angular';
import { AuthService } from '../../core/services/auth.service';
import { Role, ROLE_LABELS } from '../../core/models/profesional.model';

interface NavItem {
  label: string;
  path: string;
  icon: LucideIconData;
  allowedRoles: Role[];
}

interface NavSection {
  label: string;
  items: NavItem[];
}

const NAV_SECTIONS: NavSection[] = [
  {
    label: 'Etapa Clínica',
    items: [
      {
        label: 'Bandeja',
        path: '/internal/bandeja',
        icon: Inbox,
        allowedRoles: ['SECRETARIA', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'INVESTIGADOR_PRINCIPAL'],
      },
      {
        label: 'Pacientes',
        path: '/internal/pacientes',
        icon: Users,
        allowedRoles: ['CUERPO_TECNICO', 'CUERPO_MEDICO', 'ROTANTE_CLINICA', 'ROTANTE_BASICA', 'INVESTIGADOR_PRINCIPAL'],
      },
    ],
  },
  {
    label: 'Etapa Básica',
    items: [
      {
        label: 'Sueros',
        path: '/internal/sueros',
        icon: Droplets,
        allowedRoles: ['CUERPO_TECNICO', 'ROTANTE_BASICA', 'INVESTIGADOR_PRINCIPAL'],
      },
      {
        label: 'Pools',
        path: '/internal/pools',
        icon: Layers,
        allowedRoles: ['CUERPO_TECNICO', 'ROTANTE_BASICA', 'INVESTIGADOR_PRINCIPAL'],
      },
      {
        label: 'Modelos Animales',
        path: '/internal/modelos-animales',
        icon: Rat,
        allowedRoles: ['CUERPO_TECNICO', 'ROTANTE_BASICA', 'INVESTIGADOR_PRINCIPAL'],
      },
    ],
  },
  {
    label: 'Gestión',
    items: [
      {
        label: 'Profesionales',
        path: '/internal/profesionales',
        icon: Briefcase,
        allowedRoles: ['INVESTIGADOR_PRINCIPAL'],
      },
      {
        label: 'Reportes',
        path: '/internal/reportes',
        icon: ChartBar,
        allowedRoles: ['INVESTIGADOR_PRINCIPAL'],
      },
    ],
  },
];

@Component({
  selector: 'app-internal-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './internal-layout.component.html',
})
export class InternalLayoutComponent {
  private readonly authService = inject(AuthService);

  user = this.authService.currentUser;

  readonly collapsed = signal(localStorage.getItem('sidebar-collapsed') === 'true');
  readonly toggleIcon = computed(() => this.collapsed() ? PanelLeftOpen : PanelLeftClose);
  readonly LogOutIcon = LogOut;

  constructor() {
    effect(() => {
      localStorage.setItem('sidebar-collapsed', String(this.collapsed()));
    });
  }

  toggleCollapsed(): void {
    this.collapsed.update(v => !v);
  }

  readonly roleLabel = computed(() => {
    const role = this.user()?.role;
    return role ? ROLE_LABELS[role] : '';
  });

  readonly visibleNavSections = computed(() => {
    const role = this.user()?.role;
    if (!role) return [];
    return NAV_SECTIONS
      .map(section => ({
        ...section,
        items: section.items.filter(item => item.allowedRoles.includes(role)),
      }))
      .filter(section => section.items.length > 0);
  });

  logout(): void {
    this.authService.logout();
  }
}
