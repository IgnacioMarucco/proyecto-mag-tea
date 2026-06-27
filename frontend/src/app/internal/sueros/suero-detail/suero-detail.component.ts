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
import { SueroService } from '../../../core/services/suero.service';
import { SueroResponse } from '../../../core/models/suero.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { CopyBadgeComponent } from '../../../shared/copy-badge/copy-badge.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { MlPipe } from '../../../core/pipes/ml.pipe';
import { RANGO_COLORS, RANGO_LABELS, USO_COLORS, USO_LABELS } from '../../../shared/utils/btu.utils';

@Component({
  selector: 'app-suero-detail',
  imports: [RouterLink, StatusBadgeComponent, CopyBadgeComponent, PageHeaderComponent, FechaPipe, MlPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './suero-detail.component.html',
})
export class SueroDetailComponent {
  readonly codigo = input.required<string>();

  private readonly sueroService = inject(SueroService);
  private readonly route        = inject(ActivatedRoute);
  private readonly destroyRef   = inject(DestroyRef);

  suero     = signal<SueroResponse | null>(null);
  loading   = signal(true);
  loadError = signal<string | null>(null);

  readonly rangoLabels = RANGO_LABELS;
  readonly rangoColors = RANGO_COLORS;
  readonly usoLabels   = USO_LABELS;
  readonly usoColors   = USO_COLORS;

  readonly crumbs = computed<Crumb[]>(() => {
    const base: Crumb[] = this.route.snapshot.data['crumbs'] ?? [];
    const s = this.suero();
    if (!s) return base;
    return [...base.slice(0, -1), { label: s.codigoNumerico }];
  });

  formatBtu(val: number | null | undefined): string {
    return val != null ? val.toLocaleString('es-AR') : '—';
  }

  constructor() {
    toObservable(this.codigo).pipe(
      filter(codigo => !!codigo),
      tap(() => { this.loading.set(true); this.loadError.set(null); }),
      switchMap(codigo => this.sueroService.findByCodigo(codigo)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next:  s => { this.suero.set(s); this.loading.set(false); },
      error: () => { this.loadError.set('No se pudo cargar el suero.'); this.loading.set(false); },
    });
  }
}
