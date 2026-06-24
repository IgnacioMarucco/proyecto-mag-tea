import {
  ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed,
  inject, input, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EMPTY, catchError, filter, map, of, switchMap } from 'rxjs';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ModeloAnimalService } from '../../../core/services/modelo-animal.service';
import { CamadaService } from '../../../core/services/camada.service';
import { PoolService } from '../../../core/services/pool.service';
import {
  ModeloAnimalCreate, ModeloAnimalUpdate,
} from '../../../core/models/modelo-animal.model';
import { PoolListItem } from '../../../core/models/pool.model';
import { CamadaListItem, CamadaCreate } from '../../../core/models/camada.model';
import { extractErrorMessage } from '../../../shared/utils/error.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { FirstFocusDirective } from '../../../shared/directives/first-focus.directive';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { ToastService } from '../../../core/services/toast.service';
import { IconComponent } from '../../../shared/icon/icon.component';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';
import { MlPipe } from '../../../core/pipes/ml.pipe';
import { SueroUso } from '../../../core/models/suero.model';

@Component({
  selector: 'app-modelo-animal-form',
  imports: [ReactiveFormsModule, PageHeaderComponent, FirstFocusDirective,
            ConfirmModalComponent, IconComponent, StatusBadgeComponent, MlPipe],
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

  identificador   = input<string>();
  loading         = signal(false);
  error           = signal<string | null>(null);
  showExitConfirm = signal(false);

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
    'CONTROL': 'Caso Control', 'PROBLEMA': 'Caso Problema',
  };

  showCamadaModal    = signal(false);
  camadaModalLoading = signal(false);
  camadaModalError   = signal<string | null>(null);
  camadas            = signal<CamadaListItem[]>([]);

  private editId: number | null = null;

  form = this.fb.group({
    poolId:   [null as number | null, Validators.required],
    camadaId: [null as number | null, Validators.required],
    sexo:     ['',                    Validators.required],
  });

  camadaForm = this.fb.group({
    nombre:          ['', [Validators.required, Validators.maxLength(50)]],
    fechaNacimiento: ['',  Validators.required],
  });

  readonly pools = toSignal(
    this.poolService.findAll({ size: 100, sortBy: 'fechaCreacion', sortDir: 'desc' })
      .pipe(map(r => r.content), catchError(() => of([] as PoolListItem[]))),
    { initialValue: [] as PoolListItem[] }
  );

  readonly isEdit = computed(() => !!this.identificador());

  constructor() {
    this.loadCamadas();

    const uso   = this.route.snapshot.queryParamMap.get('uso');
    const rango = this.route.snapshot.queryParamMap.get('rango');
    if (uso)                       this.filterUso.set(uso as SueroUso);
    if (rango != null && rango !== '') this.filterRango.set(Number(rango));

    toObservable(this.identificador).pipe(
      filter((id): id is string => !!id),
      switchMap(id => this.service.findByCode(id).pipe(
        catchError(() => { this.error.set('No se pudo cargar el modelo animal'); return EMPTY; })
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(ma => {
      this.editId = ma.id;
      this.form.patchValue({ poolId: ma.poolId, camadaId: ma.camadaId, sexo: ma.sexo }, { emitEvent: false });
    });
  }

  private loadCamadas(): void {
    this.camadaService.findAll({ size: 100, sortBy: 'fechaNacimiento', sortDir: 'desc' })
      .pipe(map(r => r.content), catchError(() => of([] as CamadaListItem[])))
      .subscribe(list => this.camadas.set(list));
  }

  poolLabel(pool: PoolListItem): string {
    return pool.codigo;
  }

  camadaLabel(camada: CamadaListItem): string {
    if (!camada.fechaNacimiento) return camada.nombre;
    const d = new Date(camada.fechaNacimiento + 'T00:00:00');
    return `${camada.nombre} — ${d.toLocaleDateString('es-AR')}`;
  }

  openCamadaModal(): void {
    this.camadaForm.reset();
    this.camadaModalError.set(null);
    this.showCamadaModal.set(true);
  }

  closeCamadaModal(): void { this.showCamadaModal.set(false); }

  guardarCamada(): void {
    if (this.camadaForm.invalid) { this.camadaForm.markAllAsTouched(); return; }
    this.camadaModalLoading.set(true);
    this.camadaModalError.set(null);
    const v = this.camadaForm.value;
    const dto: CamadaCreate = {
      nombre:          v.nombre!,
      fechaNacimiento: v.fechaNacimiento!,
    };
    this.camadaService.create(dto).subscribe({
      next: camada => {
        this.loadCamadas();
        this.form.patchValue({ camadaId: camada.id });
        this.camadaModalLoading.set(false);
        this.showCamadaModal.set(false);
      },
      error: err => {
        this.camadaModalError.set(extractErrorMessage(err, 'Error al crear la camada'));
        this.camadaModalLoading.set(false);
      },
    });
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
