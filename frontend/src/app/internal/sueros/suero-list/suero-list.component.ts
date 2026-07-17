import { ChangeDetectionStrategy, Component, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, combineLatest, map, of, switchMap, tap, timer } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { SueroService, SueroListParams } from '../../../core/services/suero.service';
import { ToastService } from '../../../core/services/toast.service';
import { SueroListItem, SueroUso } from '../../../core/models/suero.model';
import { ListToolbarComponent, FilterGroup } from '../../../shared/list-toolbar/list-toolbar.component';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { DataTableComponent, TableColumn } from '../../../shared/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { RowActionsComponent, RowAction } from '../../../shared/row-actions/row-actions.component';
import { PaginatorComponent } from '../../../shared/paginator/paginator.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { SortState } from '../../../shared/utils/sort.utils';
import { hasActiveSearch } from '../../../shared/utils/list.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { VolumeBarComponent } from '../../../shared/volume-bar/volume-bar.component';
import { RANGO_COLORS, RANGO_LABELS, USO_COLORS, USO_LABELS } from '../../../shared/utils/btu.utils';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-suero-list',
  imports: [RouterLink, ListToolbarComponent, ConfirmModalComponent, DataTableComponent,
            StatusBadgeComponent, RowActionsComponent, PaginatorComponent, IconComponent, PageHeaderComponent, FechaPipe, VolumeBarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './suero-list.component.html',
  host: { '(window:keydown)': 'onGlobalKey($event)' },
})
export class SueroListComponent {
  private readonly service = inject(SueroService);
  private readonly router  = inject(Router);
  private readonly route   = inject(ActivatedRoute);
  private readonly toolbar = viewChild(ListToolbarComponent);
  private readonly toast   = inject(ToastService);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  readonly codigoPacienteFiltro = signal<string | null>(
    this.route.snapshot.queryParamMap.get('codigoPaciente') || null
  );

  private readonly params$ = new BehaviorSubject<SueroListParams>({
    page: 0, size: PAGE_SIZE, sortBy: 'fechaExtraccion', sortDir: 'desc',
    codigoPaciente: this.route.snapshot.queryParamMap.get('codigoPaciente') || undefined,
  });

  error   = signal(false);
  loading = signal(false);

  private readonly response = toSignal(
    combineLatest([this.params$, timer(0, 60_000)]).pipe(
      tap(() => { this.error.set(false); this.loading.set(true); }),
      switchMap(([params]) => this.service.findAll(params).pipe(
        tap(() => this.loading.set(false)),
        catchError(() => { this.error.set(true); this.loading.set(false); return of(null); })
      ))
    ),
    { initialValue: null }
  );

  readonly sueros        = computed(() => this.response()?.content ?? []);
  readonly totalElements = computed(() => this.response()?.totalElements ?? 0);
  readonly totalPages    = computed(() => this.response()?.totalPages ?? 1);
  readonly currentPage   = computed(() => this.response()?.page ?? 0);

  search          = signal('');
  sortState       = signal<SortState>({ key: 'fechaExtraccion', direction: 'desc' });
  activeFilters   = signal<Record<string, string | string[]>>({});
  deleting        = signal<number | null>(null);
  pendingDeleteId = signal<number | null>(null);

  readonly filterGroups: FilterGroup[] = [
    {
      key: 'uso',
      label: 'Tipo',
      multiSelect: true,
      options: [
        { key: 'CONTROL',  label: 'Control'       },
        { key: 'PROBLEMA', label: 'Caso problema' },
      ],
    },
    {
      key: 'rango',
      label: 'Rango',
      multiSelect: true,
      options: [
        { key: '0', label: 'Rango 0'            },
        { key: '1', label: 'Rango 1'            },
        { key: '2', label: 'Rango 2'            },
        { key: '3', label: 'Rango 3'            },
      ],
    },
  ];

  readonly columns: TableColumn[] = [
    { label: 'SUERO',       sortKey: 'fechaExtraccion'  },
    { label: 'TIPO'                                     },
    { label: 'RANGO / BTU', sortKey: 'valorAnticuerpos' },
    { label: 'CANT. REST.'                              },
  ];

  readonly rangoColors = RANGO_COLORS;
  readonly rangoLabels = RANGO_LABELS;
  readonly usoColors   = USO_COLORS;
  readonly usoLabels   = USO_LABELS;

  readonly emptyTitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'No hay sueros registrados'
      : 'Sin resultados'
  );
  readonly emptySubtitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'Registrá el primero con el botón de arriba'
      : 'Probá ajustando los filtros o la búsqueda'
  );

  readonly canCreatePool = computed(() => {
    const usos   = this.activeFilters()['uso']   as string[] | undefined;
    const rangos = this.activeFilters()['rango'] as string[] | undefined;
    return usos?.length === 1 && rangos?.length === 1;
  });

  formatBtu(val: number | null | undefined): string {
    return val != null ? val.toLocaleString('es-AR') : '—';
  }

  onSearch(q: string): void {
    this.search.set(q);
    this.params$.next({ ...this.params$.value, page: 0, q: q || undefined });
  }

  onFiltersChange(filters: Record<string, string | string[]>): void {
    this.activeFilters.set(filters);
    const rangos = Array.isArray(filters['rango']) ? filters['rango'] : [];
    const usos   = Array.isArray(filters['uso'])   ? filters['uso']   : [];
    this.params$.next({
      ...this.params$.value,
      page:  0,
      rango: rangos.length ? rangos            : undefined,
      uso:   usos.length   ? usos as SueroUso[] : undefined,
    });
  }

  onSortChange(sort: SortState): void {
    this.sortState.set(sort);
    this.params$.next({ ...this.params$.value, sortBy: sort.key, sortDir: sort.direction });
  }

  goToPage(page: number): void { this.params$.next({ ...this.params$.value, page }); }
  private reload(): void       { this.params$.next({ ...this.params$.value }); }

  private hasActiveSearch(): boolean {
    return hasActiveSearch(this.search, this.activeFilters, this.filterGroups);
  }

  getActionsFor(s: SueroListItem): RowAction[] {
    return [
      { label: 'Detalles',    onClick: () => this.router.navigate(['/internal/sueros', s.codigoNumerico]) },
      { label: 'Editar',      style: 'primary', onClick: () => this.router.navigate(['/internal/sueros', s.codigoNumerico, 'editar']) },
      { label: 'Dar de baja', style: 'danger',  disabled: this.deleting() === s.id, onClick: () => this.requestDelete(s.id) },
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
      next:  () => { this.deleting.set(null); this.toast.show('Suero dado de baja'); this.reload(); },
      error: () =>   this.deleting.set(null),
    });
  }

  crearPool(): void {
    const usos   = this.activeFilters()['uso']   as string[];
    const rangos = this.activeFilters()['rango'] as string[];
    this.router.navigate(['/internal/pools/nuevo'], {
      queryParams: { uso: usos[0], rango: rangos[0] },
    });
  }

  onGlobalKey(event: KeyboardEvent): void {
    const tag = (event.target as HTMLElement).tagName;
    if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;
    if (event.key === '/') { event.preventDefault(); this.toolbar()?.focusSearch(); }
    if (event.altKey && event.key === 'n') {
      event.preventDefault();
      this.router.navigate(['/internal/sueros/nuevo']);
    }
  }
}
