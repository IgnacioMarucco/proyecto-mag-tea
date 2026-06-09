import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PacienteService } from '../../../../core/services/paciente.service';
import { PacienteResponse } from '../../../../core/models/paciente.model';
import { IconComponent } from '../../../../shared/icon/icon.component';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';

@Component({
  selector: 'app-datos-basicos-section',
  imports: [ReactiveFormsModule, RouterLink, IconComponent, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './datos-basicos-section.component.html',
})
export class DatosBasicosSectionComponent {
  private readonly service = inject(PacienteService);
  private readonly fb      = inject(FormBuilder);

  paciente = input.required<PacienteResponse>();
  updated  = output<PacienteResponse>();

  showModal = signal(false);
  saving    = signal(false);
  saveError = signal<string | null>(null);

  form = this.fb.group({ fechaPrimeraVisita: [''] });

  openModal(): void {
    this.saveError.set(null);
    this.form.patchValue({
      fechaPrimeraVisita: this.paciente().fechaPrimeraVisita?.substring(0, 16) ?? '',
    });
    this.showModal.set(true);
  }

  save(): void {
    if (this.saving()) return;
    const fecha = this.form.value.fechaPrimeraVisita;
    if (!fecha) { this.saveError.set('La fecha es obligatoria'); return; }
    this.saving.set(true);
    this.saveError.set(null);
    this.service.patchPrimeraVisita(this.paciente().id, { fechaPrimeraVisita: fecha }).subscribe({
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

  formatDateTime(dt: string | null | undefined): string {
    if (!dt) return '—';
    return new Date(dt).toLocaleString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }
}
