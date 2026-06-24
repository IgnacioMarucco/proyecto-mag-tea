import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { startWith } from 'rxjs';
import { PacienteService } from '../../../../core/services/paciente.service';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { ToastService } from '../../../../core/services/toast.service';
import { PacienteResponse, PacienteCriterios } from '../../../../core/models/paciente.model';
import { ModalContainerComponent } from '../../../../shared/modal-container/modal-container.component';

@Component({
  selector: 'app-criterios-section',
  imports: [ReactiveFormsModule, ModalContainerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './criterios-section.component.html',
})
export class CriteriosSectionComponent {
  private readonly service = inject(PacienteService);
  private readonly fb      = inject(FormBuilder);
  private readonly toast   = inject(ToastService);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({
    criterioTEADSMV:             [false],
    criterioTGDDSMIV:            [false],
    criterioEdad:                [false],
    epilepsia:                   [false],
    paralisisCerebral:           [false],
    infeccionesCongenitas:       [false],
    lesionesEstructuralesSNC:    [false],
    facomatosis:                 [false],
    patologiasNeurometabolicas:  [false],
    lesionesOcupantesEspacioSNC: [false],
    patologiaPsiquiatrica:       [false],
    otrosSindromesGeneticos:     [false],
    pubertadPrecoz:              [false],
  });

  private readonly formValue = toSignal(
    this.form.valueChanges.pipe(startWith(this.form.value)),
    { initialValue: this.form.value }
  );

  protected fieldValue(key: string): boolean {
    return !!((this.formValue() as Record<string, unknown>)[key]);
  }

  readonly criteriosEstado = () => this.paciente().criteriosRegistrados
    ? this.paciente().criteriosAptitud
    : null;

  readonly estadoLabels: Record<string, string> = {
    APTO: 'Apto para el protocolo', EXCLUIDO: 'Excluido del protocolo', INCOMPLETO: 'Criterios incompletos',
  };
  readonly estadoColors: Record<string, string> = {
    APTO: 'bg-accent-light text-accent',
    EXCLUIDO: 'bg-danger-light text-danger',
    INCOMPLETO: 'bg-warning-light text-warning',
  };

  openModal(): void {
    this.saveError.set(null);
    const p = this.paciente();
    this.form.patchValue({
      criterioTEADSMV:             p.criterioTEADSMV,
      criterioTGDDSMIV:            p.criterioTGDDSMIV,
      criterioEdad:                p.criterioEdad,
      epilepsia:                   p.epilepsia,
      paralisisCerebral:           p.paralisisCerebral,
      infeccionesCongenitas:       p.infeccionesCongenitas,
      lesionesEstructuralesSNC:    p.lesionesEstructuralesSNC,
      facomatosis:                 p.facomatosis,
      patologiasNeurometabolicas:  p.patologiasNeurometabolicas,
      lesionesOcupantesEspacioSNC: p.lesionesOcupantesEspacioSNC,
      patologiaPsiquiatrica:       p.patologiaPsiquiatrica,
      otrosSindromesGeneticos:     p.otrosSindromesGeneticos,
      pubertadPrecoz:              p.pubertadPrecoz,
    });
    this.showModal.set(true);
  }

  save(): void {
    if (this.saving()) return;
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchCriterios(this.paciente().codigoNumerico, this.form.getRawValue() as PacienteCriterios).subscribe({
      next: p  => { this.toast.show('Criterios guardados'); this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }
}
