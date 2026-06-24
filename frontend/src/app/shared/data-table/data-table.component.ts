import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { SortState } from '../sort.utils';
import { IconComponent } from '../icon/icon.component';

export interface TableColumn {
  label: string;
  hidden?: 'sm' | 'md' | 'lg';
  sortKey?: string;
  align?: 'center' | 'right';
}

@Component({
  selector: 'app-data-table',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './data-table.component.html',
  imports: [IconComponent],
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
    const align =
      col.align === 'center' ? ' text-center' :
      col.align === 'right'  ? ' text-right'  : ' text-left';
    return 'px-5 py-3 text-xs font-mono text-text-muted tracking-wider uppercase' + align + hidden;
  }
}
