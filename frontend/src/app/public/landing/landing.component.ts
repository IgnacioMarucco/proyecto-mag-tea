import {
  ChangeDetectionStrategy,
  Component,
  AfterViewInit,
  OnDestroy,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormularioInteresService } from '../../core/services/formulario-interes.service';
import { FormularioInteresCreate, ComoConocioProyecto } from '../../core/models/formulario-interes.model';

@Component({
  selector: 'app-landing',
  imports: [CommonModule, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './landing.component.html',
})
export class LandingComponent implements AfterViewInit, OnDestroy {
  activeSection = 'proyecto';
  private observer: IntersectionObserver | null = null;

  private readonly fb = inject(FormBuilder);
  private readonly formularioService = inject(FormularioInteresService);

  form = this.fb.group({
    apellidoTutor: ['', [Validators.required]],
    nombreTutor: ['', [Validators.required]],
    correoTutor: ['', [Validators.required, Validators.email]],
    telefono: [''],
    apellidoNino: ['', [Validators.required]],
    nombreNino: ['', [Validators.required]],
    fechaNacimientoNino: [''],
    comoConocioProyecto: [''],
    otroComoConocio: [''],
    diasDisponibles: [''],
  });

  submitState = signal<'idle' | 'loading' | 'success' | 'error'>('idle');
  errorMessage = signal<string | null>(null);

  showOtroField = toSignal(this.form.get('comoConocioProyecto')!.valueChanges, {
    initialValue: '',
  });

  ngAfterViewInit(): void {
    const sections = Array.from(document.querySelectorAll('section[id]')) as HTMLElement[];
    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            this.activeSection = entry.target.id;
          }
        });
      },
      { root: null, rootMargin: '-40% 0px -40% 0px', threshold: 0 }
    );
    sections.forEach((s) => this.observer!.observe(s));
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitState.set('loading');
    this.errorMessage.set(null);

    const v = this.form.value;
    const dto: FormularioInteresCreate = {
      apellidoTutor: v.apellidoTutor!,
      nombreTutor: v.nombreTutor!,
      correoTutor: v.correoTutor!,
      ...(v.telefono && { telefono: v.telefono }),
      apellidoNino: v.apellidoNino!,
      nombreNino: v.nombreNino!,
      ...(v.fechaNacimientoNino && { fechaNacimientoNino: v.fechaNacimientoNino }),
      ...(v.comoConocioProyecto && { comoConocioProyecto: v.comoConocioProyecto as ComoConocioProyecto }),
      ...(v.otroComoConocio && { otroComoConocio: v.otroComoConocio }),
      ...(v.diasDisponibles && { diasDisponibles: v.diasDisponibles }),
    };

    this.formularioService.create(dto).subscribe({
      next: () => this.submitState.set('success'),
      error: (err) => {
        this.errorMessage.set(err.error?.message ?? 'Error al enviar el formulario. Intentá de nuevo.');
        this.submitState.set('error');
      },
    });
  }

  readonly projectCards = [
    {
      n: '01',
      title: 'Qué estudiamos',
      desc: 'La presencia de anticuerpos anti-MAG en niños con diagnóstico o sospecha de TEA, y su relación con el desarrollo neurológico.',
    },
    {
      n: '02',
      title: 'Cómo lo hacemos',
      desc: 'Evaluaciones neurológicas (M-CHAT-R, CARS-2, Vineland) y una pequeña extracción de sangre. Todo se realiza con consentimiento informado y acompañamiento del equipo.',
    },
    {
      n: '03',
      title: 'Para qué sirve',
      desc: 'Generar conocimiento que aporte a la detección temprana y a futuras líneas de tratamiento del Trastorno del Espectro Autista.',
    },
  ];

  readonly steps = [
    {
      title: 'Completás el formulario',
      desc: 'Nos dejás tus datos y los del niño/a. Lleva dos minutos y podés hacerlo desde tu celular.',
    },
    {
      title: 'Te llamamos',
      desc: 'Coordinamos una llamada para explicarte el protocolo en detalle y responder todas tus dudas, sin compromiso.',
    },
    {
      title: 'Primera visita',
      desc: 'Si decidís participar, te recibimos en el centro para las evaluaciones neurológicas.',
    },
  ];

  readonly comoOpciones: { value: string; label: string }[] = [
    { value: 'INSTAGRAM', label: 'Instagram' },
    { value: 'SUGERIDO_PARTICIPANTE', label: 'Sugerencia de otro participante' },
    { value: 'SUGERIDO_EQUIPO_TERAPEUTICO', label: 'Equipo terapéutico' },
    { value: 'SUGERIDO_MEDICO', label: 'Un médico' },
    { value: 'OTRO', label: 'Otro' },
  ];

  readonly donationAmounts = [2000, 5000, 10000];
}
