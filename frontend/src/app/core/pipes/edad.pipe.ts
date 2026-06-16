import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'edad', pure: true })
export class EdadPipe implements PipeTransform {
  transform(fechaNacimiento: string | null | undefined, fechaReferencia?: string | null): string {
    if (!fechaNacimiento) return '—';
    const nac = fechaNacimiento.substring(0, 10).split('-').map(Number) as [number, number, number];
    const ref = (fechaReferencia ?? new Date().toISOString()).substring(0, 10).split('-').map(Number) as [number, number, number];

    let years  = ref[0] - nac[0];
    let months = ref[1] - nac[1];
    if (ref[2] < nac[2]) months--;
    if (months < 0) { years--; months += 12; }

    return months > 0 ? `${years}a ${months}m` : `${years} años`;
  }
}
