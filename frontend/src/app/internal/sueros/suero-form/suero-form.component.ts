import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed, inject, input, signal } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, filter, map, of, startWith, tap, Observable } from 'rxjs';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { SueroService } from '../../../core/services/suero.service';
import { SueroCreate, SueroResponse, SueroTuboInput, SueroUpdate, VaciarTuboPayload } from '../../../core/models/suero.model';
import { TuboService } from '../../../core/services/tubo.service';
import { PacienteService } from '../../../core/services/paciente.service';
import { PacientePorCodigo } from '../../../core/models/paciente.model';
import { CajaService } from '../../../core/services/caja.service';
import { extractErrorMessage } from '../../../shared/utils/error.utils';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { TuboGridComponent } from '../../../shared/tubo-grid/tubo-grid.component';
import { TuboQuantityTableComponent } from '../../../shared/tubo-quantity-table/tubo-quantity-table.component';
import { FreezerPickerComponent } from '../../../shared/freezer-picker/freezer-picker.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { FirstFocusDirective } from '../../../shared/directives/first-focus.directive';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { VaciarTuboModalComponent } from '../../../shared/vaciar-tubo-modal/vaciar-tubo-modal.component';
import { MlPipe } from '../../../core/pipes/ml.pipe';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { getBtuLabel, getBtuColor } from '../../../shared/utils/btu.utils';

