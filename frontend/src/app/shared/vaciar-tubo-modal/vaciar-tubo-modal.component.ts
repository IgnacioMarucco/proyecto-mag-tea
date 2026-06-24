import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  OnDestroy,
  inject,
  input,
  output,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CdkTrapFocus } from '@angular/cdk/a11y';
import { MotivoVaciado, VaciarTuboPayload } from '../../core/models/suero.model';

const MOTIVOS: { value: MotivoVaciado; label: string }[] = [
  { value: 'CONSUMIDO',   label: 'Consumido (uso normal)' },
  { value: 'PERDIDO',     label: 'Perdido (corte de luz, accidente)' },
  { value: 'VENCIDO',     label: 'Vencido' },
  { value: 'CONTAMINADO', label: 'Contaminado' },
  { value: 'OTRO',        label: 'Otro' },
];

@Component({
  selector: 'app-vaciar-tubo-modal',
  imports: [CdkTrapFocus, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './vaciar-tubo-modal.component.html',
  host: { role: 'dialog', 'aria-modal': 'true', 'aria-labelledby': 'vaciar-modal-title' },
})
export class VaciarTuboModalComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);

  modo = input<'tubo' | 'grilla'>('tubo');

  confirmed = output<VaciarTuboPayload>();
  cancelled = output<void>();

  readonly motivos = MOTIVOS;
  readonly form: FormGroup = this.fb.group({
    motivo: ['', Validators.required],
    notas: [''],
  });

  private trigger: HTMLElement | null = null;

  get titulo(): string {
    return this.modo() === 'grilla' ? 'Liberar grilla' : 'Vaciar tubo';
  }

  get descripcion(): string {
    return this.modo() === 'grilla'
      ? 'Se vaciarán todos los tubos de este elemento. Las celdas de la grilla quedarán disponibles.'
      : 'Se marcará este tubo como vacío. La celda de la grilla quedará disponible.';
  }

  ngOnInit(): void {
    this.trigger = document.activeElement as HTMLElement;
  }

  ngOnDestroy(): void {
    this.trigger?.focus();
  }

  onConfirm(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { motivo, notas } = this.form.value;
    this.confirmed.emit({ motivo, notas: notas?.trim() || undefined });
  }
}
