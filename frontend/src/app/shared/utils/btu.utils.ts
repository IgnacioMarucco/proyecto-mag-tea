export const BTU_RANGOS = [
  { max: 1313, label: 'Rango 0', css: 'badge-rango0' },
  { max: 2500, label: 'Rango 1', css: 'badge-rango1' },
  { max: 8000, label: 'Rango 2', css: 'badge-rango2' },
  { max: Infinity, label: 'Rango 3', css: 'badge-rango3' },
] as const;

export function getBtuLabel(btu: number | null): string | null {
  if (btu == null || btu < 0) return null;
  return BTU_RANGOS.find(r => btu <= r.max)?.label ?? 'Rango 3';
}

export function getBtuColor(btu: number | null): string {
  if (btu == null || btu < 0) return '';
  return BTU_RANGOS.find(r => btu <= r.max)?.css ?? 'badge-rango3';
}

export const RANGO_LABELS: Record<string, string> = Object.fromEntries(
  BTU_RANGOS.map((r, i) => [String(i), r.label])
);
export const RANGO_COLORS: Record<string, string> = Object.fromEntries(
  BTU_RANGOS.map((r, i) => [String(i), r.css])
);
export const USO_LABELS: Record<string, string> = {
  CONTROL:  'Caso Control',
  PROBLEMA: 'Caso Problema',
};
export const USO_COLORS: Record<string, string> = {
  CONTROL:  'bg-background text-text-muted border border-border',
  PROBLEMA: 'bg-primary-light text-primary',
};
