import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { filter, switchMap, tap } from 'rxjs';
import { PacienteService } from '../../../core/services/paciente.service';
import { PacienteResponse } from '../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { DatosBasicosSectionComponent } from './sections/datos-basicos-section.component';
import { MchatSectionComponent } from './sections/mchat-section.component';
import { ResultadoMchatSectionComponent } from './sections/resultado-mchat-section.component';
import { CarsSectionComponent } from './sections/cars-section.component';
import { VinelandSectionComponent } from './sections/vineland-section.component';
import { ExtraccionSectionComponent } from './sections/extraccion-section.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';

@Component({
  selector: 'app-paciente-detail',
  imports: [
    RouterLink,
    StatusBadgeComponent,
    PageHeaderComponent,
    DatosBasicosSectionComponent,
    MchatSectionComponent,
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
  private readonly route      = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  paciente  = signal<PacienteResponse | null>(null);
  loading   = signal(true);
  loadError = signal<string | null>(null);

  criterioCumplido      = computed(() => this.paciente()?.criteriosAptitud === 'APTO');
  isControl             = computed(() => this.paciente()?.tipoPaciente === 'CONTROL');
  consentimientoFirmado = computed(() => this.paciente()?.consentimientoFirmado ?? false);
  requiereSeguimiento   = computed(() => this.paciente()?.mchatResultado === 'MEDIANO_RIESGO');

  readonly crumbs = computed<Crumb[]>(() => {
    const base: Crumb[] = this.route.snapshot.data['crumbs'] ?? [];
    const p = this.paciente();
    if (!p) return base;
    return [...base.slice(0, -1), { label: `${p.apellidoNino}, ${p.nombreNino}` }];
  });

  constructor() {
    toObservable(computed(() => Number(this.id()))).pipe(
      filter(id => !!id),
      tap(() => { this.loading.set(true); this.loadError.set(null); }),
      switchMap(id => this.pacienteService.findById(id)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next:  p => { this.paciente.set(p); this.loading.set(false); },
      error: () => { this.loadError.set('No se pudo cargar el paciente.'); this.loading.set(false); },
    });
  }

  onUpdated(p: PacienteResponse): void {
    this.paciente.set(p);
  }
}
