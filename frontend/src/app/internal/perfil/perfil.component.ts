import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PerfilService } from '../../core/services/perfil.service';
import { ToastService } from '../../core/services/toast.service';
import { extractErrorMessage } from '../../shared/utils/error.utils';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { FirstFocusDirective } from '../../shared/directives/first-focus.directive';
import { passwordsMatchValidator } from '../../shared/validators/passwords-match.validator';
import { PerfilUpdate } from '../../core/models/perfil.model';

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, PageHeaderComponent, FirstFocusDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './perfil.component.html',
})
export class PerfilComponent {
  private readonly fb      = inject(FormBuilder);
  private readonly service = inject(PerfilService);
  private readonly toast   = inject(ToastService);

  readonly crumbs = [{ label: 'Mi Perfil' }];

  datosForm = this.fb.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', Validators.required],
  });

  passwordForm = this.fb.group({
    passwordActual: ['', Validators.required],
    passwordNueva: ['', [Validators.required, Validators.minLength(8)]],
    passwordNuevaConfirmar: ['', Validators.required],
  }, { validators: passwordsMatchValidator('passwordNueva', 'passwordNuevaConfirmar') });

  loadingDatos    = signal(false);
  errorDatos      = signal<string | null>(null);
  loadingPassword = signal(false);
  errorPassword   = signal<string | null>(null);

  constructor() {
    this.service.me().subscribe({
      next: p => this.datosForm.patchValue(p),
      error: () => this.errorDatos.set('No se pudieron cargar tus datos'),
    });
  }

  onSubmitDatos(): void {
    if (this.datosForm.invalid) {
      this.datosForm.markAllAsTouched();
      return;
    }

    this.loadingDatos.set(true);
    this.errorDatos.set(null);

    this.service.update(this.datosForm.getRawValue() as PerfilUpdate).subscribe({
      next: () => {
        this.toast.show('Datos actualizados');
        this.loadingDatos.set(false);
      },
      error: err => {
        this.errorDatos.set(extractErrorMessage(err, 'Error al guardar'));
        this.loadingDatos.set(false);
      },
    });
  }

  onSubmitPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.loadingPassword.set(true);
    this.errorPassword.set(null);

    const { passwordActual, passwordNueva } = this.passwordForm.getRawValue();
    this.service.changePassword({ passwordActual: passwordActual!, passwordNueva: passwordNueva! }).subscribe({
      next: () => {
        this.toast.show('Contraseña actualizada');
        this.passwordForm.reset();
        this.loadingPassword.set(false);
      },
      error: err => {
        this.errorPassword.set(extractErrorMessage(err, 'No se pudo cambiar la contraseña'));
        this.loadingPassword.set(false);
      },
    });
  }
}
