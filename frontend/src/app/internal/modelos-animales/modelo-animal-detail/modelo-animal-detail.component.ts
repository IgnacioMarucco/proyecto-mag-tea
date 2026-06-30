import {
  ChangeDetectionStrategy, Component, DestroyRef, computed, inject, input, signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, filter, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ModeloAnimalService } from '../../../core/services/modelo-animal.service';
import { ModeloAnimalResponse } from '../../../core/models/modelo-animal.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { DatosBasicosMaSectionComponent } from './sections/datos-basicos-ma-section.component';
import { VocalizacionesSectionComponent } from './sections/vocalizaciones-section.component';
import { TresCamarasSectionComponent } from './sections/tres-camaras-section.component';
import { MicroscopiaSectionComponent } from './sections/microscopia-section.component';
import { InoculacionSectionComponent } from './sections/inoculacion-section.component';
import { CopyBadgeComponent } from '../../../shared/copy-badge/copy-badge.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { RANGO_COLORS, RANGO_LABELS, USO_COLORS, USO_LABELS } from '../../../shared/utils/btu.utils';

@Component({
  selector: 'app-modelo-animal-detail',
  imports: [
    RouterLink,
    StatusBadgeComponent,
    PageHeaderComponent,
    DatosBasicosMaSectionComponent,
    InoculacionSectionComponent,
    VocalizacionesSectionComponent,
    TresCamarasSectionComponent,
    MicroscopiaSectionComponent,
    CopyBadgeComponent,
    IconComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './modelo-animal-detail.component.html',
})
export class ModeloAnimalDetailComponent {
  readonly identificador = input.required<string>();

  private readonly service    = inject(ModeloAnimalService);
  private readonly route      = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  modeloAnimal = signal<ModeloAnimalResponse | null>(null);
  loading      = signal(true);
  loadError    = signal<string | null>(null);

  readonly protocolSteps = computed(() => {
    const ma = this.modeloAnimal();
    if (!ma) return [];
    const e = ma.estadoProtocolo;

    const raw: { label: string; done: boolean }[] = [
      { label: 'Inoculación',    done: e !== 'PENDIENTE_INOCULACION' && e !== 'INOCULACION_EN_CURSO' },
      { label: 'Vocalizaciones', done: e === 'PENDIENTE_TRES_CAMARAS' || e === 'PENDIENTE_MICROSCOPIA' || e === 'COMPLETO' },
      { label: 'Tres cámaras',   done: e === 'PENDIENTE_MICROSCOPIA' || e === 'COMPLETO' },
      { label: 'Microscopía',    done: e === 'COMPLETO' },
    ];

    let foundCurrent = false;
    return raw.map(s => {
      if (s.done) return { ...s, state: 'done' as const };
      if (!foundCurrent) { foundCurrent = true; return { ...s, state: 'current' as const }; }
      return { ...s, state: 'upcoming' as const };
    });
  });

  readonly crumbs = computed<Crumb[]>(() => {
    const base: Crumb[] = this.route.snapshot.data['crumbs'] ?? [];
    const ma = this.modeloAnimal();
    if (!ma) return base;
    return [...base.slice(0, -1), { label: ma.identificador }];
  });

  readonly rangoColors = RANGO_COLORS;
  readonly rangoLabels = RANGO_LABELS;
  readonly usoColors   = USO_COLORS;
  readonly usoLabels   = USO_LABELS;

  constructor() {
    toObservable(this.identificador).pipe(
      filter(id => !!id),
      tap(() => { this.loading.set(true); this.loadError.set(null); }),
      switchMap(id => this.service.findByCode(id).pipe(catchError(() => of(null)))),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(ma => {
      this.modeloAnimal.set(ma);
      this.loading.set(false);
      if (!ma) this.loadError.set('No se pudo cargar el modelo animal.');
    });
  }

  onUpdated(ma: ModeloAnimalResponse): void {
    this.modeloAnimal.set(ma);
  }
}
