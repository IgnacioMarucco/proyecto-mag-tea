import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { PacienteService } from '../../../../core/services/paciente.service';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { ToastService } from '../../../../core/services/toast.service';
import { PacienteResponse, PacienteVineland } from '../../../../core/models/paciente.model';
import { ModalContainerComponent } from '../../../../shared/modal-container/modal-container.component';

@Component({
  selector: 'app-vineland-section',
  imports: [ReactiveFormsModule, ModalContainerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './vineland-section.component.html',
})
export class VinelandSectionComponent {
  private readonly service = inject(PacienteService);
  private readonly fb      = inject(FormBuilder);
  private readonly toast   = inject(ToastService);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({
    comunicacion:          [null as number | null],
    autovalimiento:        [null as number | null],
    social:                [null as number | null],
    motor:                 [null as number | null],
    cocienteFinal:         [null as number | null],
    conductaDesadaptativa: [null as number | null],
    internalizante:        [null as number | null],
    externalizante:        [null as number | null],
  });

  openModal(): void {
    this.saveError.set(null);
    const p = this.paciente();
    this.form.patchValue({
      comunicacion:          p.vinelandComunicacion,
      autovalimiento:        p.vinelandAutovalimiento,
      social:                p.vinelandSocial,
      motor:                 p.vinelandMotor,
      cocienteFinal:         p.vinelandCocienteFinal,
      conductaDesadaptativa: p.vinelandConductaDesadaptativa,
      internalizante:        p.vinelandInternalizante,
      externalizante:        p.vinelandExternalizante,
    });
    this.showModal.set(true);
  }

  save(): void {
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchVineland(this.paciente().codigoNumerico, this.form.getRawValue() as PacienteVineland).subscribe({
      next: p  => { this.toast.show('Vineland guardado'); this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }
}
