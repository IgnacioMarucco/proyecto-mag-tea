import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { startWith } from 'rxjs';

export const PREGUNTAS_MCHAT = [
  'Si Ud. señala algo que está del otro lado de la habitación, ¿su hijo o hija mira hacia allí? (Por ejemplo: si Ud. señala un juguete o un animal, ¿su hijo o hija mira al juguete o al animal?)',
  '¿Alguna vez se preguntó si su hijo o hija era sordo o sorda?',
  '¿Su hijo o hija juega a simular, hacer "como si", o juegos de imaginación? (Por ejemplo: simula que toma de una taza vacía, finge hablar por teléfono, o hace como que le da de comer a una muñeca o a un peluche)',
  '¿A su hijo o hija le gusta treparse a las cosas? (Por ejemplo: muebles, juegos de la plaza, o escaleras)',
  '¿Su hijo o hija hace movimientos raros con los dedos cerca de sus ojos? (Por ejemplo: ¿mueve o agita los dedos cerca de sus ojos de manera rara?)',
  '¿Su hijo o hija señala con el dedo cuando quiere pedir algo o buscar ayuda? (Por ejemplo: señala algún alimento o juguete que está fuera de su alcance)',
  '¿Su hijo o hija señala con el dedo cuando quiere mostrarle algo interesante? (Por ejemplo: señala un avión en el cielo o un camión muy grande en la calle)',
  '¿Su hijo o hija se interesa por otros niños? (Por ejemplo: ¿mira a otros niños, les sonríe, se acerca a ellos?)',
  '¿Su hijo o hija le muestra cosas, trayéndoselas o alzándolas para que Ud. las vea – no para buscar ayuda sino simplemente para compartirlas con Ud.? (Por ejemplo: le muestra una flor, un peluche, o un camión de juguete)',
  '¿Su hijo o hija responde cuando lo/la llama por su nombre? (Por ejemplo: ¿su hijo o hija lo mira o la mira, habla o balbucea, o interrumpe lo que está haciendo cuando lo/la llama por su nombre?)',
  'Cuando le sonríe a su hijo o hija, ¿le devuelve la sonrisa?',
  '¿A su hijo o hija le molestan los ruidos comunes de todos los días? (Por ejemplo: ¿su hijo o hija grita o llora cuando escucha una aspiradora, una licuadora, una moto, la radio, música fuerte u otro ruido común?)',
  '¿Su hijo o hija camina?',
  '¿Su hijo o hija lo/la mira a los ojos cuando le está hablando, jugando con él/ella, o cuando lo/la está vistiendo?',
  '¿Su hijo o hija trata de copiar lo que Ud. hace? (Por ejemplo: decir adiós con la mano, aplaudir, o hacer un ruido gracioso cuando Ud. lo hace)',
  'Si Ud. se da vuelta para mirar algo, ¿su hijo o hija gira la cabeza para ver lo que Ud. está mirando?',
  '¿Su hijo o hija intenta hacer que Ud. lo/la mire? (Por ejemplo: ¿su hijo o hija lo/la mira para que lo/la felicite, o dice "mirá" o "mirame"?)',
  '¿Su hijo o hija entiende cuando Ud. le dice que haga algo? (Por ejemplo: si Ud. no se lo señala, ¿su hijo o hija entiende cuando le pide "poné el libro sobre la silla" o "traeme la frazadita"?)',
  'Si pasa algo nuevo, ¿su hijo o hija lo/la mira a la cara para ver qué hace Ud.? (Por ejemplo: si su hijo o hija escucha un ruido raro o gracioso, o ve un juguete nuevo, ¿lo/la mira a la cara?)',
  '¿A su hijo o hija le gustan las actividades de movimiento? (Por ejemplo: hamacarse o jugar al "caballito" sobre sus rodillas)',
];

// Preguntas donde Sí/Pasa es la respuesta patológica (falla)
const INVERTIDAS = new Set([2, 5, 12]);

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

  readonly preguntas = PREGUNTAS_MCHAT;

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
