import {
  ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed, effect, inject, input, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, catchError, map, of, switchMap } from 'rxjs';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { PoolService } from '../../../core/services/pool.service';
import { SueroService } from '../../../core/services/suero.service';
import { CajaService } from '../../../core/services/caja.service';
import { TuboService } from '../../../core/services/tubo.service';
import { PoolCreate, PoolResponse, PoolTuboInput, PoolUpdate } from '../../../core/models/pool.model';
import { SueroListItem, SueroTuboItem, SueroUso, VaciarTuboPayload } from '../../../core/models/suero.model';
import { extractErrorMessage } from '../../../shared/utils/error.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { FreezerPickerComponent } from '../../../shared/freezer-picker/freezer-picker.component';
import { TuboGridComponent } from '../../../shared/tubo-grid/tubo-grid.component';
import { TuboQuantityTableComponent } from '../../../shared/tubo-quantity-table/tubo-quantity-table.component';
import { VaciarTuboModalComponent } from '../../../shared/vaciar-tubo-modal/vaciar-tubo-modal.component';
import { MlPipe } from '../../../core/pipes/ml.pipe';
import { FirstFocusDirective } from '../../../shared/directives/first-focus.directive';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-pool-form',
  imports: [ReactiveFormsModule, PageHeaderComponent, FreezerPickerComponent, TuboGridComponent, MlPipe, TuboQuantityTableComponent, FirstFocusDirective, ConfirmModalComponent, VaciarTuboModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pool-form.component.html',
  host: { '(keydown)': 'onKeydown($event)' },
})
export class PoolFormComponent {
  private readonly elRef        = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly fb           = inject(FormBuilder);
  private readonly toast        = inject(ToastService);
  private readonly poolService  = inject(PoolService);
  private readonly sueroService = inject(SueroService);
  private readonly cajaService  = inject(CajaService);
  private readonly tuboService  = inject(TuboService);
  private readonly router       = inject(Router);
  private readonly route        = inject(ActivatedRoute);
  private readonly destroyRef   = inject(DestroyRef);

  readonly codigo  = input<string>();
  readonly isEdit  = computed(() => !!this.codigo());

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  loading         = signal(false);
  loadingSueros   = signal(false);
  error           = signal<string | null>(null);
  showExitConfirm = signal(false);
  step            = signal<1 | 2>(1);

  showVaciarModal = signal<'none' | 'tubo' | 'grilla'>('none');
  vaciarTuboId    = signal<number | null>(null);
  vaciarLoading   = signal(false);

  poolActual   = signal<PoolResponse | null>(null);
  editCajaId   = signal<number | null>(null);

  // ── Filtros locales (no forman parte del DTO) ────────────────────────────
  filterUso   = signal<SueroUso | null>(null);
  filterRango = signal<number | null>(null);

  // ── Lista de sueros y estado expandible ──────────────────────────────────
  suerosDisponibles = signal<SueroListItem[]>([]);
  expandedSueroIds  = signal<Set<number>>(new Set());
  tubosPorSuero     = signal<Map<number, SueroTuboItem[]>>(new Map());
  loadingSueroIds   = signal<Set<number>>(new Set());

  // ── Aportes seleccionados: Map<sueroTuboId, cantidadAportada> ────────────
  aportesSeleccionados = signal<Map<number, number>>(new Map());

  readonly totalAportes = computed(() =>
    Array.from(this.aportesSeleccionados().values()).reduce((s, v) => s + (v || 0), 0)
  );

  readonly aportesExcedidos = computed(() => {
    const aportes = this.aportesSeleccionados();
    const invalidos: string[] = [];
    this.tubosPorSuero().forEach(tubos => {
      for (const tubo of tubos) {
        const aportada = aportes.get(tubo.id) ?? 0;
        if (aportada > tubo.cantidadRestante) invalidos.push(tubo.posicion);
      }
    });
    return invalidos;
  });

  // ── Formulario ───────────────────────────────────────────────────────────
  private readonly todayIso = new Date().toISOString().slice(0, 10);

