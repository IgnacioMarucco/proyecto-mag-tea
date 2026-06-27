import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModeloAnimalService } from '../../../../core/services/modelo-animal.service';
import { ModeloAnimalResponse } from '../../../../core/models/modelo-animal.model';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { ToastService } from '../../../../core/services/toast.service';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { SOCIALIZACION_COLORS, SOCIALIZACION_LABELS } from '../../../../shared/utils/modelo-animal.utils';

@Component({
  selector: 'app-tres-camaras-section',
  imports: [ReactiveFormsModule, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './tres-camaras-section.component.html',
})
export class TresCamarasSectionComponent {
  private readonly service = inject(ModeloAnimalService);
  private readonly fb      = inject(FormBuilder);
  private readonly toast   = inject(ToastService);

  modeloAnimal = input.required<ModeloAnimalResponse>();
  updated      = output<ModeloAnimalResponse>();

  savingM1    = signal(false);
  savingM2    = signal(false);
  saveErrorM1 = signal<string | null>(null);
  saveErrorM2 = signal<string | null>(null);

  formM1 = this.fb.group({
    m1TiempoRatonNovedad:   [null as number | null, [Validators.required, Validators.min(0)]],
    m1TiempoObjetoNovedoso: [null as number | null, [Validators.required, Validators.min(0)]],
  });

  formM2 = this.fb.group({
    m2TiempoRatonDesconocido: [null as number | null, [Validators.required, Validators.min(0)]],
    m2TiempoRatonFamiliar:    [null as number | null, [Validators.required, Validators.min(0)]],
  });

  readonly socializacionColors = SOCIALIZACION_COLORS;
  readonly socializacionLabels = SOCIALIZACION_LABELS;

  saveM1(): void {
    if (this.formM1.invalid) { this.formM1.markAllAsTouched(); return; }
    if (this.savingM1()) return;
    this.savingM1.set(true);
    this.saveErrorM1.set(null);
    const v = this.formM1.value;
    this.service.patchTresCamaras(this.modeloAnimal().id, {
      m1TiempoRatonNovedad:   Number(v.m1TiempoRatonNovedad),
      m1TiempoObjetoNovedoso: Number(v.m1TiempoObjetoNovedoso),
    }).subscribe({
      next:  ma  => { this.toast.show('Tres cámaras guardado'); this.updated.emit(ma); this.savingM1.set(false); },
      error: err => { this.saveErrorM1.set(extractErrorMessage(err, 'Error al guardar')); this.savingM1.set(false); },
    });
  }

  saveM2(): void {
    if (this.formM2.invalid) { this.formM2.markAllAsTouched(); return; }
    if (this.savingM2()) return;
    this.savingM2.set(true);
    this.saveErrorM2.set(null);
    const v = this.formM2.value;
    this.service.patchTresCamaras(this.modeloAnimal().id, {
      m2TiempoRatonDesconocido: Number(v.m2TiempoRatonDesconocido),
      m2TiempoRatonFamiliar:    Number(v.m2TiempoRatonFamiliar),
    }).subscribe({
      next:  ma  => { this.toast.show('Tres cámaras guardado'); this.updated.emit(ma); this.savingM2.set(false); },
      error: err => { this.saveErrorM2.set(extractErrorMessage(err, 'Error al guardar')); this.savingM2.set(false); },
    });
  }
}
