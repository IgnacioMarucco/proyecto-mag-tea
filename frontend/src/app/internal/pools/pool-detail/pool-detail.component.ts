import {
  ChangeDetectionStrategy, Component, DestroyRef, computed, inject, input, signal,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, filter, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { PoolService } from '../../../core/services/pool.service';
import { ModeloAnimalService } from '../../../core/services/modelo-animal.service';
import { PoolResponse } from '../../../core/models/pool.model';
import { ModeloAnimalListItem } from '../../../core/models/modelo-animal.model';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { MlPipe } from '../../../core/pipes/ml.pipe';

@Component({
  selector: 'app-pool-detail',
  imports: [RouterLink, StatusBadgeComponent, ConfirmModalComponent, PageHeaderComponent, MlPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pool-detail.component.html',
})
export class PoolDetailComponent {
  readonly id = input.required<string>();

  private readonly poolService         = inject(PoolService);
  private readonly modeloAnimalService = inject(ModeloAnimalService);
  private readonly router              = inject(Router);
  private readonly route               = inject(ActivatedRoute);
  private readonly destroyRef          = inject(DestroyRef);

  pool            = signal<PoolResponse | null>(null);
  modelosAnimales = signal<ModeloAnimalListItem[]>([]);
  loading         = signal(true);
  loadError       = signal<string | null>(null);
  deleting        = signal(false);
  pendingDelete   = signal(false);

  readonly crumbs = computed<Crumb[]>(() => {
    const base: Crumb[] = this.route.snapshot.data['crumbs'] ?? [];
    const p = this.pool();
    if (!p) return base;
    const usoLabel = p.uso === 'CONTROL' ? 'Control' : 'Problema';
    return [...base.slice(0, -1), { label: `Pool #${p.codigo} — ${usoLabel} R${p.rango}` }];
  });

  readonly rangoColors: Record<string, string> = {
    '0': 'badge-rango0', '1': 'badge-rango1', '2': 'badge-rango2', '3': 'badge-rango3',
  };
  readonly rangoLabels: Record<string, string> = {
    '0': 'Rango 0', '1': 'Rango 1', '2': 'Rango 2', '3': 'Rango 3',
  };

  readonly usoColors: Record<string, string> = {
    'CONTROL':  'bg-background text-text-muted border border-border',
    'PROBLEMA': 'bg-primary-light text-primary',
  };
  readonly usoLabels: Record<string, string> = {
    'CONTROL': 'Control', 'PROBLEMA': 'Caso problema',
  };

  readonly sexoLabels: Record<string, string>  = { MACHO: 'Macho', HEMBRA: 'Hembra' };
  readonly sexoColors: Record<string, string>  = {
    MACHO:   'bg-primary-light text-primary',
    HEMBRA:  'bg-accent-light text-accent',
  };

  constructor() {
    toObservable(computed(() => Number(this.id()))).pipe(
      filter(id => !!id),
      tap(() => { this.loading.set(true); this.loadError.set(null); }),
      switchMap(id => this.poolService.findById(id).pipe(
        catchError(() => of(null))
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(pool => {
      this.pool.set(pool);
      this.loading.set(false);
      if (!pool) { this.loadError.set('No se pudo cargar el pool.'); return; }

      this.modeloAnimalService.findAll({ poolId: pool.id, size: 50 }).pipe(
        catchError(() => of(null)),
      ).subscribe(r => {
        if (r) this.modelosAnimales.set(r.content);
      });
    });
  }


  formatDate(date: string): string {
    return new Date(date + 'T00:00:00').toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  }

  requestDelete(): void  { this.pendingDelete.set(true); }
  cancelDelete(): void   { this.pendingDelete.set(false); }

  confirmDelete(): void {
    const p = this.pool();
    if (!p) return;
    this.pendingDelete.set(false);
    this.deleting.set(true);
    this.poolService.delete(p.id).subscribe({
      next:  () => this.router.navigate(['/internal/pools']),
      error: () => this.deleting.set(false),
    });
  }
}
