import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, input, output } from '@angular/core';
import { CdkTrapFocus } from '@angular/cdk/a11y';

@Component({
  selector: 'app-confirm-modal',
  imports: [CdkTrapFocus],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './confirm-modal.component.html',
  host: { role: 'dialog', 'aria-modal': 'true', 'aria-labelledby': 'confirm-modal-title' },
})
export class ConfirmModalComponent implements OnInit, OnDestroy {
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
