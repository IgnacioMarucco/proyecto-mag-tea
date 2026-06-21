import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { SortState } from '../sort.utils';

export interface TableColumn {
  label: string;
  hidden?: 'sm' | 'md' | 'lg';
  sortKey?: string;
}

@Component({
  selector: 'app-data-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './data-table.component.html',
})
export class DataTableComponent {
  columns       = input.required<TableColumn[]>();
  rows          = input.required<number>();
  ariaLabel     = input('');
  emptyTitle    = input('Sin resultados');
  emptySubtitle = input('Probá con otro criterio de búsqueda');
  currentSort   = input<SortState | null>(null);
  loading       = input(false);

  sortChange = output<SortState>();

  activeSortDir(col: TableColumn): 'asc' | 'desc' | null {
    if (!col.sortKey) return null;
    const sort = this.currentSort();
    if (!sort || sort.key !== col.sortKey) return null;
    return sort.direction;
  }

  onHeaderClick(col: TableColumn): void {
    if (!col.sortKey) return;
    const current   = this.currentSort();
    const direction: 'asc' | 'desc' =
      current?.key === col.sortKey && current.direction === 'asc' ? 'desc' : 'asc';
    this.sortChange.emit({ key: col.sortKey, direction });
  }

  getAriaSort(col: TableColumn): 'ascending' | 'descending' | 'none' | null {
    if (!col.sortKey) return null;
    const sort = this.currentSort();
    if (!sort || sort.key !== col.sortKey) return 'none';
    return sort.direction === 'asc' ? 'ascending' : 'descending';
  }

  thClass(col: TableColumn): string {
    const hidden =
      col.hidden === 'sm' ? ' hidden sm:table-cell' :
      col.hidden === 'md' ? ' hidden md:table-cell' :
      col.hidden === 'lg' ? ' hidden lg:table-cell' : '';
    return 'text-left px-5 py-3 text-xs font-mono text-text-muted tracking-wider uppercase' + hidden;
  }
}
