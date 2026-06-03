import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-confirm-modal',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './confirm-modal.component.html',
  host: { role: 'dialog', 'aria-modal': 'true' },
})
export class ConfirmModalComponent {
  title         = input('¿Confirmás esta acción?');
  message       = input('');
  confirmLabel  = input('Confirmar');
  cancelLabel   = input('Cancelar');
  danger        = input(true);

  confirmed = output<void>();
  cancelled = output<void>();
}
