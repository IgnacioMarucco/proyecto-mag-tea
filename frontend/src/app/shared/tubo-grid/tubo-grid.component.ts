import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

const ROWS = ['A','B','C','D','E','F','G','H','I','J'];
const COLS = [1,2,3,4,5,6,7,8,9,10];

@Component({
  selector: 'app-tubo-grid',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tubo-grid.component.html',
})
export class TuboGridComponent {
  value       = input<string>('');
  occupied    = input<string>('');
  valueChange = output<string>();

  readonly rows = ROWS;
  readonly cols = COLS;

  readonly selected = computed(() => {
    const v = this.value();
    if (!v?.trim()) return new Set<string>();
    return new Set(v.split(',').map(s => s.trim()).filter(Boolean));
  });

  readonly occupiedSet = computed(() => {
    const v = this.occupied();
    if (!v?.trim()) return new Set<string>();
    return new Set(v.split(',').map(s => s.trim()).filter(Boolean));
  });

  cellKey(row: string, col: number): string { return `${row}${col}`; }

  isSelected(row: string, col: number): boolean {
    return this.selected().has(this.cellKey(row, col));
  }

  isOccupied(row: string, col: number): boolean {
    return this.occupiedSet().has(this.cellKey(row, col));
  }

  cellClass(row: string, col: number): string {
    const base = 'w-7 h-7 mr-0.5 rounded text-xs font-mono transition-colors';
    if (this.isSelected(row, col))  return `${base} font-semibold bg-primary text-white`;
    if (this.isOccupied(row, col))  return `${base} bg-error/20 text-error border border-error/30 cursor-not-allowed`;
    return `${base} bg-background border border-border text-text-muted hover:border-primary hover:text-primary`;
  }

  toggle(row: string, col: number): void {
    if (this.isOccupied(row, col)) return;
    const key = this.cellKey(row, col);
    const sel = new Set(this.selected());
    if (sel.has(key)) sel.delete(key);
    else sel.add(key);
    const ordered = ROWS.flatMap(r => COLS.map(c => `${r}${c}`)).filter(k => sel.has(k));
    this.valueChange.emit(ordered.join(', '));
  }
}
