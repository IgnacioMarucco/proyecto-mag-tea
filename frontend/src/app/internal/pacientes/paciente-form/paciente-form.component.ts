import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { PacienteService } from '../../../core/services/paciente.service';
import { extractErrorMessage } from '../../../shared/utils/error.utils';
import { FormularioInteresService } from '../../../core/services/formulario-interes.service';
import { AuthService } from '../../../core/services/auth.service';
import { PacienteCreate, PacienteUpdate } from '../../../core/models/paciente.model';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';

@Component({
  selector: 'app-paciente-form',
  imports: [ReactiveFormsModule, PageHeaderComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './paciente-form.component.html',
})
export class PacienteFormComponent implements OnInit {
  private readonly fb               = inject(FormBuilder);
  private readonly pacienteService  = inject(PacienteService);
  private readonly formularioService = inject(FormularioInteresService);
  private readonly authService      = inject(AuthService);
  private readonly router           = inject(Router);
  private readonly route            = inject(ActivatedRoute);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  formularioId = input<string>();
  id           = input<string>();

  readonly isEdit       = computed(() => !!this.id());
  private readonly isSecretaria = computed(() =>
    this.authService.currentUser()?.role === 'SECRETARIA'
  );

  loading     = signal(false);
  error       = signal<string | null>(null);
  currentStep = signal(1);

  form = this.fb.group({
    // Paso 1
    apellidoTutor:               ['', Validators.required],
    nombreTutor:                 ['', Validators.required],
    correoTutor:                 ['', [Validators.required, Validators.email]],
    telefono:                    [''],
    apellidoNino:                ['', Validators.required],
    nombreNino:                  ['', Validators.required],
    fechaNacimientoNino:         [''],
    sexo:                        ['', Validators.required],
    fechaPrimeraVisita:          ['', Validators.required],
    tipoPaciente:                ['', Validators.required],
    notas:                       [''],
    // Paso 2
    criterioTEADSMV:             [false],
    criterioTGDDSMIV:            [false],
    criterioEdad:                [false],
    epilepsia:                   [false],
    paralisisCerebral:           [false],
    infeccionesCongenitas:       [false],
    lesionesEstructuralesSNC:    [false],
    facomatosis:                 [false],
    patologiasNeurometabolicas:  [false],
    lesionesOcupantesEspacioSNC: [false],
    patologiaPsiquiatrica:       [false],
    otrosSindromesGeneticos:     [false],
    pubertadPrecoz:              [false],
    // Solo edición
    fechaExtraccion:             [''],
  });

  private readonly STEP1_FIELDS = [
    'apellidoTutor', 'nombreTutor', 'correoTutor',
    'apellidoNino', 'nombreNino', 'sexo', 'fechaPrimeraVisita', 'tipoPaciente',
  ] as const;

  private readonly formValue = toSignal(
    this.form.valueChanges.pipe(startWith(this.form.value)),
    { initialValue: this.form.value }
  );

  readonly tipoPacienteActual = computed(() =>
    this.formValue()?.tipoPaciente as 'CONTROL' | 'PROBLEMA' | ''
  );

  private readonly tieneExclusiones = computed(() => {
    const v = this.formValue();
    return !!(
      v.epilepsia || v.paralisisCerebral || v.infeccionesCongenitas ||
      v.lesionesEstructuralesSNC || v.facomatosis || v.patologiasNeurometabolicas ||
      v.lesionesOcupantesEspacioSNC || v.patologiaPsiquiatrica ||
      v.otrosSindromesGeneticos || v.pubertadPrecoz
    );
  });

  readonly criteriosApto = computed(() => {
    const v    = this.formValue();
    const tipo = v.tipoPaciente;
    if (!tipo || this.tieneExclusiones()) return false;
    if (tipo === 'PROBLEMA') return !!v.criterioTEADSMV && !!v.criterioTGDDSMIV && !!v.criterioEdad;
    return !v.criterioTEADSMV && !v.criterioTGDDSMIV && !!v.criterioEdad;
  });

  readonly aptitudWarning = computed((): string | null => {
    if (this.criteriosApto()) return null;
    const v    = this.formValue();
    const tipo = v.tipoPaciente;
    if (!tipo) return null;
    if (this.tieneExclusiones()) return 'El paciente presenta un criterio de exclusión y no puede ser admitido al protocolo.';
    if (tipo === 'PROBLEMA') return 'Para caso problema los tres criterios de inclusión deben cumplirse (TEA, TGD y edad).';
    if (v.criterioTEADSMV || v.criterioTGDDSMIV) return 'Para caso control los criterios clínicos TEA y TGD no deben presentarse.';
    return 'Para caso control debe cumplirse el criterio de edad.';
  });

  ngOnInit(): void {
    const fid = this.formularioId();
    if (fid && !this.isEdit()) {
      this.formularioService.findById(+fid).subscribe({
        next: f => this.form.patchValue({
          apellidoTutor:       f.apellidoTutor,
          nombreTutor:         f.nombreTutor,
          correoTutor:         f.correoTutor,
          telefono:            f.telefono ?? '',
          apellidoNino:        f.apellidoNino,
          nombreNino:          f.nombreNino,
          fechaNacimientoNino: f.fechaNacimientoNino ?? '',
        }),
        error: () => this.error.set('No se pudo cargar el formulario de interés'),
      });
    }

    const id = this.id();
    if (id) {
      this.pacienteService.findById(+id).subscribe({
        next: p => this.form.patchValue({
          apellidoTutor:       p.apellidoTutor,
          nombreTutor:         p.nombreTutor,
          correoTutor:         p.correoTutor,
          telefono:            p.telefono ?? '',
          apellidoNino:        p.apellidoNino,
          nombreNino:          p.nombreNino,
          fechaNacimientoNino: p.fechaNacimientoNino ?? '',
          sexo:                p.sexo,
          fechaPrimeraVisita:  p.fechaPrimeraVisita?.substring(0, 16) ?? '',
          notas:               p.notas ?? '',
          tipoPaciente:        p.tipoPaciente,
          fechaExtraccion:     p.fechaExtraccion ?? '',
        }),
        error: () => this.error.set('No se pudo cargar el paciente'),
      });
    }
  }

  fieldVal(key: string): boolean {
    return !!this.form.get(key)?.value;
  }

  goToStep2(): void {
    this.STEP1_FIELDS.forEach(f => this.form.get(f)?.markAsTouched());
    if (this.STEP1_FIELDS.some(f => this.form.get(f)?.invalid)) return;
    this.currentStep.set(2);
  }

  goToStep1(): void {
    this.currentStep.set(1);
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (!this.isEdit() && !this.criteriosApto()) return;
    this.loading.set(true);
    this.error.set(null);
    const v = this.form.value;

    if (this.isEdit()) {
      const dto: PacienteUpdate = {
        apellidoTutor:       v.apellidoTutor!,
        nombreTutor:         v.nombreTutor!,
        correoTutor:         v.correoTutor!,
        telefono:            v.telefono || undefined,
        apellidoNino:        v.apellidoNino!,
        nombreNino:          v.nombreNino!,
        fechaNacimientoNino: v.fechaNacimientoNino || undefined,
        sexo:                v.sexo as PacienteUpdate['sexo'],
        notas:               v.notas || undefined,
        fechaPrimeraVisita:  v.fechaPrimeraVisita || undefined,
        fechaExtraccion:     v.fechaExtraccion || undefined,
      };
      this.pacienteService.update(+this.id()!, dto).subscribe({
        next: p   => this.router.navigate(['/internal/pacientes', p.id]),
        error: err => { this.error.set(extractErrorMessage(err, 'Error al guardar')); this.loading.set(false); },
      });
    } else {
      const dto: PacienteCreate = {
        formularioInteresId:         this.formularioId() ? +this.formularioId()! : undefined,
        apellidoTutor:               v.apellidoTutor!,
        nombreTutor:                 v.nombreTutor!,
        correoTutor:                 v.correoTutor!,
        telefono:                    v.telefono || undefined,
        apellidoNino:                v.apellidoNino!,
        nombreNino:                  v.nombreNino!,
        fechaNacimientoNino:         v.fechaNacimientoNino || undefined,
        sexo:                        v.sexo as PacienteCreate['sexo'],
        fechaPrimeraVisita:          v.fechaPrimeraVisita!,
        notas:                       v.notas || undefined,
        tipoPaciente:                v.tipoPaciente as PacienteCreate['tipoPaciente'],
        criterioTEADSMV:             !!v.criterioTEADSMV,
        criterioTGDDSMIV:            !!v.criterioTGDDSMIV,
        criterioEdad:                !!v.criterioEdad,
        epilepsia:                   !!v.epilepsia,
        paralisisCerebral:           !!v.paralisisCerebral,
        infeccionesCongenitas:       !!v.infeccionesCongenitas,
        lesionesEstructuralesSNC:    !!v.lesionesEstructuralesSNC,
        facomatosis:                 !!v.facomatosis,
        patologiasNeurometabolicas:  !!v.patologiasNeurometabolicas,
        lesionesOcupantesEspacioSNC: !!v.lesionesOcupantesEspacioSNC,
        patologiaPsiquiatrica:       !!v.patologiaPsiquiatrica,
        otrosSindromesGeneticos:     !!v.otrosSindromesGeneticos,
        pubertadPrecoz:              !!v.pubertadPrecoz,
        consentimientoFirmado:       false,
      };
      this.pacienteService.create(dto).subscribe({
        next: p => {
          if (this.isSecretaria()) {
            this.router.navigate(['/internal/bandeja']);
          } else {
            this.router.navigate(['/internal/pacientes', p.id]);
          }
        },
        error: err => { this.error.set(extractErrorMessage(err, 'Error al registrar')); this.loading.set(false); },
      });
    }
  }

  cancel(): void {
    if (this.isEdit()) {
      this.router.navigate(['/internal/pacientes', this.id()]);
    } else if (this.isSecretaria()) {
      this.router.navigate(['/internal/bandeja']);
    } else {
      this.router.navigate(['/internal/pacientes']);
    }
  }
}
