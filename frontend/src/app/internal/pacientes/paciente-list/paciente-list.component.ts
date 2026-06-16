import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, map, of, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { PacienteService, PacienteListParams } from '../../../core/services/paciente.service';
import { ListToolbarComponent, FilterGroup } from '../../../shared/list-toolbar/list-toolbar.component';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { DataTableComponent, TableColumn } from '../../../shared/data-table/data-table.component';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { RowActionsComponent, RowAction } from '../../../shared/row-actions/row-actions.component';
import { PaginatorComponent } from '../../../shared/paginator/paginator.component';
import { PacienteListItem } from '../../../core/models/paciente.model';
import { SortState } from '../../../shared/sort.utils';
import { IconComponent } from '../../../shared/icon/icon.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { EdadPipe } from '../../../core/pipes/edad.pipe';

const PAGE_SIZE = 20;
const ALL_ESTADOS = ['ADMITIDO', 'MCHAT_RESPONDIDO', 'EXTRACCION_PENDIENTE', 'EXTRACCION_REALIZADA'];

@Component({
  selector: 'app-paciente-list',
  imports: [RouterLink, ListToolbarComponent, ConfirmModalComponent, DataTableComponent, StatusBadgeComponent, RowActionsComponent, PaginatorComponent, IconComponent, PageHeaderComponent, EdadPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './paciente-list.component.html',
})
export class PacienteListComponent {
  private readonly service = inject(PacienteService);
  private readonly router  = inject(Router);
  private readonly route   = inject(ActivatedRoute);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  private readonly params$ = new BehaviorSubject<PacienteListParams>({
    page: 0, size: PAGE_SIZE, sortBy: 'createdAt', sortDir: 'desc',
    estados: ['ADMITIDO', 'MCHAT_RESPONDIDO', 'EXTRACCION_PENDIENTE'],
  });

  private readonly response = toSignal(
    this.params$.pipe(
      switchMap(params => this.service.findAll(params).pipe(catchError(() => of(null))))
    ),
    { initialValue: null }
  );

  readonly pacientes     = computed(() => this.response()?.content ?? []);
  readonly totalElements = computed(() => this.response()?.totalElements ?? 0);
  readonly totalPages    = computed(() => this.response()?.totalPages ?? 1);
  readonly currentPage   = computed(() => this.response()?.page ?? 0);

  search          = signal('');
  activeFilters   = signal<Record<string, string | string[]>>({ estado: ['ADMITIDO', 'MCHAT_RESPONDIDO', 'EXTRACCION_PENDIENTE'] });
  sortState       = signal<SortState>({ key: 'createdAt', direction: 'desc' });
  deleting        = signal<number | null>(null);
  pendingDeleteId = signal<number | null>(null);

  readonly filterGroups: FilterGroup[] = [
    {
      key: 'estado',
      label: 'Estado',
      multiSelect: true,
      options: [
        { key: 'ADMITIDO',             label: 'Admitido' },
        { key: 'MCHAT_RESPONDIDO',     label: 'M-CHAT respondido' },
        { key: 'EXTRACCION_PENDIENTE', label: 'Extracción pendiente' },
        { key: 'EXTRACCION_REALIZADA', label: 'Extracción realizada' },
      ],
    },
    {
      key: 'tipo',
      label: 'Tipo',
      multiSelect: true,
      options: [
        { key: 'PROBLEMA', label: 'Caso problema' },
        { key: 'CONTROL',  label: 'Control' },
      ],
    },
  ];

  readonly columns: TableColumn[] = [
    { label: 'Tutor/a' },
    { label: 'Niño/a' },
    { label: 'Edad',   hidden: 'sm' },
    { label: 'Tipo',   hidden: 'sm' },
    { label: 'Estado' },
    { label: 'Fecha',  hidden: 'md', sortKey: 'createdAt' },
  ];

  readonly tipoLabels: Record<string, string> = {
    CONTROL:  'Control',
    PROBLEMA: 'Caso problema',
  };

  readonly tipoColors: Record<string, string> = {
    CONTROL:  'bg-background text-text-muted border border-border',
    PROBLEMA: 'bg-primary-light text-primary',
  };

  readonly estadoLabels: Record<string, string> = {
    ADMITIDO:             'Admitido',
    MCHAT_RESPONDIDO:     'M-CHAT respondido',
    EXTRACCION_PENDIENTE: 'Extracción pendiente',
    EXTRACCION_REALIZADA: 'Extracción realizada',
  };

  readonly estadoColors: Record<string, string> = {
    ADMITIDO:             'bg-background text-text-muted border border-border',
    MCHAT_RESPONDIDO:     'bg-warning/10 text-warning',
    EXTRACCION_PENDIENTE: 'bg-primary-light text-primary',
    EXTRACCION_REALIZADA: 'bg-accent-light text-accent',
  };

  readonly emptyTitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'No hay pacientes registrados'
      : 'Sin resultados'
  );

  readonly emptySubtitle = computed(() =>
    this.totalElements() === 0 && !this.hasActiveSearch()
      ? 'Admitir un formulario de interés para registrar el primero'
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
    const tipoVal = filters['tipo'];
    const tipos = Array.isArray(tipoVal) ? tipoVal : [];
    this.params$.next({
      ...this.params$.value,
      page: 0,
      estados: estados.length && estados.length < ALL_ESTADOS.length ? estados : undefined,
      tipos:   tipos.length < 2 ? tipos : undefined,
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
    const p = this.params$.value as Record<string, unknown>;
    return !!(p['q'] || (p['estados'] as string[] | undefined)?.length);
  }

proximaFecha(p: PacienteListItem): { label: string; valor: string } | null {
    switch (p.pacienteEstado) {
      case 'ADMITIDO':
      case 'MCHAT_RESPONDIDO':
        if (!p.fechaPrimeraVisita) return null;
        return { label: 'Primera visita', valor: this.formatDateTime(p.fechaPrimeraVisita) };
      case 'EXTRACCION_PENDIENTE':
        if (!p.fechaExtraccion) return null;
        return { label: 'Extracción', valor: this.formatDate(p.fechaExtraccion) };
      default:
        return null;
    }
  }

  private formatDate(date: string): string {
    return new Date(date + 'T00:00:00').toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  }

  private formatDateTime(dt: string): string {
    return new Date(dt).toLocaleString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }

  getActionsFor(p: PacienteListItem): RowAction[] {
    return [
      { label: 'Detalles', style: 'primary', onClick: () => this.router.navigate(['/internal/pacientes', p.id]) },
      { label: 'Editar',      style: 'default', onClick: () => this.router.navigate(['/internal/pacientes', p.id, 'editar']) },
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
      next:  () => { this.deleting.set(null); this.reload(); },
      error: () =>   this.deleting.set(null),
    });
  }
}
