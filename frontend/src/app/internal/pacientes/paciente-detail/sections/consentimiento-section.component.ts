import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { PacienteService } from '../../../../core/services/paciente.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { IconComponent } from '../../../../shared/icon/icon.component';

@Component({
  selector: 'app-consentimiento-section',
  imports: [ReactiveFormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './consentimiento-section.component.html',
})
export class ConsentimientoSectionComponent {
  private readonly service = inject(PacienteService);
  private readonly fb      = inject(FormBuilder);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({ consentimientoFirmado: [false] });

  openModal(): void {
    this.saveError.set(null);
    this.form.patchValue({ consentimientoFirmado: this.paciente().consentimientoFirmado });
    this.showModal.set(true);
  }

  save(): void {
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchConsentimiento(this.paciente().id, {
      consentimientoFirmado: !!this.form.value.consentimientoFirmado,
    }).subscribe({
      next: p  => { this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(err.error?.message ?? 'Error al guardar'); this.saving.set(false); },
    });
  }
}
