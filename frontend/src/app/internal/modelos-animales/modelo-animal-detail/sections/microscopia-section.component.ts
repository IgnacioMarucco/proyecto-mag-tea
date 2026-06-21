import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModeloAnimalService } from '../../../../core/services/modelo-animal.service';
import { ModeloAnimalResponse } from '../../../../core/models/modelo-animal.model';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';

@Component({
  selector: 'app-microscopia-section',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './microscopia-section.component.html',
})
export class MicroscopiaSectionComponent {
  private readonly service = inject(ModeloAnimalService);
  private readonly fb      = inject(FormBuilder);

  modeloAnimal = input.required<ModeloAnimalResponse>();
  updated      = output<ModeloAnimalResponse>();

  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({
    numCelulasGanglionares: [null as number | null, [Validators.required, Validators.min(0)]],
    numCelulasPurkinje:     [null as number | null, [Validators.required, Validators.min(0)]],
  });

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);
    const v = this.form.value;
    this.service.patchMicroscopia(this.modeloAnimal().id, {
      numCelulasGanglionares: Number(v.numCelulasGanglionares),
      numCelulasPurkinje:     Number(v.numCelulasPurkinje),
    }).subscribe({
      next:  ma  => { this.updated.emit(ma); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }
}
