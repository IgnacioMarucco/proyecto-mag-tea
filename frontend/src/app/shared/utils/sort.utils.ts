export interface SortState {
  key: string;
  direction: 'asc' | 'desc';
}

export function applySort<T>(
  arr: T[],
  sort: SortState | null,
  getValue?: (item: T, key: string) => unknown
): T[] {
  if (!sort) return arr;
  return [...arr].sort((a, b) => {
    const va = getValue ? getValue(a, sort.key) : (a as Record<string, unknown>)[sort.key];
    const vb = getValue ? getValue(b, sort.key) : (b as Record<string, unknown>)[sort.key];
    if (va == null && vb == null) return 0;
    if (va == null) return 1;
    if (vb == null) return -1;
    const cmp = typeof va === 'string' && typeof vb === 'string'
      ? va.localeCompare(vb, 'es-AR', { sensitivity: 'base' })
      : va < vb ? -1 : va > vb ? 1 : 0;
    return sort.direction === 'asc' ? cmp : -cmp;
  });
}
