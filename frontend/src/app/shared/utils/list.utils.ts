import { Signal } from '@angular/core';
import { FilterGroup } from '../list-toolbar/list-toolbar.component';

export function hasActiveSearch(
  search: Signal<string>,
  activeFilters: Signal<Record<string, string | string[]>>,
  filterGroups: FilterGroup[]
): boolean {
  if (search()) return true;
  const f = activeFilters();
  return filterGroups.some(group => {
    const val = f[group.key];
    if (val === undefined) return false;
    return group.multiSelect
      ? (val as string[]).length < group.options.length
      : val !== group.options[0]?.key;
  });
}
