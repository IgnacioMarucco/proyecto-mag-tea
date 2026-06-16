import { ChangeDetectionStrategy, Component, OnInit, OnDestroy, input, output } from '@angular/core';
import { CdkTrapFocus } from '@angular/cdk/a11y';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-modal-container',
  imports: [IconComponent, CdkTrapFocus],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { role: 'dialog', 'aria-modal': 'true', 'aria-labelledby': 'modal-container-title' },
  template: `
    <div class="fixed inset-0 bg-sidebar/50 flex items-center justify-center z-50 p-5"
         (click)="closed.emit()">
      <div class="bg-surface border border-border rounded-lg shadow-md w-full flex flex-col"
           [class.max-w-sm]="size() === 'sm'"
           [class.max-w-xl]="size() === 'md'"
           [class.max-w-2xl]="size() === 'lg'"
           [class.max-w-3xl]="size() === 'xl'"
           style="max-height: 90vh"
           (click)="$event.stopPropagation()"
           (keydown.escape)="closed.emit()"
           cdkTrapFocus
           cdkTrapFocusAutoCapture>
        <div class="flex items-center justify-between px-6 pt-5 pb-4 border-b border-border shrink-0 rounded-t-lg">
          <h3 id="modal-container-title" class="text-base font-semibold text-text">{{ title() }}</h3>
          <div class="flex items-center gap-2">
            <ng-content select="[modal-header-actions]" />
            <button type="button" (click)="closed.emit()" aria-label="Cerrar"
              class="text-text-muted hover:text-text cursor-pointer">
              <app-icon name="x-mark" class="w-5 h-5" />
            </button>
          </div>
        </div>
        <div class="overflow-y-auto">
          <ng-content />
        </div>
      </div>
    </div>
  `,
})
export class ModalContainerComponent implements OnInit, OnDestroy {
  title  = input.required<string>();
  size   = input<'sm' | 'md' | 'lg' | 'xl'>('md');
  closed = output<void>();

  private trigger: HTMLElement | null = null;

  ngOnInit(): void {
    this.trigger = document.activeElement as HTMLElement;
  }

  ngOnDestroy(): void {
    this.trigger?.focus();
  }
}
