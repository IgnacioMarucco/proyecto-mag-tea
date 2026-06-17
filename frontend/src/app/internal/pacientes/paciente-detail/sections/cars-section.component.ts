import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { startWith } from 'rxjs';
import { PacienteService } from '../../../../core/services/paciente.service';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { ModalContainerComponent } from '../../../../shared/modal-container/modal-container.component';
import { CARS_ITEMS } from '../../../../shared/constants/cars.constants';

@Component({
  selector: 'app-cars-section',
  imports: [ReactiveFormsModule, StatusBadgeComponent, ModalContainerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './cars-section.component.html',
})
export class CarsSectionComponent {
  private readonly service = inject(PacienteService);
  private readonly fb      = inject(FormBuilder);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  readonly items   = CARS_ITEMS;
  readonly valores = [1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0];

  form = this.fb.group({
    ...Object.fromEntries(Array.from({ length: 15 }, (_, i) => [`item${i + 1}`, [null as number | null]])),
    ...Object.fromEntries(Array.from({ length: 15 }, (_, i) => [`obs${i  + 1}`, [null as string | null]])),
    tScore:    [null as number | null],
    percentil: [null as number | null],
  });

  private readonly formValue = toSignal(
    this.form.valueChanges.pipe(startWith(this.form.value)),
    { initialValue: this.form.value as Record<string, unknown> }
  );

  protected itemSelected(row: number, val: number): boolean {
    return Number((this.formValue() as any)[`item${row}`]) === val;
  }

  readonly rawScore = computed(() =>
    Array.from({ length: 15 }, (_, i) => Number((this.formValue() as any)[`item${i + 1}`]) || 0)
      .reduce((s, x) => s + x, 0)
  );

  readonly resultadoEstimado = computed(() => {
    const raw = this.rawScore();
    if (raw === 0) return null;
    if (raw < 30) return 'MINIMO_NO_TEA';
    if (raw < 37) return 'LEVE_MODERADO';
    return 'SEVERO';
  });

  readonly resultadoLabels: Record<string, string> = {
    MINIMO_NO_TEA: 'Mínimo / No TEA', LEVE_MODERADO: 'Leve a Moderado', SEVERO: 'Severo',
  };
  readonly resultadoColors: Record<string, string> = {
    MINIMO_NO_TEA: 'bg-accent-light text-accent',
    LEVE_MODERADO: 'bg-warning-light text-warning',
    SEVERO: 'bg-danger-light text-danger',
  };

  openModal(): void {
    this.saveError.set(null);
    const p  = this.paciente();
    const ci = p.carsItems;
    this.form.reset();
    if (ci) {
      const patch: Record<string, unknown> = {};
      for (let i = 1; i <= 15; i++) {
        patch[`item${i}`] = (ci as any)[`item${i}`] ?? null;
        patch[`obs${i}`]  = (ci as any)[`obs${i}`]  ?? null;
      }
      patch['tScore']    = p.carsTScore    ?? null;
      patch['percentil'] = p.carsPercentil ?? null;
      this.form.patchValue(patch);
    } else {
      this.form.patchValue({ tScore: p.carsTScore ?? null, percentil: p.carsPercentil ?? null });
    }
    this.showModal.set(true);
  }

  save(): void {
    if (this.saving()) return;
    const v = this.form.value as any;
    const allSet = Array.from({ length: 15 }, (_, i) => v[`item${i + 1}`]).every(x => x !== null);
    if (!allSet) { this.saveError.set('Debés completar los 15 ítems de CARS-2'); return; }
    const dto = {
      ...Object.fromEntries(Array.from({ length: 15 }, (_, i) => [`item${i + 1}`, Number(v[`item${i + 1}`])])),
      ...Object.fromEntries(Array.from({ length: 15 }, (_, i) => [`obs${i  + 1}`, v[`obs${i  + 1}`] || null])),
      tScore:    v['tScore']    != null ? Number(v['tScore'])    : null,
      percentil: v['percentil'] != null ? Number(v['percentil']) : null,
    } as any;
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchCars(this.paciente().id, dto).subscribe({
      next:  p   => { this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }
}
