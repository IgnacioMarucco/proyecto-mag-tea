import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { filter, map, switchMap, tap } from 'rxjs';
import { PacienteService } from '../../../core/services/paciente.service';
import { PacienteResponse } from '../../../core/models/paciente.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { CopyBadgeComponent } from '../../../shared/copy-badge/copy-badge.component';
import { DatosBasicosSectionComponent } from './sections/datos-basicos-section.component';
import { MchatSectionComponent } from './sections/mchat-section.component';
import { ResultadoMchatSectionComponent } from './sections/resultado-mchat-section.component';
import { CarsSectionComponent } from './sections/cars-section.component';
import { VinelandSectionComponent } from './sections/vineland-section.component';
import { ExtraccionSectionComponent } from './sections/extraccion-section.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { IconComponent } from '../../../shared/icon/icon.component';

@Component({
  selector: 'app-paciente-detail',
  imports: [
    RouterLink,
    StatusBadgeComponent,
    CopyBadgeComponent,
    PageHeaderComponent,
    DatosBasicosSectionComponent,
    MchatSectionComponent,
    ResultadoMchatSectionComponent,
    CarsSectionComponent,
    VinelandSectionComponent,
    ExtraccionSectionComponent,
    IconComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './paciente-detail.component.html',
})
export class PacienteDetailComponent {
  readonly codigo = input.required<string>();

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

  readonly protocolSteps = computed(() => {
    const p = this.paciente();
    if (!p) return [];
    const estado = p.pacienteEstado;
    const control = this.isControl();

    const raw: { label: string; done: boolean }[] = control
      ? [
          { label: 'Admisión',   done: true },
          { label: '1ª visita',  done: p.fechaTurnoExtraccion != null },
          { label: 'Extracción', done: estado === 'EXTRACCION_REALIZADA' },
        ]
      : [
          { label: 'Admisión',   done: true },
          { label: 'M-CHAT',     done: ['MCHAT_RESPONDIDO', 'EXTRACCION_PENDIENTE', 'EXTRACCION_REALIZADA'].includes(estado) },
          { label: '1ª visita',  done: p.fechaTurnoExtraccion != null },
          { label: 'Extracción', done: estado === 'EXTRACCION_REALIZADA' },
        ];

    let foundCurrent = false;
    return raw.map(s => {
      if (s.done) return { ...s, state: 'done' as const };
      if (!foundCurrent) { foundCurrent = true; return { ...s, state: 'current' as const }; }
      return { ...s, state: 'upcoming' as const };
    });
  });

  private readonly baseCrumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  readonly crumbs = computed<Crumb[]>(() => {
    const base = this.baseCrumbs();
    const p = this.paciente();
    if (!p) return base;
    return [...base.slice(0, -1), { label: `${p.apellidoNino}, ${p.nombreNino}` }];
  });

  constructor() {
    toObservable(this.codigo).pipe(
      filter(codigo => !!codigo),
      tap(() => { this.loading.set(true); this.loadError.set(null); }),
      switchMap(codigo => this.pacienteService.findDetail(codigo)),
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