  form = this.fb.group({
    cajaId:        [null as number | null, Validators.required],
    fechaCreacion: [this.todayIso, Validators.required],
  });

  // ── Tubos del pool ───────────────────────────────────────────────────────
  tubosValue           = signal('');
  tubosPoolConCantidad = signal<PoolTuboInput[]>([]);
  ocupadas             = signal<string[]>([]);

  // Posiciones que quedarían libres al consumir 100% de un tubo de suero
  readonly freedPositions = computed(() => {
    const allTubos: SueroTuboItem[] = [];
    this.tubosPorSuero().forEach(tubos => allTubos.push(...tubos));
    const aportes = this.aportesSeleccionados();
    return allTubos
      .filter(t => {
        const aportada = aportes.get(t.id) ?? 0;
        return aportada > 0 && aportada >= t.cantidadRestante;
      })
      .map(t => t.posicion);
  });

  // Ocupadas ajustadas: excluye las que se liberarían con los aportes del paso 1
  readonly ocupadasAjustadas = computed(() => {
    const freed = new Set(this.freedPositions());
    return this.ocupadas().filter(pos => !freed.has(pos));
  });

  readonly ocupadasStr    = computed(() => this.ocupadasAjustadas().join(', '));
  readonly totalPoolTubos = computed(() =>
    this.tubosPoolConCantidad().reduce((s, t) => s + (t.cantidadInicial || 0), 0)
  );
  readonly diferencia    = computed(() =>
    Math.round((this.totalAportes() - this.totalPoolTubos()) * 1000) / 1000
  );
  readonly invarianteOk  = computed(() => this.diferencia() === 0);
  readonly absDiferencia = computed(() => Math.abs(this.diferencia()));

