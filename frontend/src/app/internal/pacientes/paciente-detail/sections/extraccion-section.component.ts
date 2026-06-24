import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PacienteService } from '../../../../core/services/paciente.service';
import { AuthService } from '../../../../core/services/auth.service';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { ToastService } from '../../../../core/services/toast.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { ModalContainerComponent } from '../../../../shared/modal-container/modal-container.component';
import { FechaPipe } from '../../../../core/pipes/fecha.pipe';

@Component({
  selector: 'app-extraccion-section',
  imports: [ReactiveFormsModule, RouterLink, ModalContainerComponent, FechaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './extraccion-section.component.html',
})
export class ExtraccionSectionComponent {
  private readonly service  = inject(PacienteService);
  private readonly auth     = inject(AuthService);
  private readonly fb       = inject(FormBuilder);
  private readonly toast    = inject(ToastService);

  readonly isInvestigadorPrincipal = computed(
    () => this.auth.currentUser()?.role === 'INVESTIGADOR_PRINCIPAL'
  );

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({ fechaTurnoExtraccion: [''] });

  openModal(): void {
    this.saveError.set(null);
    this.form.patchValue({ fechaTurnoExtraccion: this.paciente().fechaTurnoExtraccion?.substring(0, 16) ?? '' });
    this.showModal.set(true);
  }

  save(): void {
    if (this.saving()) return;
    const fecha = this.form.value.fechaTurnoExtraccion;
    if (!fecha) { this.saveError.set('La fecha de extracción es obligatoria'); return; }
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchSegundaVisita(this.paciente().codigoNumerico, { fechaTurnoExtraccion: fecha }).subscribe({
      next: p  => { this.toast.show('Turno de extracción guardado'); this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(extractErrorMessage(err, 'Error al guardar')); this.saving.set(false); },
    });
  }

}
