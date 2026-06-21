import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ModeloAnimalResponse } from '../../../../core/models/modelo-animal.model';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';

@Component({
  selector: 'app-datos-basicos-ma-section',
  imports: [RouterLink, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './datos-basicos-ma-section.component.html',
})
export class DatosBasicosMaSectionComponent {
  modeloAnimal = input.required<ModeloAnimalResponse>();

  readonly rangoColors: Record<string, string> = {
    '1': 'badge-rango1',
    '2': 'badge-rango2',
    '3': 'badge-rango3',
  };
  readonly rangoLabels: Record<string, string> = {
    '1': 'Rango 1', '2': 'Rango 2', '3': 'Rango 3',
  };
  readonly sexoLabels: Record<string, string> = { MACHO: 'Macho', HEMBRA: 'Hembra' };
  readonly sexoColors: Record<string, string> = {
    MACHO:  'bg-primary-light text-primary',
    HEMBRA: 'bg-accent-light text-accent',
  };

  readonly diaNacimiento = computed(() => {
    const ma = this.modeloAnimal();
    const nac = new Date(ma.fechaNacimiento + 'T00:00:00');
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const diff = Math.floor((hoy.getTime() - nac.getTime()) / (1000 * 60 * 60 * 24));
    return diff + 1;
  });

  formatDate(date: string): string {
    return new Date(date + 'T00:00:00').toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  }
}
