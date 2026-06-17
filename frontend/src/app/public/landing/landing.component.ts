import {
  ChangeDetectionStrategy,
  Component,
  AfterViewInit,
  OnDestroy,
  inject,
  signal,
} from '@angular/core';
import { extractErrorMessage } from '../../shared/utils/error.utils';
import { CommonModule, NgOptimizedImage } from '@angular/common';
import { Meta, Title } from '@angular/platform-browser';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormularioInteresService } from '../../core/services/formulario-interes.service';
import { FormularioInteresCreate, ComoConocioProyecto } from '../../core/models/formulario-interes.model';
import { DonacionService } from '../../core/services/donacion.service';

@Component({
  selector: 'app-landing',
  imports: [CommonModule, NgOptimizedImage, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './landing.component.html',
})
export class LandingComponent implements AfterViewInit, OnDestroy {
  activeSection = 'proyecto';
  readonly portalInternoUrl = (() => {
    const { protocol, hostname, port } = window.location;
    return `${protocol}//app.${hostname}${port ? ':' + port : ''}/login`;
  })();
  private observer: IntersectionObserver | null = null;

  private readonly fb = inject(FormBuilder);
  private readonly formularioService = inject(FormularioInteresService);
  private readonly donacionService = inject(DonacionService);

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

  constructor() {
    inject(Title).setTitle('MAG-TEA — Investigación sobre anticuerpos y TEA');
    inject(Meta).addTags([
      { name: 'description', content: 'Proyecto de investigación sobre anticuerpos anti-MAG y TEA en niños de 2 a 8 años. Centro Wernicke + CIQUIBIC, Córdoba.' },
      { property: 'og:title', content: 'MAG-TEA — Investigación sobre anticuerpos y TEA' },
      { property: 'og:description', content: 'Estudiamos la relación entre anticuerpos anti-MAG y el Trastorno del Espectro Autista. Sumate como familia participante desde Córdoba.' },
      { property: 'og:type', content: 'website' },
    ]);
  }

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
        this.errorMessage.set(extractErrorMessage(err, 'Error al enviar el formulario. Intentá de nuevo.'));
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
      title: 'Primera visita al consultorio',
      desc: 'Si decidís participar, te recibimos en el consultorio para las evaluaciones neurológicas (M-CHAT-R, CARS-2, Vineland).',
    },
    {
      title: 'Segunda visita al laboratorio',
      desc: 'Se realiza una pequeña extracción de sangre para el análisis de anticuerpos. El equipo acompaña a la familia durante todo el proceso.',
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

  selectedAmount = signal<number | null>(null);
  customAmount = signal<number | null>(null);
  donacionState = signal<'idle' | 'loading'>('idle');
  donanteName = signal<string>('');
  donanteEmail = signal<string>('');

  selectAmount(amount: number): void {
    this.selectedAmount.set(amount);
    this.customAmount.set(null);
  }

  onCustomAmountChange(event: Event): void {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.customAmount.set(isNaN(value) ? null : value);
    this.selectedAmount.set(null);
  }

  donar(): void {
    const monto = this.selectedAmount() ?? this.customAmount();
    if (!monto || monto <= 0) return;

    this.donacionState.set('loading');
    const dto = {
      monto,
      ...(this.donanteName().trim() && { donante: this.donanteName().trim() }),
      ...(this.donanteEmail().trim() && { correo: this.donanteEmail().trim() }),
    };
    this.donacionService.iniciar(dto).subscribe({
      next: ({ initPoint }) => {
        window.location.href = initPoint;
      },
      error: () => {
        this.donacionState.set('idle');
      },
    });
  }
}
