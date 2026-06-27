import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModeloAnimalService } from '../../../../core/services/modelo-animal.service';
import { ModeloAnimalResponse } from '../../../../core/models/modelo-animal.model';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { ToastService } from '../../../../core/services/toast.service';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { BANDA_COLORS, BANDA_LABELS } from '../../../../shared/utils/modelo-animal.utils';

@Component({
  selector: 'app-vocalizaciones-section',
  imports: [ReactiveFormsModule, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './vocalizaciones-section.component.html',
})
export class VocalizacionesSectionComponent {
  private readonly service = inject(ModeloAnimalService);
  private readonly fb      = inject(FormBuilder);
  private readonly toast   = inject(ToastService);

  modeloAnimal = input.required<ModeloAnimalResponse>();
  updated      = output<ModeloAnimalResponse>();

  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({
    muestra1Khz: [null as number | null, [Validators.required, Validators.min(0)]],
    muestra2Khz: [null as number | null, [Validators.required, Validators.min(0)]],
  });

  readonly bandaColors = BANDA_COLORS;
  readonly bandaLabels = BANDA_LABELS;

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);
    const v = this.form.value;
    this.service.patchVocalizaciones(this.modeloAnimal().id, {
      muestra1Khz: Number(v.muestra1Khz),
      muestra2Khz: Number(v.muestra2Khz),
    }).subscribe({
      next:  ma  => { this.toast.show('Vocalizaciones guardadas'); this.updated.emit(ma); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }
}
