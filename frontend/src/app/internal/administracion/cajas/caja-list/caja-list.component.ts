import { ChangeDetectionStrategy, Component, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, map, of, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { CajaService, CajaListParams } from '../../../../core/services/caja.service';
import { ToastService } from '../../../../core/services/toast.service';
import { CajaListItem } from '../../../../core/models/caja.model';
import { ListToolbarComponent, FilterGroup } from '../../../../shared/list-toolbar/list-toolbar.component';
import { ConfirmModalComponent } from '../../../../shared/confirm-modal/confirm-modal.component';
import { DataTableComponent, TableColumn } from '../../../../shared/data-table/data-table.component';
import { RowActionsComponent, RowAction } from '../../../../shared/row-actions/row-actions.component';
import { PaginatorComponent } from '../../../../shared/paginator/paginator.component';
import { IconComponent } from '../../../../shared/icon/icon.component';
import { SortState } from '../../../../shared/utils/sort.utils';
import { Crumb, PageHeaderComponent } from '../../../../shared/page-header/page-header.component';

@Component({
  selector: 'app-caja-list',
  imports: [RouterLink, ListToolbarComponent, ConfirmModalComponent, DataTableComponent,
            RowActionsComponent, PaginatorComponent, IconComponent, PageHeaderComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './caja-list.component.html',
  host: { '(window:keydown)': 'onGlobalKey($event)' },
})
export class CajaListComponent {
  private readonly service = inject(CajaService);
  private readonly router  = inject(Router);
  private readonly route   = inject(ActivatedRoute);
  private readonly toolbar = viewChild(ListToolbarComponent);
  private readonly toast   = inject(ToastService);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  private readonly params$ = new BehaviorSubject<CajaListParams>({
    page: 0, size: 50, sortBy: 'freezer', sortDir: 'asc',
  });

  private readonly response = toSignal(
    this.params$.pipe(
      switchMap(params => this.service.findAll(params).pipe(catchError(() => of(null))))
    ),
    { initialValue: null }
  );

  readonly cajas         = computed(() => this.response()?.content ?? []);
  readonly totalElements = computed(() => this.response()?.totalElements ?? 0);
  readonly totalPages    = computed(() => this.response()?.totalPages ?? 1);
  readonly currentPage   = computed(() => this.response()?.page ?? 0);

  search          = signal('');
  sortState       = signal<SortState>({ key: 'freezer', direction: 'asc' });
  deleting        = signal<number | null>(null);
  pendingDeleteId = signal<number | null>(null);

  readonly filterGroups: FilterGroup[] = [];

  readonly columns: TableColumn[] = [
    { label: 'FREEZER', sortKey: 'freezer' },
    { label: 'CAJÓN',   sortKey: 'cajon'   },
    { label: 'CAJA',    sortKey: 'numero'  },
  ];

  readonly emptyTitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'No hay cajas registradas'
      : 'Sin resultados'
  );
  readonly emptySubtitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'Creá la primera con el botón de arriba'
      : 'Probá con otro criterio de búsqueda'
  );

  onSearch(q: string): void {
    this.search.set(q);
    this.params$.next({ ...this.params$.value, page: 0, q: q || undefined });
  }

  onSortChange(sort: SortState): void {
    this.sortState.set(sort);
    this.params$.next({ ...this.params$.value, sortBy: sort.key, sortDir: sort.direction });
  }

  goToPage(page: number): void { this.params$.next({ ...this.params$.value, page }); }
  private reload(): void       { this.params$.next({ ...this.params$.value }); }

  private hasActiveSearch(): boolean {
    return !!this.params$.value.q;
  }

  getActionsFor(caja: CajaListItem): RowAction[] {
    return [
      { label: 'Editar',      style: 'primary', onClick: () => this.router.navigate(['/internal/cajas', caja.id, 'editar']) },
      { label: 'Dar de baja', style: 'danger',  disabled: this.deleting() === caja.id, onClick: () => this.requestDelete(caja.id) },
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
      next:  () => { this.deleting.set(null); this.toast.show('Caja dada de baja'); this.reload(); },
      error: () =>   this.deleting.set(null),
    });
  }

  onGlobalKey(event: KeyboardEvent): void {
    const tag = (event.target as HTMLElement).tagName;
    if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;
    if (event.key === '/') { event.preventDefault(); this.toolbar()?.focusSearch(); }
    if (event.altKey && event.key === 'n') {
      event.preventDefault();
      this.router.navigate(['/internal/cajas/nuevo']);
    }
  }
}
