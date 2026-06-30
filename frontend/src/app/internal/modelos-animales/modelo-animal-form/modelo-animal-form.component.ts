import {
  ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed,
  inject, input, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EMPTY, catchError, filter, map, of, switchMap } from 'rxjs';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ModeloAnimalService } from '../../../core/services/modelo-animal.service';
import { PoolService } from '../../../core/services/pool.service';
import {
  ModeloAnimalCreate, ModeloAnimalUpdate,
} from '../../../core/models/modelo-animal.model';
import { PoolListItem } from '../../../core/models/pool.model';
import { extractErrorMessage } from '../../../shared/utils/error.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { FirstFocusDirective } from '../../../shared/directives/first-focus.directive';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { ToastService } from '../../../core/services/toast.service';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { MlPipe } from '../../../core/pipes/ml.pipe';
import { SueroUso } from '../../../core/models/suero.model';
import { RANGO_COLORS, RANGO_LABELS, USO_COLORS, USO_LABELS } from '../../../shared/utils/btu.utils';
import { CamadaPickerComponent } from '../../../shared/camada-picker/camada-picker.component';

@Component({
  selector: 'app-modelo-animal-form',
  imports: [ReactiveFormsModule, PageHeaderComponent, FirstFocusDirective,
            ConfirmModalComponent, StatusBadgeComponent, MlPipe,
            CamadaPickerComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './modelo-animal-form.component.html',
  host: { '(keydown)': 'onKeydown($event)' },
})
export class ModeloAnimalFormComponent {
  private readonly elRef       = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly toast       = inject(ToastService);
  private readonly fb          = inject(FormBuilder);
  private readonly service     = inject(ModeloAnimalService);
  private readonly poolService = inject(PoolService);
  private readonly router      = inject(Router);
  private readonly route       = inject(ActivatedRoute);
  private readonly destroyRef  = inject(DestroyRef);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  identificador   = input<string>();
  loading         = signal(false);
  error           = signal<string | null>(null);
  showExitConfirm = signal(false);
  submitted       = signal(false);
  initialCamadaId = signal<number | null>(null);

  step          = signal<1 | 2>(1);
  filterUso     = signal<SueroUso | null>(null);
  filterRango   = signal<number | null>(null);
  selectedPool  = signal<PoolListItem | null>(null);

  readonly filteredPools = computed(() => {
    const uso   = this.filterUso();
    const rango = this.filterRango();
    return this.pools().filter(p =>
      (!uso   || p.uso   === uso) &&
      (rango === null || p.rango === rango)
    );
  });

  readonly rangoColors = RANGO_COLORS;
  readonly rangoLabels = RANGO_LABELS;
  readonly usoColors   = USO_COLORS;
  readonly usoLabels   = USO_LABELS;

  private editId: number | null = null;

  form = this.fb.group({
    poolId:   [null as number | null, Validators.required],
    camadaId: [null as number | null, Validators.required],
    sexo:     ['',                    Validators.required],
  });

  readonly pools = toSignal(
    this.poolService.findAll({ size: 100, sortBy: 'fechaCreacion', sortDir: 'desc' })
      .pipe(map(r => r.content), catchError(() => of([] as PoolListItem[]))),
    { initialValue: [] as PoolListItem[] }
  );

  readonly isEdit = computed(() => !!this.identificador());

  constructor() {
    const uso   = this.route.snapshot.queryParamMap.get('uso');
    const rango = this.route.snapshot.queryParamMap.get('rango');
    if (uso)                           this.filterUso.set(uso as SueroUso);
    if (rango != null && rango !== '') this.filterRango.set(Number(rango));

    toObservable(this.identificador).pipe(
      filter((id): id is string => !!id),
      switchMap(id => this.service.findByCode(id).pipe(
        catchError(() => { this.error.set('No se pudo cargar el modelo animal'); return EMPTY; })
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(ma => {
      this.editId = ma.id;
      this.initialCamadaId.set(ma.camadaId);
      this.form.patchValue({ poolId: ma.poolId, camadaId: ma.camadaId, sexo: ma.sexo }, { emitEvent: false });
    });
  }

  poolLabel(pool: PoolListItem): string {
    return pool.codigo;
  }

  onCamadaChange(id: number | null): void {
    this.form.patchValue({ camadaId: id });
  }

  selectPool(pool: PoolListItem): void {
    this.selectedPool.set(pool);
  }

  avanzarAStep2(): void {
    const pool = this.selectedPool();
    if (!pool) return;
    this.form.patchValue({ poolId: pool.id });
    this.step.set(2);
  }

  volverAStep1(): void {
    this.step.set(1);
  }

  onKeydown(event: KeyboardEvent): void {
    if ((event.ctrlKey || event.metaKey) && event.key === 's') {
      event.preventDefault();
      this.onSubmit();
    }
    if (event.key === 'Escape') this.handleEscape();
  }

  handleEscape(): void {
    if (this.form.dirty) this.showExitConfirm.set(true);
    else this.cancel();
  }

  confirmExit(): void {
    this.showExitConfirm.set(false);
    this.cancel();
  }

  onSubmit(): void {
    this.submitted.set(true);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const firstInvalid = this.elRef.nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]');
      firstInvalid?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      firstInvalid?.focus();
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const v = this.form.value;
    const dto: ModeloAnimalCreate = {
      poolId:   Number(v.poolId),
      camadaId: Number(v.camadaId),
      sexo:     v.sexo as 'MACHO' | 'HEMBRA',
    };

    const editId = this.editId;
    const req$ = editId
      ? this.service.update(editId, dto as ModeloAnimalUpdate)
      : this.service.create(dto);

    req$.subscribe({
      next:  result => {
        this.toast.show(this.isEdit() ? 'Modelo animal actualizado' : 'Modelo animal registrado');
        this.router.navigate(['/internal/modelos-animales', result.identificador]);
      },
      error: err => { this.error.set(extractErrorMessage(err, 'Error al guardar')); this.loading.set(false); },
    });
  }

  cancel(): void {
    this.form.reset();
    this.router.navigate(['/internal/modelos-animales']);
  }
}