@Component({
  selector: 'app-suero-form',
  imports: [ReactiveFormsModule, PageHeaderComponent, TuboGridComponent, TuboQuantityTableComponent, FreezerPickerComponent, IconComponent, FirstFocusDirective, ConfirmModalComponent, VaciarTuboModalComponent, MlPipe, FechaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './suero-form.component.html',
  host: { '(keydown)': 'onKeydown($event)' },
})
export class SueroFormComponent {
  private readonly elRef           = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly destroyRef      = inject(DestroyRef);
  private readonly fb              = inject(FormBuilder);
  private readonly service         = inject(SueroService);
  private readonly tuboService     = inject(TuboService);
  private readonly toast           = inject(ToastService);
  private readonly pacienteService = inject(PacienteService);
  private readonly cajaService     = inject(CajaService);
  private readonly router          = inject(Router);
  private readonly route           = inject(ActivatedRoute);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  codigo          = input<string>();
  loading         = signal(false);
  error           = signal<string | null>(null);
  showExitConfirm = signal(false);

  // ── Vaciado de tubos ──────────────────────────────────────────────────────
  showVaciarModal  = signal<'none' | 'tubo' | 'grilla'>('none');
  vaciarTuboId     = signal<number | null>(null);
  vaciarLoading    = signal(false);

  // ── Formulario principal ──────────────────────────────────────────────────
  form = this.fb.group({
    pacienteId:       [null as number | null, Validators.required],
    cajaId:           [null as number | null, Validators.required],
    valorAnticuerpos: [null as number | null],
  });

  // ── Lookup de paciente por código ─────────────────────────────────────────
  codigoInput      = signal('');
  lookupLoading    = signal(false);
  lookupError      = signal<string | null>(null);
  pacienteResuelto = signal<PacientePorCodigo | null>(null);

  // ── Suero cargado (modo edición) ──────────────────────────────────────────
  sueroLoaded = signal<SueroResponse | null>(null);

  // ── Grilla y tubos ───────────────────────────────────────────────────────
  cajaSeleccionada = signal(false);
  tubosValue       = signal('');
  tubosConCantidad = signal<SueroTuboInput[]>([]);
  ocupadas         = signal<string[]>([]);

  readonly ocupadasStr    = computed(() => this.ocupadas().join(', '));
  readonly totalCalculado = computed(() =>
    this.tubosConCantidad().reduce((s, t) => s + (t.cantidad || 0), 0)
  );

  // ── Preview rango ─────────────────────────────────────────────────────────
  private readonly btuSignal = toSignal(
    this.form.get('valorAnticuerpos')!.valueChanges.pipe(
      startWith(this.form.get('valorAnticuerpos')!.value)
    ),
    { initialValue: null as number | null }
  );
  readonly rangoPreview      = computed(() => getBtuLabel(this.btuSignal() ?? null));
  readonly rangoPreviewColor = computed(() => getBtuColor(this.btuSignal() ?? null));

  readonly isEdit = computed(() => !!this.codigo());

  constructor() {
    toObservable(this.codigo).pipe(
      filter((codigo): codigo is string => !!codigo),
      tap(() => {
        this.form.get('pacienteId')!.clearValidators();
        this.form.get('pacienteId')!.updateValueAndValidity();
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(codigo => this.loadSuero(codigo));
  }

  private loadSuero(codigo: string): void {
    this.service.findByCodigo(codigo).subscribe({
      next: suero => {
        this.sueroLoaded.set(suero);
        this.tubosValue.set(suero.tubos.map(t => t.posicion).join(', '));
        this.tubosConCantidad.set(
          suero.tubos.map(t => ({ posicion: t.posicion, cantidad: t.cantidadRestante }))
        );
        this.form.patchValue({
          cajaId:           suero.cajaId,
          valorAnticuerpos: suero.valorAnticuerpos,
        });
        this.onCajaChange(suero.cajaId);
      },
      error: () => this.error.set('No se pudo cargar el suero'),
    });
  }

  // ── Lookup paciente ───────────────────────────────────────────────────────
  onCodigoBlur(): void { this.resolverCodigo(); }

  onCodigoKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') { event.preventDefault(); this.resolverCodigo(); }
  }

  resolverCodigo(): void {
    const codigo = this.codigoInput().trim();
    if (!codigo) return;
    this.lookupLoading.set(true);
    this.lookupError.set(null);
    this.pacienteResuelto.set(null);
    this.form.patchValue({ pacienteId: null });

    this.pacienteService.findByCodigo(codigo).subscribe({
      next: p => {
        this.pacienteResuelto.set(p);
        this.form.patchValue({ pacienteId: p.id });
        this.lookupLoading.set(false);
      },
      error: err => {
        this.lookupError.set(extractErrorMessage(err, 'Código no encontrado'));
        this.lookupLoading.set(false);
      },
    });
  }

  clearPaciente(): void {
    this.codigoInput.set('');
    this.pacienteResuelto.set(null);
    this.lookupError.set(null);
    this.form.patchValue({ pacienteId: null });
  }

  // ── Ubicación ─────────────────────────────────────────────────────────────
  onCajaChange(cajaId: number | null): void {
    this.form.patchValue({ cajaId });
    this.cajaSeleccionada.set(!!cajaId);
    if (!cajaId) { this.ocupadas.set([]); return; }
    const excludeId = this.sueroLoaded()?.id;
    this.cajaService.getOcupacion(cajaId, excludeId)
      .pipe(catchError(() => of({ ocupadas: [] as string[] })))
      .subscribe(r => this.ocupadas.set(r.ocupadas));
  }

  // ── Tubos ─────────────────────────────────────────────────────────────────
  onTubosChange(val: string): void {
    this.tubosValue.set(val);
    const posiciones = val ? val.split(',').map(s => s.trim()).filter(Boolean) : [];
    this.tubosConCantidad.update(prev => {
      const prevMap = new Map(prev.map(t => [t.posicion, t.cantidad]));
      return posiciones.map(pos => ({
        posicion: pos,
        cantidad: prevMap.get(pos) ?? 0,
      }));
    });
  }

  onTubosConCantidadChange(tubos: SueroTuboInput[]): void {
    this.tubosConCantidad.set(tubos);
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
    const tubos = this.tubosConCantidad();
    if (this.form.invalid || !tubos.length) {
      this.form.markAllAsTouched();
      if (!tubos.length) this.error.set('Seleccioná al menos un tubo en la grilla');
      else {
        const firstInvalid = this.elRef.nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]');
        firstInvalid?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        firstInvalid?.focus();
      }
      return;
    }
    if (tubos.some(t => !t.cantidad || t.cantidad <= 0)) {
      this.error.set('Todos los tubos deben tener una cantidad mayor a 0');
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const v     = this.form.value;
    const btu   = v.valorAnticuerpos != null ? Number(v.valorAnticuerpos) : null;
    const suero = this.sueroLoaded();

    const req$ = suero
      ? this.service.update(suero.id, {
          cajaId:           Number(v.cajaId),
          tubos,
          fechaExtraccion:  suero.fechaExtraccion,
          valorAnticuerpos: btu,
        } satisfies SueroUpdate)
      : this.service.create({
          pacienteId:       Number(v.pacienteId),
          cajaId:           Number(v.cajaId),
          tubos,
          fechaExtraccion:  this.pacienteResuelto()!.fechaTurnoExtraccion!,
          valorAnticuerpos: btu,
        } satisfies SueroCreate);

    req$.subscribe({
      next:  () => { this.toast.show(this.isEdit() ? 'Suero actualizado' : 'Suero registrado'); this.router.navigate(['/internal/sueros']); },
      error: err => { this.error.set(extractErrorMessage(err, 'Error al guardar')); this.loading.set(false); },
    });
  }

  cancel(): void { this.router.navigate(['/internal/sueros']); }

  // ── Vaciado de tubos ──────────────────────────────────────────────────────
  openVaciarTubo(tuboId: number): void {
    this.vaciarTuboId.set(tuboId);
    this.showVaciarModal.set('tubo');
  }

  openLiberarGrilla(): void {
    this.showVaciarModal.set('grilla');
  }

  onVaciarConfirm(payload: VaciarTuboPayload): void {
    const modo   = this.showVaciarModal();
    const tuboId = this.vaciarTuboId();
    const suero  = this.sueroLoaded();
    this.showVaciarModal.set('none');
    this.vaciarLoading.set(true);
    if (!suero) return;

    const req$: Observable<void> = tuboId !== null
      ? this.tuboService.vaciar(tuboId, payload)
      : this.service.liberarGrilla(suero.id, payload).pipe(map(() => undefined as void));

    req$.subscribe({
      next: () => {
        this.vaciarLoading.set(false);
        this.vaciarTuboId.set(null);
        this.toast.show(modo === 'grilla' ? 'Grilla liberada' : 'Tubo vaciado');
        this.loadSuero(this.codigo()!);
      },
      error: err => {
        this.vaciarLoading.set(false);
        this.error.set(extractErrorMessage(err, 'Error al vaciar'));
      },
    });
  }

  onVaciarCancel(): void {
    this.showVaciarModal.set('none');
    this.vaciarTuboId.set(null);
  }

}
