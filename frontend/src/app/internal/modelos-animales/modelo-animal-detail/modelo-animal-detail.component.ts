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

@Component({
  selector: 'app-modelo-animal-detail',
  imports: [
    RouterLink,
    StatusBadgeComponent,
    PageHeaderComponent,
    DatosBasicosMaSectionComponent,
    VocalizacionesSectionComponent,
    TresCamarasSectionComponent,
    MicroscopiaSectionComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './modelo-animal-detail.component.html',
})
export class ModeloAnimalDetailComponent {
  readonly id = input.required<string>();

  private readonly service    = inject(ModeloAnimalService);
  private readonly route      = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  modeloAnimal = signal<ModeloAnimalResponse | null>(null);
  loading      = signal(true);
  loadError    = signal<string | null>(null);

  readonly crumbs = computed<Crumb[]>(() => {
    const base: Crumb[] = this.route.snapshot.data['crumbs'] ?? [];
    const ma = this.modeloAnimal();
    if (!ma) return base;
    return [...base.slice(0, -1), { label: ma.identificador }];
  });

  readonly rangoColors: Record<string, string> = {
    '1': 'badge-rango1',
    '2': 'badge-rango2',
    '3': 'badge-rango3',
  };
  readonly rangoLabels: Record<string, string> = {
    '1': 'Rango 1', '2': 'Rango 2', '3': 'Rango 3',
  };

  constructor() {
    toObservable(computed(() => Number(this.id()))).pipe(
      filter(id => !!id),
      tap(() => { this.loading.set(true); this.loadError.set(null); }),
      switchMap(id => this.service.findById(id).pipe(catchError(() => of(null)))),
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
