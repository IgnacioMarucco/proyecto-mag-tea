import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { PacienteService } from '../../../../core/services/paciente.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { IconComponent } from '../../../../shared/icon/icon.component';

@Component({
  selector: 'app-vineland-section',
  imports: [ReactiveFormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './vineland-section.component.html',
})
export class VinelandSectionComponent {
  private readonly service = inject(PacienteService);
  private readonly fb      = inject(FormBuilder);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({
    vinelandComunicacion:          [null as number | null],
    vinelandAutovalimiento:        [null as number | null],
    vinelandSocial:                [null as number | null],
    vinelandMotor:                 [null as number | null],
    vinelandCocienteFinal:         [null as number | null],
    vinelandConductaDesadaptativa: [null as number | null],
    vinelandInternalizante:        [null as number | null],
    vinelandExternalizante:        [null as number | null],
  });

  openModal(): void {
    this.saveError.set(null);
    const p = this.paciente();
    this.form.patchValue({
      vinelandComunicacion:          p.vinelandComunicacion,
      vinelandAutovalimiento:        p.vinelandAutovalimiento,
      vinelandSocial:                p.vinelandSocial,
      vinelandMotor:                 p.vinelandMotor,
      vinelandCocienteFinal:         p.vinelandCocienteFinal,
      vinelandConductaDesadaptativa: p.vinelandConductaDesadaptativa,
      vinelandInternalizante:        p.vinelandInternalizante,
      vinelandExternalizante:        p.vinelandExternalizante,
    });
    this.showModal.set(true);
  }

  save(): void {
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchVineland(this.paciente().id, this.form.value as any).subscribe({
      next: p  => { this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(err.error?.message ?? 'Error al guardar'); this.saving.set(false); },
    });
  }
}
