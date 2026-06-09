import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { PacienteService } from '../../../../core/services/paciente.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { IconComponent } from '../../../../shared/icon/icon.component';

@Component({
  selector: 'app-extraccion-section',
  imports: [ReactiveFormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './extraccion-section.component.html',
})
export class ExtraccionSectionComponent {
  private readonly service = inject(PacienteService);
  private readonly fb      = inject(FormBuilder);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({ fechaExtraccion: [''] });

  openModal(): void {
    this.saveError.set(null);
    this.form.patchValue({ fechaExtraccion: this.paciente().fechaExtraccion ?? '' });
    this.showModal.set(true);
  }

  save(): void {
    if (this.saving()) return;
    const fecha = this.form.value.fechaExtraccion;
    if (!fecha) { this.saveError.set('La fecha de extracción es obligatoria'); return; }
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchSegundaVisita(this.paciente().id, { fechaExtraccion: fecha }).subscribe({
      next: p  => { this.updated.emit(p); this.showModal.set(false); this.saving.set(false); },
      error: err => { this.saveError.set(err.error?.message ?? 'Error al guardar'); this.saving.set(false); },
    });
  }

  formatDate(date: string | null | undefined): string {
    if (!date) return '—';
    return new Date(date + 'T00:00:00').toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  }
}
