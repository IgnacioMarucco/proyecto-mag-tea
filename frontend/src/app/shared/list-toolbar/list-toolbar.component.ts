import { ChangeDetectionStrategy, Component, computed, ElementRef, input, output, signal, viewChild } from '@angular/core';

export interface FilterOption {
  key: string;
  label: string;
}

export interface FilterGroup {
  key: string;
  label: string;
  options: FilterOption[];
  multiSelect?: boolean;
}

@Component({
  selector: 'app-list-toolbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './list-toolbar.component.html',
})
export class ListToolbarComponent {
  placeholder   = input('Buscar…');
  showSearch    = input(true);
  filterGroups  = input<FilterGroup[]>([]);
  activeFilters = input<Record<string, string | string[]>>({});

  searchChange  = output<string>();
  filtersChange = output<Record<string, string | string[]>>();

  private readonly triggerBtn = viewChild<ElementRef<HTMLButtonElement>>('triggerBtn');

  searchValue  = signal('');
  panelOpen    = signal(false);
  panelStyle   = signal<Record<string, string>>({});

  activeCount = computed(() => {
    const filters = this.activeFilters();
    return this.filterGroups().filter(group => {
      if (group.multiSelect) {
        const val = filters[group.key];
        if (val === undefined) return false;
        return (val as string[]).length < group.options.length;
      }
      const defaultKey = group.options[0]?.key;
      return !!filters[group.key] && filters[group.key] !== defaultKey;
    }).length;
  });

  onInput(value: string): void {
    this.searchValue.set(value);
    this.searchChange.emit(value);
  }

  clearSearch(): void {
    this.searchValue.set('');
    this.searchChange.emit('');
  }

  togglePanel(): void {
    if (!this.panelOpen()) {
      const rect = this.triggerBtn()?.nativeElement.getBoundingClientRect();
      if (rect) {
        this.panelStyle.set({
          top:   `${rect.bottom + 4}px`,
          right: `${window.innerWidth - rect.right}px`,
        });
      }
    }
    this.panelOpen.update(v => !v);
  }

  // ── Single-select ──────────────────────────────────────────────────────────

  isOptionActive(groupKey: string, optionKey: string): boolean {
    return this.activeFilters()[groupKey] === optionKey;
  }

  isGroupDefault(group: FilterGroup): boolean {
    if (group.multiSelect) {
      const val = this.activeFilters()[group.key];
      if (val === undefined) return true;
      return (val as string[]).length >= group.options.length;
    }
    const defaultKey = group.options[0]?.key;
    const current    = this.activeFilters()[group.key];
    return !current || current === defaultKey;
  }

  getActiveLabel(group: FilterGroup): string {
    const val = this.activeFilters()[group.key] as string;
    return group.options.find(o => o.key === val)?.label ?? val;
  }

  selectOption(groupKey: string, optionKey: string): void {
    this.filtersChange.emit({ ...this.activeFilters(), [groupKey]: optionKey });
  }

  clearGroup(groupKey: string): void {
    const group = this.filterGroups().find(g => g.key === groupKey);
    if (!group) return;
    const next = { ...this.activeFilters() };
    if (group.multiSelect) {
      delete next[groupKey];
    } else {
      next[groupKey] = group.options[0]?.key ?? '';
    }
    this.filtersChange.emit(next);
  }

  // ── Multi-select ───────────────────────────────────────────────────────────

  isOptionChecked(groupKey: string, optionKey: string): boolean {
    const val = this.activeFilters()[groupKey];
    if (val === undefined) return true;
    return (val as string[]).includes(optionKey);
  }

  isAllSelected(group: FilterGroup): boolean {
    const val = this.activeFilters()[group.key];
    if (val === undefined) return true;
    return (val as string[]).length >= group.options.length;
  }

  toggleOption(groupKey: string, optionKey: string): void {
    const group = this.filterGroups().find(g => g.key === groupKey);
    if (!group) return;
    const current = this.activeFilters()[groupKey];
    const allKeys = group.options.map(o => o.key);
    let selected: string[];

    if (current === undefined) {
      selected = allKeys.filter(k => k !== optionKey);
    } else {
      const arr = current as string[];
      selected = arr.includes(optionKey)
        ? arr.filter(k => k !== optionKey)
        : [...arr, optionKey];
    }

    const next = { ...this.activeFilters() };
    if (selected.length >= allKeys.length) {
      delete next[groupKey];
    } else {
      next[groupKey] = selected;
    }
    this.filtersChange.emit(next);
  }

  selectAllInGroup(group: FilterGroup): void {
    const next = { ...this.activeFilters() };
    delete next[group.key];
    this.filtersChange.emit(next);
  }

  getMultiPills(group: FilterGroup): string[] {
    const val = this.activeFilters()[group.key];
    if (!val) return [];
    const arr = val as string[];
    if (arr.length >= group.options.length) return [];
    return arr;
  }

  getPillLabel(group: FilterGroup, key: string): string {
    return group.options.find(o => o.key === key)?.label ?? key;
  }

  removeFromMulti(groupKey: string, optionKey: string): void {
    const group = this.filterGroups().find(g => g.key === groupKey);
    if (!group) return;
    const current  = this.activeFilters()[groupKey];
    const allKeys  = group.options.map(o => o.key);
    const base     = current === undefined ? allKeys : (current as string[]);
    const selected = base.filter(k => k !== optionKey);

    const next = { ...this.activeFilters() };
    if (selected.length === 0 || selected.length >= allKeys.length) {
      delete next[groupKey];
    } else {
      next[groupKey] = selected;
    }
    this.filtersChange.emit(next);
  }
}
