import {
  ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed,
  effect, inject, input, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, filter, map, of, switchMap } from 'rxjs';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ModeloAnimalService } from '../../../core/services/modelo-animal.service';
import { PoolService } from '../../../core/services/pool.service';
import { CamadaService } from '../../../core/services/camada.service';
import {
  ModeloAnimalCreate, ModeloAnimalUpdate,
  ModeloAnimalPoolAporteInput,
} from '../../../core/models/modelo-animal.model';
import { PoolListItem, PoolResponse } from '../../../core/models/pool.model';
import { CamadaListItem } from '../../../core/models/camada.model';
import { extractErrorMessage } from '../../../shared/utils/error.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { FirstFocusDirective } from '../../../shared/directives/first-focus.directive';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { ToastService } from '../../../core/services/toast.service';

interface TuboRow {
  tuboId: number;
  posicion: string;
  cantidadRestante: number;
  selected: boolean;
  cantidadConsumida: number | null;
  dia: number | null;
}

@Component({
  selector: 'app-modelo-animal-form',
  imports: [ReactiveFormsModule, RouterLink, PageHeaderComponent, FirstFocusDirective, ConfirmModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './modelo-animal-form.component.html',
  host: { '(keydown)': 'onKeydown($event)' },
})
export class ModeloAnimalFormComponent {
  private readonly elRef          = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly toast          = inject(ToastService);
  private readonly fb             = inject(FormBuilder);
  private readonly service        = inject(ModeloAnimalService);
  private readonly poolService    = inject(PoolService);
  private readonly camadaService  = inject(CamadaService);
  private readonly router         = inject(Router);
  private readonly route          = inject(ActivatedRoute);
  private readonly destroyRef     = inject(DestroyRef);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  id              = input<string>();
  loading         = signal(false);
  error           = signal<string | null>(null);
  showExitConfirm = signal(false);

  poolCargado  = signal<PoolResponse | null>(null);
  loadingPool  = signal(false);
  tuboRows     = signal<TuboRow[]>([]);

  form = this.fb.group({
    identificador:        ['',                    Validators.required],
    poolId:               [null as number | null,  Validators.required],
    camadaId:             [null as number | null,  Validators.required],
    fechaNacimiento:      ['',                    Validators.required],
    sexo:                 ['',                    Validators.required],
    fechaDia1Inoculacion: ['',                  Validators.required],
  });

  readonly pools = toSignal(
    this.poolService.findAll({ size: 100, sortBy: 'fechaCreacion', sortDir: 'desc' })
      .pipe(map(r => r.content), catchError(() => of([] as PoolListItem[]))),
    { initialValue: [] as PoolListItem[] }
  );

  readonly camadas = toSignal(
    this.camadaService.findAll({ size: 100, sortBy: 'nombre', sortDir: 'asc' })
      .pipe(map(r => r.content), catchError(() => of([] as CamadaListItem[]))),
    { initialValue: [] as CamadaListItem[] }
  );

  readonly aportes = computed<ModeloAnimalPoolAporteInput[]>(() =>
    this.tuboRows()
      .filter(r => r.selected)
      .map(r => ({
        poolTuboId:        r.tuboId,
        cantidadConsumida: r.cantidadConsumida ?? undefined,
        dia:               r.dia ?? undefined,
      }))
  );

  get isEdit(): boolean { return !!this.id(); }

  constructor() {
    const poolIdParam = this.route.snapshot.queryParamMap.get('poolId');
    if (poolIdParam) {
      this.form.patchValue({ poolId: Number(poolIdParam) });
    }

    // Edit mode: load existing data and pre-populate tube rows
    effect(() => {
      const id = this.id();
      if (!id) return;
      this.service.findById(+id).subscribe({
        next: ma => {
          this.form.patchValue({
            identificador:        ma.identificador,
            poolId:               ma.poolId,
            camadaId:             ma.camadaId,
            fechaNacimiento:      ma.fechaNacimiento,
            sexo:                 ma.sexo,
            fechaDia1Inoculacion: ma.fechaDia1Inoculacion,
          }, { emitEvent: false });
          this.loadingPool.set(true);
          this.poolService.findById(ma.poolId).subscribe({
            next: pool => {
              this.loadingPool.set(false);
              this.initTuboRows(pool, (ma.aportes ?? []).map(a => ({
                poolTuboId:        a.poolTuboId,
                cantidadConsumida: a.cantidadConsumida ?? undefined,
                dia:               a.dia ?? undefined,
              })));
            },
            error: () => this.loadingPool.set(false),
          });
        },
        error: () => this.error.set('No se pudo cargar el modelo animal'),
      });
    });

    // New mode: react to pool selector changes
    this.form.get('poolId')!.valueChanges.pipe(
      filter(v => !!v),
      switchMap(v => {
        this.loadingPool.set(true);
        return this.poolService.findById(Number(v)).pipe(catchError(() => of(null)));
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(pool => {
      this.loadingPool.set(false);
      if (pool) this.initTuboRows(pool);
      else { this.poolCargado.set(null); this.tuboRows.set([]); }
    });
  }

  private initTuboRows(pool: PoolResponse, preSelected: ModeloAnimalPoolAporteInput[] = []): void {
    this.poolCargado.set(pool);
    this.tuboRows.set(pool.tubos.map(t => ({
      tuboId:            t.id,
      posicion:          t.posicion,
      cantidadRestante:  t.cantidadRestante,
      selected:          preSelected.some(a => a.poolTuboId === t.id),
      cantidadConsumida: preSelected.find(a => a.poolTuboId === t.id)?.cantidadConsumida ?? null,
      dia:               preSelected.find(a => a.poolTuboId === t.id)?.dia ?? null,
    })));
  }

  toggleTubo(tuboId: number): void {
    this.tuboRows.update(rows =>
      rows.map(r => r.tuboId === tuboId ? { ...r, selected: !r.selected } : r)
    );
  }

  updateTuboCantidad(tuboId: number, event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    const n = val ? Number(val) : null;
    this.tuboRows.update(rows =>
      rows.map(r => r.tuboId === tuboId ? { ...r, cantidadConsumida: n } : r)
    );
  }

  updateTuboDia(tuboId: number, event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    const n = val ? Number(val) : null;
    this.tuboRows.update(rows =>
      rows.map(r => r.tuboId === tuboId ? { ...r, dia: n } : r)
    );
  }

  poolLabel(pool: PoolListItem): string {
    return `Pool #${pool.codigo} — Rango ${pool.rango}`;
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
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const firstInvalid = this.elRef.nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]');
      firstInvalid?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      firstInvalid?.focus();
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const v       = this.form.value;
    const aportes = this.aportes();
    const dto: ModeloAnimalCreate = {
      identificador:        v.identificador!,
      poolId:               Number(v.poolId),
      camadaId:             Number(v.camadaId),
      fechaNacimiento:      v.fechaNacimiento!,
      sexo:                 v.sexo as 'MACHO' | 'HEMBRA',
      fechaDia1Inoculacion: v.fechaDia1Inoculacion!,
      aportes:              aportes.length ? aportes : undefined,
    };

    const id = this.id();
    const req$ = id
      ? this.service.update(+id, dto as ModeloAnimalUpdate)
      : this.service.create(dto);

    req$.subscribe({
      next:  result => { this.toast.show(this.isEdit ? 'Modelo animal actualizado' : 'Modelo animal registrado'); this.router.navigate(['/internal/modelos-animales', result.id]); },
      error: err    => { this.error.set(extractErrorMessage(err, 'Error al guardar')); this.loading.set(false); },
    });
  }

  cancel(): void {
    this.form.reset();
    this.router.navigate(['/internal/modelos-animales']);
  }
}
