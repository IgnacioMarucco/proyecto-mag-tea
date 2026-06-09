import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { PacienteService } from '../../../../core/services/paciente.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';
import { IconComponent } from '../../../../shared/icon/icon.component';

@Component({
  selector: 'app-criterios-section',
  imports: [ReactiveFormsModule, StatusBadgeComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './criterios-section.component.html',
})
export class CriteriosSectionComponent {
  private readonly service = inject(PacienteService);
  private readonly fb      = inject(FormBuilder);

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

  readonly criteriosEstado = computed(() => {
    const p = this.paciente();
    if (!p.criteriosRegistrados) return null;
    const hasExclusion =
      p.epilepsia || p.paralisisCerebral || p.infeccionesCongenitas ||
      p.lesionesEstructuralesSNC || p.facomatosis || p.patologiasNeurometabolicas ||
      p.lesionesOcupantesEspacioSNC || p.patologiaPsiquiatrica ||
      p.otrosSindromesGeneticos || p.pubertadPrecoz;
    if (hasExclusion) return 'EXCLUIDO';
    if (p.criterioTEADSMV && p.criterioTGDDSMIV && p.criterioEdad) return 'APTO';
    return 'INCOMPLETO';
  });

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
    // Preserva consentimientoFirmado actual para no sobreescribirlo
    const dto = { ...this.form.value, consentimientoFirmado: this.paciente().consentimientoFirmado } as any;
    this.service.patchCriterios(this.paciente().id, dto).subscribe({
      next: p  => { this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(err.error?.message ?? 'Error al guardar'); this.saving.set(false); },
    });
  }
}
