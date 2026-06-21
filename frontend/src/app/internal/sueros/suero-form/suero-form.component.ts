import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, signal } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { SueroService } from '../../../core/services/suero.service';
import { SueroCreate, SueroResponse, SueroTuboInput, SueroUpdate } from '../../../core/models/suero.model';
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

@Component({
  selector: 'app-suero-form',
  imports: [ReactiveFormsModule, PageHeaderComponent, TuboGridComponent, TuboQuantityTableComponent, FreezerPickerComponent, IconComponent, FirstFocusDirective, ConfirmModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './suero-form.component.html',
  host: { '(keydown)': 'onKeydown($event)' },
})
export class SueroFormComponent {
  private readonly elRef           = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly fb              = inject(FormBuilder);
  private readonly service         = inject(SueroService);
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
  tubosValue       = signal('');
  tubosConCantidad = signal<SueroTuboInput[]>([]);
  ocupadas         = signal<string[]>([]);

  readonly ocupadasStr    = computed(() => this.ocupadas().join(', '));
  readonly totalCalculado = computed(() =>
    this.tubosConCantidad().reduce((s, t) => s + (t.cantidadInicial || 0), 0)
  );

  // ── Preview rango ─────────────────────────────────────────────────────────
  private readonly btuSignal = signal<number | null>(null);
  readonly rangoPreview      = computed(() => this.calcRangoLabel(this.btuSignal()));
  readonly rangoPreviewColor = computed(() => this.calcRangoColor(this.btuSignal()));

  get isEdit(): boolean { return !!this.codigo(); }

  constructor() {
    this.form.get('valorAnticuerpos')!.valueChanges.subscribe(v => this.btuSignal.set(v));

    effect(() => {
      const codigo = this.codigo();
      if (!codigo) return;

      this.form.get('pacienteId')!.clearValidators();
      this.form.get('pacienteId')!.updateValueAndValidity();

      this.service.findByCodigo(codigo).subscribe({
        next: suero => {
          this.sueroLoaded.set(suero);
          this.tubosValue.set(suero.tubos.map(t => t.posicion).join(', '));
          this.tubosConCantidad.set(
            suero.tubos.map(t => ({ posicion: t.posicion, cantidadInicial: t.cantidadInicial }))
          );
          this.form.patchValue({
            cajaId:           suero.cajaId,
            valorAnticuerpos: suero.valorAnticuerpos,
          });
          this.onCajaChange(suero.cajaId);
        },
        error: () => this.error.set('No se pudo cargar el suero'),
      });
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
      const prevMap = new Map(prev.map(t => [t.posicion, t.cantidadInicial]));
      return posiciones.map(pos => ({
        posicion: pos,
        cantidadInicial: prevMap.get(pos) ?? 0,
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
    if (tubos.some(t => !t.cantidadInicial || t.cantidadInicial <= 0)) {
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
          fechaExtraccion:  this.pacienteResuelto()!.fechaExtraccion!,
          valorAnticuerpos: btu,
        } satisfies SueroCreate);

    req$.subscribe({
      next:  () => { this.toast.show(this.isEdit ? 'Suero actualizado' : 'Suero registrado'); this.router.navigate(['/internal/sueros']); },
      error: err => { this.error.set(extractErrorMessage(err, 'Error al guardar')); this.loading.set(false); },
    });
  }

  cancel(): void { this.router.navigate(['/internal/sueros']); }

  formatDate(date: string | null): string {
    if (!date) return '—';
    return new Date(date + 'T00:00:00').toLocaleDateString('es-AR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
    });
  }

  private calcRangoLabel(btu: number | null): string | null {
    if (btu == null || btu < 0) return null;
    if (btu <= 1313) return 'Rango 0';
    if (btu <= 2500) return 'Rango 1';
    if (btu <= 8000) return 'Rango 2';
    return 'Rango 3';
  }

  private calcRangoColor(btu: number | null): string {
    if (btu == null || btu < 0) return '';
    if (btu <= 1313) return 'badge-rango0';
    if (btu <= 2500) return 'badge-rango1';
    if (btu <= 8000) return 'badge-rango2';
    return 'badge-rango3';
  }
}
