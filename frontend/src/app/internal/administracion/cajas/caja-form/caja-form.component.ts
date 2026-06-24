import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EMPTY, catchError, filter, map, switchMap } from 'rxjs';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { CajaService } from '../../../../core/services/caja.service';
import { CajaCreate, CajaUpdate } from '../../../../core/models/caja.model';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { Crumb, PageHeaderComponent } from '../../../../shared/page-header/page-header.component';
import { FirstFocusDirective } from '../../../../shared/directives/first-focus.directive';
import { ConfirmModalComponent } from '../../../../shared/confirm-modal/confirm-modal.component';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
  selector: 'app-caja-form',
  imports: [ReactiveFormsModule, PageHeaderComponent, FirstFocusDirective, ConfirmModalComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './caja-form.component.html',
  host: { '(keydown)': 'onKeydown($event)' },
})
export class CajaFormComponent {
  private readonly elRef      = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly toast      = inject(ToastService);
  private readonly fb         = inject(FormBuilder);
  private readonly service    = inject(CajaService);
  private readonly router     = inject(Router);
  private readonly route      = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly crumbs = toSignal(
    this.route.data.pipe(map(d => d['crumbs'] as Crumb[] ?? [])),
    { initialValue: [] as Crumb[] }
  );

  id              = input<string>();
  loading         = signal(false);
  error           = signal<string | null>(null);
  showExitConfirm = signal(false);

  form = this.fb.group({
    freezer: ['',                  Validators.required],
    cajon:   [null as number | null, [Validators.required, Validators.min(1)]],
    numero:  [null as number | null, [Validators.required, Validators.min(1)]],
  });

  readonly isEdit = computed(() => !!this.id());

  constructor() {
    toObservable(this.id).pipe(
      filter((id): id is string => !!id),
      switchMap(id => this.service.findById(+id).pipe(
        catchError(() => { this.error.set('No se pudo cargar la caja'); return EMPTY; })
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(caja => this.form.patchValue(caja));
  }

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

    const raw = this.form.value;
    const dto: CajaCreate = {
      freezer: raw.freezer!,
      cajon:   Number(raw.cajon),
      numero:  Number(raw.numero),
    };

    const id = this.id();
    const req$ = id
      ? this.service.update(+id, dto as CajaUpdate)
      : this.service.create(dto);

    req$.subscribe({
      next:  () => { this.toast.show(this.isEdit() ? 'Caja actualizada' : 'Caja creada'); this.router.navigate(['/internal/cajas']); },
      error: err => { this.error.set(extractErrorMessage(err, 'Error al guardar')); this.loading.set(false); },
    });
  }

  cancel(): void {
    this.form.reset();
    this.router.navigate(['/internal/cajas']);
  }
}
