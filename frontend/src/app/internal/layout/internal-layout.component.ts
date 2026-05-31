import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-internal-layout',
  imports: [RouterOutlet, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="layout">
      <nav class="sidebar" aria-label="Menú principal">
        <h2 class="sidebar-title">MAG-TEA</h2>
        <ul role="list">
          <li><a routerLink="/internal/profesionales">Profesionales</a></li>
        </ul>
        <button (click)="logout()" class="logout-btn">Cerrar sesión</button>
      </nav>
      <main class="content">
        <router-outlet />
      </main>
    </div>
  `,
})
export class InternalLayoutComponent {
  private readonly authService = inject(AuthService);

  logout(): void {
    this.authService.logout();
  }
}
