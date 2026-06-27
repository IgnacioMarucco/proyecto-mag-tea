import { ChangeDetectionStrategy, Component, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, map, of, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ModeloAnimalService, ModeloAnimalListParams } from '../../../core/services/modelo-animal.service';
import { ToastService } from '../../../core/services/toast.service';
import { ModeloAnimalListItem, EstadoProtocolo } from '../../../core/models/modelo-animal.model';
import { ListToolbarComponent, FilterGroup } from '../../../shared/list-toolbar/list-toolbar.component';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { DataTableComponent, TableColumn } from '../../../shared/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { RowActionsComponent, RowAction } from '../../../shared/row-actions/row-actions.component';
import { PaginatorComponent } from '../../../shared/paginator/paginator.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { SortState } from '../../../shared/utils/sort.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { RANGO_COLORS, RANGO_LABELS, USO_COLORS, USO_LABELS } from '../../../shared/utils/btu.utils';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-modelo-animal-list',
  imports: [RouterLink, ListToolbarComponent, ConfirmModalComponent, DataTableComponent,
            StatusBadgeComponent, RowActionsComponent, PaginatorComponent, IconComponent, PageHeaderComponent, FechaPipe],
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
    page: 0, size: PAGE_SIZE, sortBy: 'fechaProximoEvento', sortDir: 'asc',
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
  sortState       = signal<SortState>({ key: 'fechaProximoEvento', direction: 'asc' });
  activeFilters   = signal<Record<string, string | string[]>>({});
  deleting        = signal<number | null>(null);
  pendingDeleteId = signal<number | null>(null);

  readonly filterGroups: FilterGroup[] = [
    {
      key: 'uso',
      label: 'Tipo',
      multiSelect: false,
      options: [
        { key: 'PROBLEMA', label: 'Caso Problema' },
        { key: 'CONTROL',  label: 'Caso Control'  },
      ],
    },
    {
      key: 'rango',
      label: 'Rango',
      multiSelect: false,
      options: [
        { key: '1', label: 'Rango 1' },
        { key: '2', label: 'Rango 2' },
        { key: '3', label: 'Rango 3' },
      ],
    },
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
    {
      key: 'estado',
      label: 'Estado',
      multiSelect: false,
      options: [
        { key: 'PENDIENTE_INOCULACION',    label: 'Pendiente inoculación'   },
        { key: 'INOCULACION_EN_CURSO',     label: 'Inoculación en curso'    },
        { key: 'PENDIENTE_VOCALIZACIONES', label: 'Pendiente vocalizaciones' },
        { key: 'PENDIENTE_TRES_CAMARAS',   label: 'Pendiente 3 cámaras'     },
        { key: 'PENDIENTE_MICROSCOPIA',    label: 'Pendiente microscopía'   },
        { key: 'COMPLETO',                 label: 'Completo'                },
      ],
    },
  ];

  readonly columns: TableColumn[] = [
    { label: 'CÓDIGO',          sortKey: 'identificador'      },
    { label: 'CAMADA',          sortKey: 'camadaNombre', hidden: 'sm' },
    { label: 'TIPO',            hidden: 'sm'              },
    { label: 'RANGO',           hidden: 'sm'              },
    { label: 'ESTADO',          hidden: 'md'              },
    { label: 'PRÓXIMO EVENTO',  sortKey: 'fechaProximoEvento' },
  ];

  readonly rangoColors = RANGO_COLORS;
  readonly rangoLabels = RANGO_LABELS;
  readonly usoColors   = USO_COLORS;
  readonly usoLabels   = USO_LABELS;

  readonly estadoColors: Record<EstadoProtocolo, string> = {
    PENDIENTE_INOCULACION:    'bg-background text-text-muted border border-border',
    INOCULACION_EN_CURSO:     'bg-blue-50 text-blue-700 border border-blue-200',
    PENDIENTE_VOCALIZACIONES: 'bg-yellow-50 text-yellow-700 border border-yellow-200',
    PENDIENTE_TRES_CAMARAS:   'bg-orange-50 text-orange-700 border border-orange-200',
    PENDIENTE_MICROSCOPIA:    'bg-purple-50 text-purple-700 border border-purple-200',
    COMPLETO:                 'bg-success/10 text-success border border-success/30',
  };

  readonly estadoLabels: Record<EstadoProtocolo, string> = {
    PENDIENTE_INOCULACION:    'Pendiente inoculación',
    INOCULACION_EN_CURSO:     'Inoculación en curso',
    PENDIENTE_VOCALIZACIONES: 'Pendiente vocalizaciones',
    PENDIENTE_TRES_CAMARAS:   'Pendiente 3 cámaras',
    PENDIENTE_MICROSCOPIA:    'Pendiente microscopía',
    COMPLETO:                 'Completo',
  };

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


  onSearch(q: string): void {
    this.search.set(q);
    this.params$.next({ ...this.params$.value, page: 0, q: q || undefined });
  }

  onFiltersChange(filters: Record<string, string | string[]>): void {
    this.activeFilters.set(filters);
    const uso        = filters['uso']         as string | undefined;
    const rangoVal   = filters['rango']       as string | undefined;
    const sexo       = filters['sexo']        as string | undefined;
    const alertasVal = filters['soloAlertas'] as string | undefined;
    const estadoVal  = filters['estado']      as EstadoProtocolo | undefined;
    this.params$.next({
      ...this.params$.value,
      page:        0,
      uso:         (uso === 'PROBLEMA' || uso === 'CONTROL') ? uso : undefined,
      rango:       rangoVal ? Number(rangoVal) : undefined,
      sexo:        (sexo === 'MACHO' || sexo === 'HEMBRA') ? sexo : undefined,
      soloAlertas: alertasVal === 'true' ? true : undefined,
      estado:      estadoVal ?? undefined,
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
    return !!(p.q || p.uso || p.rango != null || p.sexo || p.soloAlertas || p.estado);
  }

  getActionsFor(ma: ModeloAnimalListItem): RowAction[] {
    return [
      { label: 'Ver detalle', onClick: () => this.router.navigate(['/internal/modelos-animales', ma.identificador]) },
      { label: 'Editar',      style: 'primary', onClick: () => this.router.navigate(['/internal/modelos-animales', ma.identificador, 'editar']) },
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
