import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, input, output } from '@angular/core';
import { CdkTrapFocus } from '@angular/cdk/a11y';

@Component({
  selector: 'app-confirm-modal',
  imports: [CdkTrapFocus],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './confirm-modal.component.html',
  host: { role: 'dialog', 'aria-modal': 'true', '[attr.aria-labelledby]': 'titleId' },
})
export class ConfirmModalComponent implements OnInit, OnDestroy {
  readonly titleId = `confirm-modal-${crypto.randomUUID?.() ?? Math.random().toString(36).slice(2)}`;

  title         = input('¿Confirmás esta acción?');
  message       = input('');
  confirmLabel  = input('Confirmar');
  cancelLabel   = input('Cancelar');
  danger        = input(true);

  confirmed = output<void>();
  cancelled = output<void>();

  private trigger: HTMLElement | null = null;

  ngOnInit(): void {
    this.trigger = document.activeElement as HTMLElement;
  }

  ngOnDestroy(): void {
    this.trigger?.focus();
  }
}
