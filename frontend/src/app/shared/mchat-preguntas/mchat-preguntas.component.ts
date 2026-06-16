import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { startWith } from 'rxjs';
import { MCHAT_PREGUNTAS } from '../constants/mchat.constants';

// Preguntas donde Sí/Pasa es la respuesta patológica (falla)
const INVERTIDAS = new Set(MCHAT_PREGUNTAS.filter(p => p.invertida).map(p => p.numero));

@Component({
  selector: 'app-mchat-preguntas',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './mchat-preguntas.component.html',
})
export class MchatPreguntasComponent {
  private readonly fb = inject(FormBuilder);

  // 'inicial' → Sí/No (familia) | 'seguimiento' → Pasa/Falla (profesional)
  // En ambos modos: true = Sí/Pasa, false = No/Falla
  modo = input.required<'inicial' | 'seguimiento'>();

  // Array de 20 booleans para pre-rellenar (true=Sí/Pasa). null = sin datos, formulario vacío.
  respuestas  = input<boolean[] | null>(null);

  // Solo lectura: muestra las respuestas sin permitir edición
  readonly    = input<boolean>(false);

  // Si true, requiere respuesta en los 20 ítems antes de poder enviar (formulario público)
  required    = input<boolean>(false);

  saving      = input<boolean>(false);
  error       = input<string | null>(null);
  submitLabel = input<string>('Guardar');
  savingLabel = input<string>('Guardando…');
  cancelLabel = input<string | null>(null);

  submitted = output<boolean[]>();
  cancelled = output<void>();

  readonly preguntas = MCHAT_PREGUNTAS;

  form = this.fb.group(
    Object.fromEntries(Array.from({ length: 20 }, (_, i) => [`q${i + 1}`, [null as boolean | null]]))
  );

  private readonly formValue = toSignal(
    this.form.valueChanges.pipe(startWith(this.form.value)),
    { initialValue: this.form.value as Record<string, boolean | null> }
  );

  readonly labelPositivo = computed(() => this.modo() === 'seguimiento' ? 'Pasa' : 'Sí');
  readonly labelNegativo = computed(() => this.modo() === 'seguimiento' ? 'Falla' : 'No');

  protected itemValue(n: number): boolean | null {
    const v = (this.formValue() as Record<string, boolean | null>)[`q${n}`];
    return v === undefined ? null : v;
  }

  // true = Sí/Pasa, false = No/Falla. Invertidas (2,5,12): Sí/Pasa es la falla.
  isFalla(n: number, value: boolean | null): boolean {
    if (value === null) return false;
    return INVERTIDAS.has(n) ? value : !value;
  }

  getLabel(n: number, value: boolean | null): string {
    if (value === null) return '—';
    if (this.modo() === 'seguimiento') return value ? 'Pasa' : 'Falla';
    return value ? 'Sí' : 'No';
  }

  readonly fallasCount = computed(() =>
    Array.from({ length: 20 }, (_, i) => {
      const n = i + 1;
      const val = (this.formValue() as Record<string, boolean | null>)[`q${n}`];
      return this.isFalla(n, val ?? null);
    }).filter(Boolean).length
  );

  constructor() {
    effect(() => {
      const r = this.respuestas();
      if (r !== null) {
        this.form.patchValue(Object.fromEntries(r.map((val, i) => [`q${i + 1}`, val])));
      }
      // Si null: el formulario queda vacío (deseleccionado) independientemente del modo
    });
  }

  onSubmit(): void {
    const values = Array.from({ length: 20 }, (_, i) => this.form.value[`q${i + 1}`] as boolean | null);
    if (this.required()) {
      if (values.some(v => v === null)) {
        this.form.markAllAsTouched();
        return;
      }
    }
    this.submitted.emit(values.map(v => !!v));
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
