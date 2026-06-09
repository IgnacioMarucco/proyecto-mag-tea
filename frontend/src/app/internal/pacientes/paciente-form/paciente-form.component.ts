import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PacienteService } from '../../../core/services/paciente.service';
import { FormularioInteresService } from '../../../core/services/formulario-interes.service';
import { AuthService } from '../../../core/services/auth.service';
import { PacienteCreate, PacienteUpdate } from '../../../core/models/paciente.model';
import { IconComponent } from '../../../shared/icon/icon.component';

@Component({
  selector: 'app-paciente-form',
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './paciente-form.component.html',
})
export class PacienteFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly pacienteService = inject(PacienteService);
  private readonly formularioService = inject(FormularioInteresService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  private get isSecretaria(): boolean {
    return this.authService.currentUser()?.role === 'SECRETARIA';
  }

  formularioId = input<string>();
  id = input<string>();

  loading = signal(false);
  error = signal<string | null>(null);

  form = this.fb.group({
    apellidoTutor:       ['', Validators.required],
    nombreTutor:         ['', Validators.required],
    correoTutor:         ['', [Validators.required, Validators.email]],
    telefono:            [''],
    apellidoNino:        ['', Validators.required],
    nombreNino:          ['', Validators.required],
    fechaNacimientoNino: [''],
    sexo:                ['', Validators.required],
    fechaPrimeraVisita:  ['', Validators.required],
    notas:               [''],
  });

  get isEdit(): boolean { return !!this.id(); }

  constructor() {
    // Ajustar validaciones según modo
    effect(() => {
      const ctrl = this.form.get('fechaPrimeraVisita')!;
      if (this.id()) {
        ctrl.clearValidators();
      } else {
        ctrl.setValidators(Validators.required);
      }
      ctrl.updateValueAndValidity();
    });

    // Precarga desde formulario de interés (alta desde bandeja)
    effect(() => {
      const fid = this.formularioId();
      if (fid && !this.id()) {
        this.formularioService.findById(+fid).subscribe({
          next: f => this.form.patchValue({
            apellidoTutor: f.apellidoTutor,
            nombreTutor:   f.nombreTutor,
            correoTutor:   f.correoTutor,
            telefono:      f.telefono ?? '',
            apellidoNino:  f.apellidoNino,
            nombreNino:    f.nombreNino,
            fechaNacimientoNino: f.fechaNacimientoNino ?? '',
          }),
          error: () => this.error.set('No se pudo cargar el formulario de interés'),
        });
      }
    });

    // Carga datos en modo edición
    effect(() => {
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
            notas:               p.notas ?? '',
          }),
          error: () => this.error.set('No se pudo cargar el paciente'),
        });
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.error.set(null);
    const v = this.form.value;

    if (this.isEdit) {
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
      };
      this.pacienteService.update(+this.id()!, dto).subscribe({
        next: p => this.router.navigate(['/internal/pacientes', p.id]),
        error: err => { this.error.set(err.error?.message ?? 'Error al guardar'); this.loading.set(false); },
      });
    } else {
      const dto: PacienteCreate = {
        formularioInteresId: this.formularioId() ? +this.formularioId()! : undefined,
        apellidoTutor:       v.apellidoTutor!,
        nombreTutor:         v.nombreTutor!,
        correoTutor:         v.correoTutor!,
        telefono:            v.telefono || undefined,
        apellidoNino:        v.apellidoNino!,
        nombreNino:          v.nombreNino!,
        fechaNacimientoNino: v.fechaNacimientoNino || undefined,
        sexo:                v.sexo as PacienteCreate['sexo'],
        fechaPrimeraVisita:  v.fechaPrimeraVisita!,
      };
      this.pacienteService.create(dto).subscribe({
        next: p => {
          if (this.isSecretaria) {
            this.router.navigate(['/internal/bandeja']);
          } else {
            this.router.navigate(['/internal/pacientes', p.id]);
          }
        },
        error: err => { this.error.set(err.error?.message ?? 'Error al registrar'); this.loading.set(false); },
      });
    }
  }

  cancel(): void {
    if (this.isEdit) {
      this.router.navigate(['/internal/pacientes', this.id()]);
    } else if (this.isSecretaria) {
      this.router.navigate(['/internal/bandeja']);
    } else {
      this.router.navigate(['/internal/pacientes']);
    }
  }
}