  constructor() {
    // ── Pre-cargar filtros desde query params (navegando desde suero-list) ──
    const uso   = this.route.snapshot.queryParamMap.get('uso');
    const rango = this.route.snapshot.queryParamMap.get('rango');
    if (uso)                        this.filterUso.set(uso as SueroUso);
    if (rango != null && rango !== '') this.filterRango.set(Number(rango));

    // ── Modo edición: cargar pool existente ──────────────────────────────
    effect(() => {
      const codigo = this.codigo();
      if (!codigo) return;
      this.poolService.findByCodigo(codigo).subscribe(pool => {
        this.poolActual.set(pool);
        this.editCajaId.set(pool.cajaId);
        this.form.patchValue({
          cajaId:        pool.cajaId,
          fechaCreacion: pool.fechaCreacion,
        });
        this.tubosValue.set(pool.tubos.map(t => t.posicion).join(', '));
        this.tubosPoolConCantidad.set(pool.tubos.map(t => ({
          posicion:        t.posicion,
          cantidadInicial: t.cantidadInicial,
        })));
        this.step.set(2);
        this.onCajaChange(pool.cajaId);
      });
    });

    // ── Modo creación: filtros de sueros ─────────────────────────────────
    combineLatest([
      toObservable(this.filterUso),
      toObservable(this.filterRango),
    ]).pipe(
      switchMap(([uso, rango]) => {
        if (!uso || rango == null) return of([] as SueroListItem[]);
        this.loadingSueros.set(true);
        return this.sueroService.findAll({
          rango: [String(rango)],
          uso:   [uso],
          size:  100,
        }).pipe(map(r => r.content), catchError(() => of([] as SueroListItem[])));
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(sueros => {
      this.loadingSueros.set(false);
      this.suerosDisponibles.set(sueros);
      this.resetAporteState();
    });
  }

  private resetAporteState(): void {
    this.expandedSueroIds.set(new Set());
    this.tubosPorSuero.set(new Map());
    this.loadingSueroIds.set(new Set());
    this.aportesSeleccionados.set(new Map());
  }

  // ── Expandible por suero ──────────────────────────────────────────────────
  toggleExpand(sueroId: number): void {
    const expanded = new Set(this.expandedSueroIds());
    if (expanded.has(sueroId)) {
      expanded.delete(sueroId);
      this.expandedSueroIds.set(expanded);
      return;
    }
    expanded.add(sueroId);
    this.expandedSueroIds.set(expanded);

    if (this.tubosPorSuero().has(sueroId)) return;

    const loading = new Set(this.loadingSueroIds());
    loading.add(sueroId);
    this.loadingSueroIds.set(loading);

    this.sueroService.findById(sueroId).subscribe({
      next: resp => {
        this.tubosPorSuero.update(prev => {
          const next = new Map(prev);
          next.set(sueroId, resp.tubos.filter(t => t.cantidadRestante > 0));
          return next;
        });
        const l = new Set(this.loadingSueroIds());
        l.delete(sueroId);
        this.loadingSueroIds.set(l);
      },
      error: () => {
        const l = new Set(this.loadingSueroIds());
        l.delete(sueroId);
        this.loadingSueroIds.set(l);
      },
    });
  }

  isExpanded(sueroId: number): boolean       { return this.expandedSueroIds().has(sueroId); }
  getTubosDeSuero(sueroId: number): SueroTuboItem[] { return this.tubosPorSuero().get(sueroId) ?? []; }
  isSueroLoading(sueroId: number): boolean   { return this.loadingSueroIds().has(sueroId); }
  isAporteSelected(tuboId: number): boolean  { return this.aportesSeleccionados().has(tuboId); }
  getAporteCantidad(tuboId: number): number  { return this.aportesSeleccionados().get(tuboId) ?? 0; }

  toggleTubo(tuboId: number): void {
    this.aportesSeleccionados.update(prev => {
      const next = new Map(prev);
      if (next.has(tuboId)) next.delete(tuboId);
      else next.set(tuboId, 0);
      return next;
    });
  }

  updateAporte(tuboId: number, rawValue: string): void {
    this.aportesSeleccionados.update(prev => {
      if (!prev.has(tuboId)) return prev;
      const next = new Map(prev);
      next.set(tuboId, parseFloat(rawValue) || 0);
      return next;
    });
  }

  restanteDespTubo(tubo: SueroTuboItem, tuboId: number): number | null {
    if (!this.isAporteSelected(tuboId)) return null;
    const aportada = this.getAporteCantidad(tuboId);
    if (!aportada) return null;
    return tubo.cantidadRestante - aportada;
  }

  // ── Ubicación ─────────────────────────────────────────────────────────────
  onCajaChange(cajaId: number | null): void {
    this.form.patchValue({ cajaId });
    if (!cajaId) { this.ocupadas.set([]); return; }
    this.cajaService.getOcupacion(cajaId)
      .pipe(catchError(() => of({ ocupadas: [] as string[] })))
      .subscribe(r => {
        const ownPositions = this.poolActual()?.tubos.map(t => t.posicion) ?? [];
        this.ocupadas.set(r.ocupadas.filter(pos => !ownPositions.includes(pos)));
      });
  }

  // ── Tubos del pool ────────────────────────────────────────────────────────
  onTubosChange(val: string): void {
    this.tubosValue.set(val);
    const posiciones = val ? val.split(',').map(s => s.trim()).filter(Boolean) : [];
    this.tubosPoolConCantidad.update(prev => {
      const prevMap = new Map(prev.map(t => [t.posicion, t.cantidadInicial]));
      return posiciones.map(pos => ({
        posicion: pos,
        cantidadInicial: prevMap.get(pos) ?? 0,
      }));
    });
  }

  onTubosPoolConCantidadChange(tubos: PoolTuboInput[]): void {
    this.tubosPoolConCantidad.set(tubos);
  }

  // ── Navegación de pasos ──────────────────────────────────────────────────
  avanzarAStep2(): void {
    const aportes = Array.from(this.aportesSeleccionados().entries())
      .filter(([, cantidad]) => cantidad > 0);
    if (aportes.length === 0) {
      this.error.set('Seleccioná al menos un tubo de suero con cantidad para aportar');
      return;
    }
    const excedidos = this.aportesExcedidos();
    if (excedidos.length > 0) {
      this.error.set(
        `La cantidad ingresada supera el volumen disponible en: ${excedidos.join(', ')}`
      );
      return;
    }
    this.error.set(null);
    this.step.set(2);
  }

  volverAStep1(): void {
    this.step.set(1);
    this.error.set(null);
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

  // ── Envío ─────────────────────────────────────────────────────────────────
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const firstInvalid = this.elRef.nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]');
      firstInvalid?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      firstInvalid?.focus();
      return;
    }

    if (this.tubosPoolConCantidad().length === 0) {
      this.error.set('Seleccioná al menos un tubo en la grilla del pool');
      return;
    }
    if (this.tubosPoolConCantidad().some(t => !t.cantidadInicial || t.cantidadInicial <= 0)) {
      this.error.set('Todos los tubos del pool deben tener una cantidad mayor a 0');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const codigo = this.codigo();
    if (codigo) {
      const pool = this.poolActual()!;
      const dto: PoolUpdate = {
        cajaId:        Number(this.form.value.cajaId),
        fechaCreacion: this.form.value.fechaCreacion!,
        tubos:         this.tubosPoolConCantidad(),
      };
      this.poolService.update(pool.id, dto).subscribe({
        next:  result => { this.toast.show('Pool actualizado'); this.router.navigate(['/internal/pools', result.codigo]); },
        error: err => { this.error.set(extractErrorMessage(err, 'Error al guardar')); this.loading.set(false); },
      });
      return;
    }

    const aportes = Array.from(this.aportesSeleccionados().entries())
      .filter(([, cantidad]) => cantidad > 0)
      .map(([sueroTuboId, cantidadAportada]) => ({ sueroTuboId, cantidadAportada }));

    if (aportes.length === 0) {
      this.error.set('Seleccioná al menos un tubo de suero con cantidad para aportar');
      this.loading.set(false);
      return;
    }
    if (!this.invarianteOk()) {
      this.error.set(
        `La suma de aportes (${this.totalAportes().toFixed(2)} ml) debe coincidir ` +
        `con el total de tubos del pool (${this.totalPoolTubos().toFixed(2)} ml)`
      );
      this.loading.set(false);
      return;
    }

    const dto: PoolCreate = {
      cajaId:  Number(this.form.value.cajaId),
      aportes,
      tubos:   this.tubosPoolConCantidad(),
    };

    this.poolService.create(dto).subscribe({
      next:  () => { this.toast.show('Pool creado'); this.router.navigate(['/internal/pools']); },
      error: err => { this.error.set(extractErrorMessage(err, 'Error al guardar')); this.loading.set(false); },
    });
  }

  cancel(): void { this.router.navigate(['/internal/pools']); }

  // ── Vaciar tubos (modo edición) ───────────────────────────────────────────
  openVaciarTubo(tuboId: number): void {
    this.vaciarTuboId.set(tuboId);
    this.showVaciarModal.set('tubo');
  }

  openLiberarGrilla(): void {
    this.showVaciarModal.set('grilla');
  }

  onVaciarCancel(): void {
    this.showVaciarModal.set('none');
    this.vaciarTuboId.set(null);
  }

  onVaciarConfirm(payload: VaciarTuboPayload): void {
    const modo   = this.showVaciarModal();
    const tuboId = this.vaciarTuboId();
    const pool   = this.poolActual();
    this.showVaciarModal.set('none');
    this.vaciarLoading.set(true);
    if (!pool) return;

    const req$ = tuboId !== null
      ? this.tuboService.vaciar(tuboId, payload)
      : this.poolService.liberarGrilla(pool.id, payload).pipe(map(() => undefined as void));

    req$.subscribe({
      next: () => {
        this.vaciarLoading.set(false);
        this.vaciarTuboId.set(null);
        this.toast.show(modo === 'grilla' ? 'Grilla liberada' : 'Tubo vaciado');
        this.reloadPool();
      },
      error: () => this.vaciarLoading.set(false),
    });
  }

  private reloadPool(): void {
    const codigo = this.codigo();
    if (!codigo) return;
    this.poolService.findByCodigo(codigo).pipe(
      catchError(() => of(null)),
    ).subscribe(pool => {
      if (pool) this.poolActual.set(pool);
    });
  }
}
