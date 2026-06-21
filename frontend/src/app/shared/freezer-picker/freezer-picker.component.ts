import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, map, of } from 'rxjs';
import { CajaService } from '../../core/services/caja.service';
import { CajaListItem, CajaCreate } from '../../core/models/caja.model';
import { extractErrorMessage } from '../utils/error.utils';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-freezer-picker',
  imports: [ReactiveFormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './freezer-picker.component.html',
})
export class FreezerPickerComponent {
  private readonly cajaService = inject(CajaService);
  private readonly fb          = inject(FormBuilder);

  defaultCajaId = input<number | null>(null);
  cajaIdChange  = output<number | null>();

  cajasData  = signal<CajaListItem[]>([]);
  freezerSel = signal<string | null>(null);
  cajonSel   = signal<number | null>(null);
  cajaIdSel  = signal<number | null>(null);

  readonly freezers = computed(() =>
    [...new Set(this.cajasData().map(c => c.freezer))].sort()
  );
  readonly cajones = computed(() =>
    [...new Set(
      this.cajasData().filter(c => c.freezer === this.freezerSel()).map(c => c.cajon)
    )].sort((a, b) => a - b)
  );
  readonly cajasFiltradas = computed(() =>
    this.cajasData()
      .filter(c => c.freezer === this.freezerSel() && c.cajon === this.cajonSel())
      .sort((a, b) => a.numero - b.numero)
  );
  private inicializado = false;

  showModal    = signal(false);
  modalLoading = signal(false);
  modalError   = signal<string | null>(null);
  cajaForm = this.fb.group({
    freezer: ['',                    Validators.required],
    cajon:   [null as number | null, [Validators.required, Validators.min(1)]],
    numero:  [null as number | null, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.loadCajas();

    effect(() => {
      const defaultId = this.defaultCajaId();
      const cajas     = this.cajasData();
      if (this.inicializado || !defaultId || !cajas.length) return;
      const caja = cajas.find(c => c.id === defaultId);
      if (caja) {
        this.freezerSel.set(caja.freezer);
        this.cajonSel.set(caja.cajon);
        this.cajaIdSel.set(caja.id);
        this.inicializado = true;
      }
    });
  }

  private loadCajas(): void {
    this.cajaService.findAll({ size: 200, sortBy: 'freezer', sortDir: 'asc' })
      .pipe(map(r => r.content), catchError(() => of([] as CajaListItem[])))
      .subscribe(cajas => this.cajasData.set(cajas));
  }

  selectFreezer(value: string): void {
    this.freezerSel.set(value || null);
    this.cajonSel.set(null);
    this.cajaIdSel.set(null);
    this.cajaIdChange.emit(null);
  }

  selectCajon(value: string): void {
    this.cajonSel.set(value ? Number(value) : null);
    this.cajaIdSel.set(null);
    this.cajaIdChange.emit(null);
  }

  selectCaja(value: string): void {
    const id = value ? Number(value) : null;
    this.cajaIdSel.set(id);
    this.cajaIdChange.emit(id);
  }

  openModal(): void {
    this.cajaForm.reset();
    this.modalError.set(null);
    this.showModal.set(true);
  }

  closeModal(): void { this.showModal.set(false); }

  guardarCaja(): void {
    if (this.cajaForm.invalid) { this.cajaForm.markAllAsTouched(); return; }
    this.modalLoading.set(true);
    this.modalError.set(null);
    const v = this.cajaForm.value;
    const dto: CajaCreate = {
      freezer: v.freezer!,
      cajon:   Number(v.cajon),
      numero:  Number(v.numero),
    };
    this.cajaService.create(dto).subscribe({
      next: caja => {
        this.loadCajas();
        this.freezerSel.set(dto.freezer);
        this.cajonSel.set(dto.cajon);
        this.cajaIdSel.set(caja.id);
        this.cajaIdChange.emit(caja.id);
        this.modalLoading.set(false);
        this.showModal.set(false);
      },
      error: err => {
        this.modalError.set(extractErrorMessage(err, 'Error al crear la caja'));
        this.modalLoading.set(false);
      },
    });
  }
}
