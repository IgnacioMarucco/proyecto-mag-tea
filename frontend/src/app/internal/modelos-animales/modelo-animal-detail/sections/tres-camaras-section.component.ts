import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModeloAnimalService } from '../../../../core/services/modelo-animal.service';
import { ModeloAnimalResponse } from '../../../../core/models/modelo-animal.model';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';

@Component({
  selector: 'app-tres-camaras-section',
  imports: [ReactiveFormsModule, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tres-camaras-section.component.html',
})
export class TresCamarasSectionComponent {
  private readonly service = inject(ModeloAnimalService);
  private readonly fb      = inject(FormBuilder);

  modeloAnimal = input.required<ModeloAnimalResponse>();
  updated      = output<ModeloAnimalResponse>();

  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({
    m1TiempoRatonNovedad:      [null as number | null, [Validators.required, Validators.min(0)]],
    m1TiempoObjetoNovedoso:    [null as number | null, [Validators.required, Validators.min(0)]],
    m2TiempoRatonDesconocido:  [null as number | null, [Validators.required, Validators.min(0)]],
    m2TiempoRatonFamiliar:     [null as number | null, [Validators.required, Validators.min(0)]],
  });

  readonly socializacionColors: Record<string, string> = {
    NORMAL:              'bg-accent-light text-accent',
    FALTA_SOCIALIZACION: 'bg-error/10 text-error',
  };
  readonly socializacionLabels: Record<string, string> = {
    NORMAL:              'Normal',
    FALTA_SOCIALIZACION: 'Falta socialización',
  };

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);
    const v = this.form.value;
    this.service.patchTresCamaras(this.modeloAnimal().id, {
      m1TiempoRatonNovedad:     Number(v.m1TiempoRatonNovedad),
      m1TiempoObjetoNovedoso:   Number(v.m1TiempoObjetoNovedoso),
      m2TiempoRatonDesconocido: Number(v.m2TiempoRatonDesconocido),
      m2TiempoRatonFamiliar:    Number(v.m2TiempoRatonFamiliar),
    }).subscribe({
      next:  ma  => { this.updated.emit(ma); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }
}
