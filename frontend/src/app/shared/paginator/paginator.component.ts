import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-paginator',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './paginator.component.html',
  imports: [IconComponent],
})
export class PaginatorComponent {
  currentPage   = input.required<number>();
  totalPages    = input.required<number>();
  totalElements = input.required<number>();
  itemLabel     = input('registros');

  pageChange = output<number>();

  readonly visiblePages = computed(() => {
    const total   = this.totalPages();
    const current = this.currentPage();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i);

    const around = new Set(
      [0, 1, current - 1, current, current + 1, total - 2, total - 1]
        .filter(p => p >= 0 && p < total)
    );
    const sorted = [...around].sort((a, b) => a - b);

    const pages: number[] = [];
    for (let i = 0; i < sorted.length; i++) {
      if (i > 0 && sorted[i] - sorted[i - 1] > 1) pages.push(-1);
      pages.push(sorted[i]);
    }
    return pages;
  });

  goTo(page: number): void {
    if (page >= 0 && page < this.totalPages()) this.pageChange.emit(page);
  }
}
