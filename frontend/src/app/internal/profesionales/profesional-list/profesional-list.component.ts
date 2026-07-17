import { ChangeDetectionStrategy, Component, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { BehaviorSubject, catchError, map, of, switchMap } from 'rxjs';
import { ProfesionalService } from '../../../core/services/profesional.service';
import { ToastService } from '../../../core/services/toast.service';
import { ROLE_LABELS, Role, ProfesionalResponse } from '../../../core/models/profesional.model';
import { ListToolbarComponent, FilterGroup } from '../../../shared/list-toolbar/list-toolbar.component';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { DataTableComponent, TableColumn } from '../../../shared/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { RowActionsComponent, RowAction } from '../../../shared/row-actions/row-actions.component';
import { PaginatorComponent } from '../../../shared/paginator/paginator.component';
import { SortState } from '../../../shared/utils/sort.utils';
import { IconComponent } from '../../../shared/icon/icon.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';

@Component({
  selector: 'app-profesional-list',
  imports: [RouterLink, ListToolbarComponent, ConfirmModalComponent, DataTableComponent, StatusBadgeComponent, RowActionsComponent, PaginatorComponent, IconComponent, PageHeaderComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profesional-list.component.html',
  host: { '(window:keydown)': 'onGlobalKey($event)' },
})
export class ProfesionalListComponent {
  private readonly service = inject(ProfesionalService);
  private readonly router  = inject(Router);
  private readonly route   = inject(ActivatedRoute);
  private readonly toolbar = viewChild(ListToolbarComponent);
  private readonly toast   = inject(ToastService);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  private readonly params$ = new BehaviorSubject<{
    page: number; size: number; q?: string; rol?: string; sortBy: string; sortDir: string;
  }>({ page: 0, size: 50, sortBy: 'apellido', sortDir: 'asc' });

  private readonly response = toSignal(
    this.params$.pipe(
      switchMap(params => this.service.findAll(params).pipe(catchError(() => of(null))))
    ),
    { initialValue: null }
  );

  readonly profesionales = computed(() => this.response()?.content ?? []);
  readonly totalElements = computed(() => this.response()?.totalElements ?? 0);
  readonly totalPages    = computed(() => this.response()?.totalPages ?? 1);
  readonly currentPage   = computed(() => this.response()?.page ?? 0);

  readonly roleLabels = ROLE_LABELS;

  search          = signal('');
  activeFilters   = signal<Record<string, string | string[]>>({ rol: 'todos' });
  sortState       = signal<SortState>({ key: 'apellido', direction: 'asc' });
  deleting        = signal<number | null>(null);
  pendingDeleteId = signal<number | null>(null);

  readonly filterGroups: FilterGroup[] = [
    {
      key: 'rol',
      label: 'Rol',
      options: [
        { key: 'todos', label: 'Todos' },
        ...Object.entries(ROLE_LABELS).map(([key, label]) => ({ key, label })),
      ],
    },
  ];

  readonly columns: TableColumn[] = [
    { label: 'APELLIDO, NOMBRE', sortKey: 'apellido' },
    { label: 'EMAIL' },
    { label: 'TELEFONO' },
    { label: 'ROL' },
  ];

  readonly rolColors: Record<string, string> = {
    CUERPO_MEDICO:          'bg-primary-light text-primary',
    CUERPO_TECNICO:         'bg-accent-light text-accent',
    INVESTIGADOR_PRINCIPAL: 'bg-warning/10 text-warning',
  };

  readonly emptyTitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'No hay profesionales registrados'
      : 'Sin resultados'
  );

  readonly emptySubtitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'Creá el primero con el botón de arriba'
      : 'Probá con otro criterio de búsqueda'
  );

  onSearch(q: string): void {
    this.search.set(q);
    this.params$.next({ ...this.params$.value, page: 0, q: q || undefined });
  }

  onFiltersChange(filters: Record<string, string | string[]>): void {
    this.activeFilters.set(filters);
    const rol = filters['rol'] as string | undefined;
    this.params$.next({
      ...this.params$.value,
      page: 0,
      rol: rol && rol !== 'todos' ? rol : undefined,
    });
  }

  onSortChange(sort: SortState): void {
    this.sortState.set(sort);
    this.params$.next({ ...this.params$.value, sortBy: sort.key, sortDir: sort.direction });
  }

  goToPage(page: number): void {
    this.params$.next({ ...this.params$.value, page });
  }

  private reload(): void {
    this.params$.next({ ...this.params$.value });
  }

  private hasActiveSearch(): boolean {
    const p = this.params$.value;
    return !!(p.q || p.rol);
  }

  getActionsFor(p: ProfesionalResponse): RowAction[] {
    return [
      { label: 'Editar',      style: 'primary', onClick: () => this.router.navigate(['/internal/profesionales', p.id, 'editar']) },
      { label: 'Dar de baja', style: 'danger',  disabled: this.deleting() === p.id, onClick: () => this.requestDelete(p.id) },
    ];
  }

  requestDelete(id: number): void { this.pendingDeleteId.set(id); }
  cancelDelete(): void            { this.pendingDeleteId.set(null); }

  confirmDelete(): void {
    const id = this.pendingDeleteId();
    if (id === null) return;
    this.pendingDeleteId.set(null);
    this.deleting.set(id);
    this.service.delete(id).subscribe({
      next:  () => { this.deleting.set(null); this.toast.show('Profesional dado de baja'); this.reload(); },
      error: () =>   this.deleting.set(null),
    });
  }

  onGlobalKey(event: KeyboardEvent): void {
    const tag = (event.target as HTMLElement).tagName;
    if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;
    if (event.key === '/') { event.preventDefault(); this.toolbar()?.focusSearch(); }
    if (event.altKey && event.key === 'n') {
      event.preventDefault();
      this.router.navigate(['/internal/profesionales/nuevo']);
    }
  }
}
