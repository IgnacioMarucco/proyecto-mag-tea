import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { BehaviorSubject, switchMap, catchError, of } from 'rxjs';
import { ProfesionalService } from '../../../core/services/profesional.service';
import { ROLE_LABELS, Role } from '../../../core/models/profesional.model';
import { ListToolbarComponent, ToolbarFilter } from '../../../shared/list-toolbar/list-toolbar.component';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-profesional-list',
  imports: [RouterLink, ListToolbarComponent, ConfirmModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profesional-list.component.html',
})
export class ProfesionalListComponent {
  private readonly service = inject(ProfesionalService);
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  private readonly todos = toSignal(
    this.refresh$.pipe(
      switchMap(() => this.service.findAll().pipe(catchError(() => of([]))))
    ),
    { initialValue: [] }
  );

  readonly roleLabels = ROLE_LABELS;

  search      = signal('');
  activeFilter = signal<string>('todos');
  deleting    = signal<number | null>(null);
  pendingDeleteId = signal<number | null>(null);

  readonly filters: ToolbarFilter[] = [
    { key: 'todos', label: 'Todos' },
    ...Object.entries(ROLE_LABELS).map(([key, label]) => ({ key, label })),
  ];

  readonly profesionales = computed(() => {
    const q    = this.search().toLowerCase().trim();
    const rol  = this.activeFilter();
    return this.todos().filter(p => {
      const matchRol    = rol === 'todos' || p.role === (rol as Role);
      const matchSearch = !q ||
        p.apellido.toLowerCase().includes(q) ||
        p.nombre.toLowerCase().includes(q) ||
        p.email.toLowerCase().includes(q);
      return matchRol && matchSearch;
    });
  });

  requestDelete(id: number): void {
    this.pendingDeleteId.set(id);
  }

  cancelDelete(): void {
    this.pendingDeleteId.set(null);
  }

  confirmDelete(): void {
    const id = this.pendingDeleteId();
    if (id === null) return;
    this.pendingDeleteId.set(null);
    this.deleting.set(id);
    this.service.delete(id).subscribe({
      next: () => { this.deleting.set(null); this.refresh$.next(); },
      error: () => this.deleting.set(null),
    });
  }
}
