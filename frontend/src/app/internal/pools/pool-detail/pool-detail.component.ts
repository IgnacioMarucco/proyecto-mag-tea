import {
  ChangeDetectionStrategy, Component, DestroyRef, computed, inject, input, signal,
} from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, filter, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { PoolService } from '../../../core/services/pool.service';
import { ModeloAnimalService } from '../../../core/services/modelo-animal.service';
import { PoolResponse } from '../../../core/models/pool.model';
import { ModeloAnimalListItem } from '../../../core/models/modelo-animal.model';
import { CopyBadgeComponent } from '../../../shared/copy-badge/copy-badge.component';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { MlPipe } from '../../../core/pipes/ml.pipe';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { RANGO_COLORS, RANGO_LABELS, USO_COLORS, USO_LABELS } from '../../../shared/utils/btu.utils';

@Component({
  selector: 'app-pool-detail',
  imports: [RouterLink, CopyBadgeComponent, StatusBadgeComponent, PageHeaderComponent, MlPipe, FechaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pool-detail.component.html',
})
export class PoolDetailComponent {
  readonly codigo = input.required<string>();

  private readonly poolService         = inject(PoolService);
  private readonly modeloAnimalService = inject(ModeloAnimalService);
  private readonly route               = inject(ActivatedRoute);
  private readonly destroyRef          = inject(DestroyRef);

  pool            = signal<PoolResponse | null>(null);
  modelosAnimales = signal<ModeloAnimalListItem[]>([]);
  loading         = signal(true);
  loadError       = signal<string | null>(null);

  readonly crumbs = computed<Crumb[]>(() => {
    const base: Crumb[] = this.route.snapshot.data['crumbs'] ?? [];
    const p = this.pool();
    if (!p) return base;
    return [...base.slice(0, -1), { label: p.codigo }];
  });

  readonly rangoColors = RANGO_COLORS;
  readonly rangoLabels = RANGO_LABELS;
  readonly usoColors   = USO_COLORS;
  readonly usoLabels   = USO_LABELS;

  constructor() {
    toObservable(this.codigo).pipe(
      filter(codigo => !!codigo),
      tap(() => { this.loading.set(true); this.loadError.set(null); }),
      switchMap(codigo => this.poolService.findByCodigo(codigo).pipe(
        catchError(() => of(null))
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(pool => {
      this.pool.set(pool);
      this.loading.set(false);
      if (!pool) { this.loadError.set('No se pudo cargar el pool.'); return; }

      this.modeloAnimalService.findAll({ poolId: pool.id, size: 50, sortBy: 'createdAt', sortDir: 'desc' }).pipe(
        catchError(() => of(null)),
      ).subscribe(r => {
        if (r) this.modelosAnimales.set(r.content);
      });
    });
  }

}
