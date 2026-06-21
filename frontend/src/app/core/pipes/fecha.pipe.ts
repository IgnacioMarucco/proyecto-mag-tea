import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'fecha', pure: true })
export class FechaPipe implements PipeTransform {
  transform(value: string | null | undefined, format: 'date' | 'datetime' = 'date'): string {
    if (!value) return '—';
    // Agregar T00:00:00 en strings de solo fecha para evitar offset UTC en zonas negativas
    const date = new Date(format === 'date' && !value.includes('T') ? value + 'T00:00:00' : value);
    if (isNaN(date.getTime())) return '—';
    return format === 'datetime'
      ? date.toLocaleString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
      : date.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
