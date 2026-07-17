import { ChangeDetectionStrategy, Component, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, combineLatest, map, of, switchMap, tap, timer } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { PoolService, PoolListParams } from '../../../core/services/pool.service';
import { ToastService } from '../../../core/services/toast.service';
import { SueroService } from '../../../core/services/suero.service';
import { PoolListItem } from '../../../core/models/pool.model';
import { SueroDisponibilidad } from '../../../core/models/suero.model';
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
import { MlPipe } from '../../../core/pipes/ml.pipe';
import { VolumeBarComponent } from '../../../shared/volume-bar/volume-bar.component';
import { RANGO_COLORS, RANGO_LABELS, USO_COLORS, USO_LABELS } from '../../../shared/utils/btu.utils';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-pool-list',
  imports: [RouterLink, ListToolbarComponent, ConfirmModalComponent, DataTableComponent,
            StatusBadgeComponent, RowActionsComponent, PaginatorComponent, IconComponent, PageHeaderComponent, FechaPipe, MlPipe, VolumeBarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pool-list.component.html',
  host: { '(window:keydown)': 'onGlobalKey($event)' },
})
export class PoolListComponent {
  private readonly service      = inject(PoolService);
  private readonly sueroService = inject(SueroService);
  private readonly router       = inject(Router);
  private readonly route        = inject(ActivatedRoute);
  private readonly toolbar      = viewChild(ListToolbarComponent);
  private readonly toast        = inject(ToastService);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  readonly disponibilidad = toSignal(
    this.sueroService.getDisponibilidad().pipe(catchError(() => of([] as SueroDisponibilidad[]))),
    { initialValue: [] as SueroDisponibilidad[] }
  );

  readonly disponibilidadMatrix = computed(() => {
    const d = this.disponibilidad();
    return (['PROBLEMA', 'CONTROL'] as const).map(uso => ({
      uso,
      label: uso === 'PROBLEMA' ? 'Problema' : 'Control',
      rangos: [0, 1, 2, 3].map(rango => {
        const found = d.find(x => x.uso === uso && x.rango === rango);
        return found ?? { uso, rango, cantidadSueros: 0, mlDisponibles: 0, ratonesPosibles: 0 };
      }),
    }));
  });

  private readonly params$ = new BehaviorSubject<PoolListParams>({
    page: 0, size: PAGE_SIZE, sortBy: 'fechaCreacion', sortDir: 'desc',
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

  readonly pools         = computed(() => this.response()?.content ?? []);
  readonly totalElements = computed(() => this.response()?.totalElements ?? 0);
  readonly totalPages    = computed(() => this.response()?.totalPages ?? 1);
  readonly currentPage   = computed(() => this.response()?.page ?? 0);

  search          = signal('');
  sortState       = signal<SortState>({ key: 'fechaCreacion', direction: 'desc' });
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
        { key: '0', label: 'Rango 0' },
        { key: '1', label: 'Rango 1' },
        { key: '2', label: 'Rango 2' },
        { key: '3', label: 'Rango 3' },
      ],
    },
  ];

  readonly columns: TableColumn[] = [
    { label: 'POOL',          sortKey: 'fechaCreacion'  },
    { label: 'TIPO',          hidden: 'sm'              },
    { label: 'RANGO',         sortKey: 'rango'          },
    { label: 'CANT. REST.'                                        },
    { label: 'APORTES', hidden: 'md', align: 'center'            },
    { label: 'RATONES', hidden: 'sm', align: 'center'            },
  ];

  readonly rangoColors = RANGO_COLORS;
  readonly rangoLabels = RANGO_LABELS;
  readonly usoColors   = USO_COLORS;
  readonly usoLabels   = USO_LABELS;

  readonly canCreateRaton = computed(() => {
    const usos   = this.activeFilters()['uso']   as string[] | undefined;
    const rangos = this.activeFilters()['rango'] as string[] | undefined;
    return usos?.length === 1 && rangos?.length === 1;
  });

  crearRaton(): void {
    if (!this.canCreateRaton()) return;
    const usos   = this.activeFilters()['uso']   as string[];
    const rangos = this.activeFilters()['rango'] as string[];
    this.router.navigate(['/internal/modelos-animales/nuevo'], {
      queryParams: { uso: usos[0], rango: rangos[0] },
    });
  }

  readonly emptyTitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'No hay pools registrados'
      : 'Sin resultados'
  );
  readonly emptySubtitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'Creá el primero con el botón de arriba'
      : 'Probá ajustando los filtros o la búsqueda'
  );

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
      page:   0,
      rangos: rangos.length ? rangos : undefined,
      usos:   usos.length   ? usos   : undefined,
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

  getActionsFor(pool: PoolListItem): RowAction[] {
    return [
      { label: 'Detalles', onClick: () => this.router.navigate(['/internal/pools', pool.codigo]) },
      { label: 'Editar',      style: 'primary', onClick: () => this.router.navigate(['/internal/pools', pool.codigo, 'editar']) },
      { label: 'Dar de baja', style: 'danger',  disabled: this.deleting() === pool.id, onClick: () => this.requestDelete(pool.id) },
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
      next:  () => { this.deleting.set(null); this.toast.show('Pool dado de baja'); this.reload(); },
      error: () =>   this.deleting.set(null),
    });
  }

  onGlobalKey(event: KeyboardEvent): void {
    const tag = (event.target as HTMLElement).tagName;
    if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;
    if (event.key === '/') { event.preventDefault(); this.toolbar()?.focusSearch(); }
    if (event.altKey && event.key === 'n') {
      event.preventDefault();
      this.router.navigate(['/internal/pools/nuevo']);
    }
  }
}
