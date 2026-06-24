import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, catchError, combineLatest, map, of, switchMap, tap, timer } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormularioInteresService, FormularioListParams } from '../../../core/services/formulario-interes.service';
import { EstadoFormulario, FormularioInteresResponse } from '../../../core/models/formulario-interes.model';
import { ListToolbarComponent, FilterGroup } from '../../../shared/list-toolbar/list-toolbar.component';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { DataTableComponent, TableColumn } from '../../../shared/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { RowActionsComponent, RowAction } from '../../../shared/row-actions/row-actions.component';
import { PaginatorComponent } from '../../../shared/paginator/paginator.component';
import { SortState } from '../../../shared/utils/sort.utils';
import { hasActiveSearch } from '../../../shared/utils/list.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { EdadPipe } from '../../../core/pipes/edad.pipe';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { FormularioDetalleModalComponent } from './formulario-detalle-modal.component';

const PAGE_SIZE = 20;
const ALL_ESTADOS = ['PENDIENTE', 'CONTACTADO', 'ADMITIDO', 'DESCARTADO'];

@Component({
  selector: 'app-bandeja',
  imports: [ListToolbarComponent, ConfirmModalComponent, DataTableComponent, StatusBadgeComponent, RowActionsComponent, PaginatorComponent, PageHeaderComponent, EdadPipe, FechaPipe, FormularioDetalleModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './bandeja.component.html',
})
export class BandejaComponent {
  private readonly service = inject(FormularioInteresService);
  private readonly router  = inject(Router);
  private readonly route   = inject(ActivatedRoute);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  private readonly params$ = new BehaviorSubject<FormularioListParams>({
    page: 0, size: PAGE_SIZE, sortBy: 'fechaContacto', sortDir: 'desc',
    estados: ['PENDIENTE', 'CONTACTADO'],
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

  readonly formularios   = computed(() => this.response()?.content ?? []);
  readonly totalElements = computed(() => this.response()?.totalElements ?? 0);
  readonly totalPages    = computed(() => this.response()?.totalPages ?? 1);
  readonly currentPage   = computed(() => this.response()?.page ?? 0);

  search             = signal('');
  activeFilters      = signal<Record<string, string | string[]>>({ estado: ['PENDIENTE', 'CONTACTADO'] });
  sortState          = signal<SortState>({ key: 'fechaContacto', direction: 'desc' });
  procesando         = signal<number | null>(null);
  pendingDescartarId = signal<number | null>(null);
  pendingRevertirId  = signal<number | null>(null);
  selectedFormulario = signal<FormularioInteresResponse | null>(null);

  readonly filterGroups: FilterGroup[] = [
    {
      key: 'estado',
      label: 'Estado',
      multiSelect: true,
      options: [
        { key: 'PENDIENTE',  label: 'Pendiente' },
        { key: 'CONTACTADO', label: 'Contactado' },
        { key: 'ADMITIDO',   label: 'Admitido' },
        { key: 'DESCARTADO', label: 'Descartado' },
      ],
    },
  ];

  readonly columns: TableColumn[] = [
    { label: 'PACIENTE' },
    { label: 'EDAD' },
    { label: 'TELÉFONO' },
    { label: 'MAIL' },
    { label: 'FECHA', sortKey: 'fechaContacto' },
    { label: 'ESTADO' },
  ];

  readonly estadoLabels: Record<EstadoFormulario, string> = {
    PENDIENTE:   'Pendiente',
    CONTACTADO:  'Contactado',
    ADMITIDO:    'Admitido',
    DESCARTADO:  'Descartado',
  };

  readonly estadoColors: Record<EstadoFormulario, string> = {
    PENDIENTE:   'bg-warning/10 text-warning',
    CONTACTADO:  'bg-primary-light text-primary',
    ADMITIDO:    'bg-accent-light text-accent',
    DESCARTADO:  'bg-background text-text-muted border border-border',
  };

  readonly emptyTitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'No hay formularios recibidos'
      : 'Sin resultados'
  );

  readonly emptySubtitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'Los formularios enviados desde la web aparecerán acá'
      : 'Probá ajustando los filtros o la búsqueda'
  );

  onSearch(q: string): void {
    this.search.set(q);
    this.params$.next({ ...this.params$.value, page: 0, q: q || undefined });
  }

  onFiltersChange(filters: Record<string, string | string[]>): void {
    this.activeFilters.set(filters);
    const estadoVal = filters['estado'];
    const estados = Array.isArray(estadoVal) ? estadoVal : [];
    this.params$.next({
      ...this.params$.value,
      page: 0,
      estados: estados.length && estados.length < ALL_ESTADOS.length ? estados : undefined,
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
    return hasActiveSearch(this.search, this.activeFilters, this.filterGroups);
  }

getActionsFor(f: FormularioInteresResponse): RowAction[] {
    const procesando = this.procesando();
    return [
      { label: 'Detalles', onClick: () => this.verDetalles(f) },
      ...(f.estado === 'PENDIENTE' ? [
        { label: 'Contactar', style: 'primary' as const, disabled: procesando === f.id, onClick: () => this.contactar(f.id) },
        { label: 'Descartar', style: 'danger'  as const, disabled: procesando === f.id, onClick: () => this.requestDescartar(f.id) },
      ] : []),
      ...(f.estado === 'CONTACTADO' ? [
        { label: 'Revertir a Pendiente', style: 'default' as const, disabled: procesando === f.id, onClick: () => this.requestRevertir(f.id) },
        { label: 'Admitir',              style: 'primary' as const, onClick: () => this.router.navigate(['/internal/pacientes/nuevo'], { queryParams: { formularioId: f.id } }) },
        { label: 'Descartar',            style: 'danger'  as const, disabled: procesando === f.id, onClick: () => this.requestDescartar(f.id) },
      ] : []),
    ];
  }

  verDetalles(f: FormularioInteresResponse): void { this.selectedFormulario.set(f); }
  cerrarDetalle(): void                           { this.selectedFormulario.set(null); }

  contactar(id: number): void {
    this.procesando.set(id);
    this.service.cambiarEstado(id, 'CONTACTADO').subscribe({
      next:  () => { this.procesando.set(null); this.reload(); },
      error: () =>   this.procesando.set(null),
    });
  }

  requestDescartar(id: number): void  { this.pendingDescartarId.set(id); }
  cancelDescartar(): void             { this.pendingDescartarId.set(null); }

  confirmDescartar(): void {
    const id = this.pendingDescartarId();
    if (id === null) return;
    this.pendingDescartarId.set(null);
    this.procesando.set(id);
    this.service.cambiarEstado(id, 'DESCARTADO').subscribe({
      next:  () => { this.procesando.set(null); this.reload(); },
      error: () =>   this.procesando.set(null),
    });
  }

  requestRevertir(id: number): void  { this.pendingRevertirId.set(id); }
  cancelRevertir(): void             { this.pendingRevertirId.set(null); }

  confirmRevertir(): void {
    const id = this.pendingRevertirId();
    if (id === null) return;
    this.pendingRevertirId.set(null);
    this.procesando.set(id);
    this.service.revertirAPendiente(id).subscribe({
      next:  () => { this.procesando.set(null); this.reload(); },
      error: () =>   this.procesando.set(null),
    });
  }
}
