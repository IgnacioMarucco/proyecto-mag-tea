import { ChangeDetectionStrategy, Component, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, map, of, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ModeloAnimalService, ModeloAnimalListParams } from '../../../core/services/modelo-animal.service';
import { ToastService } from '../../../core/services/toast.service';
import { ModeloAnimalListItem } from '../../../core/models/modelo-animal.model';
import { ListToolbarComponent, FilterGroup } from '../../../shared/list-toolbar/list-toolbar.component';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { DataTableComponent, TableColumn } from '../../../shared/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { RowActionsComponent, RowAction } from '../../../shared/row-actions/row-actions.component';
import { PaginatorComponent } from '../../../shared/paginator/paginator.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { SortState } from '../../../shared/sort.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-modelo-animal-list',
  imports: [RouterLink, ListToolbarComponent, ConfirmModalComponent, DataTableComponent,
            StatusBadgeComponent, RowActionsComponent, PaginatorComponent, IconComponent, PageHeaderComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './modelo-animal-list.component.html',
  host: { '(window:keydown)': 'onGlobalKey($event)' },
})
export class ModeloAnimalListComponent {
  private readonly service = inject(ModeloAnimalService);
  private readonly router  = inject(Router);
  private readonly route   = inject(ActivatedRoute);
  private readonly toolbar = viewChild(ListToolbarComponent);
  private readonly toast   = inject(ToastService);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  private readonly params$ = new BehaviorSubject<ModeloAnimalListParams>({
    page: 0, size: PAGE_SIZE, sortBy: 'createdAt', sortDir: 'desc',
  });

  private readonly response = toSignal(
    this.params$.pipe(
      switchMap(params => this.service.findAll(params).pipe(catchError(() => of(null))))
    ),
    { initialValue: null }
  );

  readonly modelosAnimales = computed(() => this.response()?.content ?? []);
  readonly totalElements   = computed(() => this.response()?.totalElements ?? 0);
  readonly totalPages      = computed(() => this.response()?.totalPages ?? 1);
  readonly currentPage     = computed(() => this.response()?.page ?? 0);

  search          = signal('');
  sortState       = signal<SortState>({ key: 'createdAt', direction: 'desc' });
  activeFilters   = signal<Record<string, string | string[]>>({});
  deleting        = signal<number | null>(null);
  pendingDeleteId = signal<number | null>(null);

  readonly filterGroups: FilterGroup[] = [
    {
      key: 'sexo',
      label: 'Sexo',
      multiSelect: false,
      options: [
        { key: 'MACHO',  label: 'Macho'  },
        { key: 'HEMBRA', label: 'Hembra' },
      ],
    },
    {
      key: 'soloAlertas',
      label: 'Alertas',
      multiSelect: false,
      options: [
        { key: 'true',  label: 'Solo con alertas hoy' },
        { key: 'false', label: 'Todos'                },
      ],
    },
  ];

  readonly columns: TableColumn[] = [
    { label: 'Identificador',  sortKey: 'identificador'   },
    { label: 'Pool / Rango',   hidden: 'sm'               },
    { label: 'Camada',         hidden: 'md'               },
    { label: 'Fecha nac.',     hidden: 'sm', sortKey: 'fechaNacimiento' },
    { label: 'Sexo',           hidden: 'sm'               },
    { label: 'Alertas'                                    },
  ];

  readonly rangoColors: Record<string, string> = {
    '1': 'badge-rango1',
    '2': 'badge-rango2',
    '3': 'badge-rango3',
  };
  readonly rangoLabels: Record<string, string> = {
    '1': 'Rango 1', '2': 'Rango 2', '3': 'Rango 3',
  };

  readonly sexoColors: Record<string, string>  = {
    MACHO:  'bg-primary-light text-primary',
    HEMBRA: 'bg-accent-light text-accent',
  };
  readonly sexoLabels: Record<string, string>  = { MACHO: 'Macho', HEMBRA: 'Hembra' };

  readonly emptyTitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'No hay modelos animales registrados'
      : 'Sin resultados'
  );
  readonly emptySubtitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'Registrá el primero con el botón de arriba'
      : 'Probá ajustando los filtros o la búsqueda'
  );

  formatDate(date: string): string {
    return new Date(date + 'T00:00:00').toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  }

  onSearch(q: string): void {
    this.search.set(q);
    this.params$.next({ ...this.params$.value, page: 0, q: q || undefined });
  }

  onFiltersChange(filters: Record<string, string | string[]>): void {
    this.activeFilters.set(filters);
    const sexo        = filters['sexo']        as string | undefined;
    const alertasVal  = filters['soloAlertas'] as string | undefined;
    this.params$.next({
      ...this.params$.value,
      page:        0,
      sexo:        (sexo === 'MACHO' || sexo === 'HEMBRA') ? sexo : undefined,
      soloAlertas: alertasVal === 'true' ? true : undefined,
    });
  }

  onSortChange(sort: SortState): void {
    this.sortState.set(sort);
    this.params$.next({ ...this.params$.value, sortBy: sort.key, sortDir: sort.direction });
  }

  goToPage(page: number): void { this.params$.next({ ...this.params$.value, page }); }
  private reload(): void       { this.params$.next({ ...this.params$.value }); }

  private hasActiveSearch(): boolean {
    const p = this.params$.value;
    return !!(p.q || p.sexo || p.soloAlertas);
  }

  getActionsFor(ma: ModeloAnimalListItem): RowAction[] {
    return [
      { label: 'Ver detalle', style: 'primary', onClick: () => this.router.navigate(['/internal/modelos-animales', ma.id]) },
      { label: 'Editar',      style: 'primary', onClick: () => this.router.navigate(['/internal/modelos-animales', ma.id, 'editar']) },
      { label: 'Dar de baja', style: 'danger',  disabled: this.deleting() === ma.id, onClick: () => this.requestDelete(ma.id) },
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
      next:  () => { this.deleting.set(null); this.toast.show('Modelo animal dado de baja'); this.reload(); },
      error: () =>   this.deleting.set(null),
    });
  }

  onGlobalKey(event: KeyboardEvent): void {
    const tag = (event.target as HTMLElement).tagName;
    if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;
    if (event.key === '/') { event.preventDefault(); this.toolbar()?.focusSearch(); }
    if ((event.ctrlKey || event.metaKey) && event.key === 'n') {
      event.preventDefault();
      this.router.navigate(['/internal/modelos-animales/nuevo']);
    }
  }
}
