import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProfesionalService } from '../../../core/services/profesional.service';
import { ROLE_LABELS, ROLES, ProfesionalCreate, ProfesionalUpdate } from '../../../core/models/profesional.model';

@Component({
  selector: 'app-profesional-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profesional-form.component.html',
})
export class ProfesionalFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProfesionalService);
  private readonly router = inject(Router);

  id = input<string>();

  readonly roles = ROLES;
  readonly roleLabels = ROLE_LABELS;

  form = this.fb.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(8)]],
    role: ['', Validators.required],
  });

  loading = signal(false);
  error = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.id();
      if (id) {
        this.service.findById(+id).subscribe({
          next: p => {
            this.form.patchValue(p);
            this.form.get('password')?.clearValidators();
            this.form.get('password')?.updateValueAndValidity();
          },
          error: () => this.error.set('No se pudo cargar el profesional'),
        });
      }
    });
  }

  get isEdit(): boolean {
    return !!this.id();
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.error.set(null);

    const id = this.id();
    const request$ = id
      ? this.service.update(+id, this.form.value as ProfesionalUpdate)
      : this.service.create(this.form.value as ProfesionalCreate);

    request$.subscribe({
      next: () => this.router.navigate(['/internal/profesionales']),
      error: err => {
        this.error.set(err.error?.message ?? 'Error al guardar');
        this.loading.set(false);
      },
    });
  }

  cancel(): void {
    this.form.reset();
    this.router.navigate(['/internal/profesionales']);
  }
}
