import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ProfesionalService } from '../../../core/services/profesional.service';
import { extractErrorMessage } from '../../../shared/utils/error.utils';
import { ROLE_LABELS, ROLES, ProfesionalCreate, ProfesionalUpdate } from '../../../core/models/profesional.model';
import { Crumb, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { FirstFocusDirective } from '../../../shared/directives/first-focus.directive';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-profesional-form',
  imports: [ReactiveFormsModule, PageHeaderComponent, FirstFocusDirective, ConfirmModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profesional-form.component.html',
  host: { '(keydown)': 'onKeydown($event)' },
})
export class ProfesionalFormComponent {
  private readonly elRef   = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly toast   = inject(ToastService);
  private readonly fb      = inject(FormBuilder);
  private readonly service = inject(ProfesionalService);
  private readonly router  = inject(Router);
  private readonly route   = inject(ActivatedRoute);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  id = input<string>();

  readonly roles = ROLES;
  readonly roleLabels = ROLE_LABELS;

  form = this.fb.group({
    nombre: ['', Validators.required],
    apellido: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', Validators.required],
    password: ['', [Validators.minLength(8)]],
    role: ['', Validators.required],
  });

  loading         = signal(false);
  error           = signal<string | null>(null);
  showExitConfirm = signal(false);

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

  readonly isEdit = computed(() => !!this.id());

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

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const firstInvalid = this.elRef.nativeElement.querySelector<HTMLElement>('[aria-invalid="true"]');
      firstInvalid?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      firstInvalid?.focus();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const id = this.id();
    const request$ = id
      ? this.service.update(+id, this.form.value as ProfesionalUpdate)
      : this.service.create(this.form.value as ProfesionalCreate);

    request$.subscribe({
      next: () => { this.toast.show(this.isEdit() ? 'Profesional actualizado' : 'Profesional creado'); this.router.navigate(['/internal/profesionales']); },
      error: err => {
        this.error.set(extractErrorMessage(err, 'Error al guardar'));
        this.loading.set(false);
      },
    });
  }

  cancel(): void {
    this.form.reset();
    this.router.navigate(['/internal/profesionales']);
  }
}
