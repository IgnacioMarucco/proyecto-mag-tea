import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'ml', pure: true })
export class MlPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    return value != null ? value.toFixed(2) : '—';
  }
}
