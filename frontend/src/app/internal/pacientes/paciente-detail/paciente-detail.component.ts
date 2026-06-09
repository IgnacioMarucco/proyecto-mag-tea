import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { PacienteService } from '../../../core/services/paciente.service';
import { PacienteResponse } from '../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { DatosBasicosSectionComponent } from './sections/datos-basicos-section.component';
import { ConsentimientoSectionComponent } from './sections/consentimiento-section.component';
import { MchatSectionComponent } from './sections/mchat-section.component';
import { CriteriosSectionComponent } from './sections/criterios-section.component';
import { ResultadoMchatSectionComponent } from './sections/resultado-mchat-section.component';
import { CarsSectionComponent } from './sections/cars-section.component';
import { VinelandSectionComponent } from './sections/vineland-section.component';
import { ExtraccionSectionComponent } from './sections/extraccion-section.component';

@Component({
  selector: 'app-paciente-detail',
  imports: [
    RouterLink,
    StatusBadgeComponent,
    IconComponent,
    DatosBasicosSectionComponent,
    ConsentimientoSectionComponent,
    MchatSectionComponent,
    CriteriosSectionComponent,
    ResultadoMchatSectionComponent,
    CarsSectionComponent,
    VinelandSectionComponent,
    ExtraccionSectionComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './paciente-detail.component.html',
})
export class PacienteDetailComponent {
  readonly id = input.required<string>();

  private readonly pacienteService = inject(PacienteService);

  paciente  = signal<PacienteResponse | null>(null);
  loading   = signal(true);
  loadError = signal<string | null>(null);

  criterioCumplido = computed(() => {
    const p = this.paciente();
    if (!p || !p.criteriosRegistrados) return false;
    const inclusion = p.criterioTEADSMV && p.criterioTGDDSMIV && p.criterioEdad;
    const exclusion =
      p.epilepsia || p.paralisisCerebral || p.infeccionesCongenitas ||
      p.lesionesEstructuralesSNC || p.facomatosis || p.patologiasNeurometabolicas ||
      p.lesionesOcupantesEspacioSNC || p.patologiaPsiquiatrica ||
      p.otrosSindromesGeneticos || p.pubertadPrecoz;
    return !!(inclusion && !exclusion);
  });

  constructor() {
    effect(() => {
      const numId = Number(this.id());
      if (!numId) return;
      this.loading.set(true);
      this.pacienteService.findById(numId).subscribe({
        next:  p => { this.paciente.set(p); this.loading.set(false); },
        error: () => { this.loadError.set('No se pudo cargar el paciente.'); this.loading.set(false); },
      });
    });
  }

  onUpdated(p: PacienteResponse): void {
    this.paciente.set(p);
  }
}
