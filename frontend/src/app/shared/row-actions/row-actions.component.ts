import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { IconComponent } from '../icon/icon.component';

export type RowActionStyle = 'default' | 'primary' | 'danger';

export interface RowAction {
  label: string;
  style?: RowActionStyle;
  disabled?: boolean;
  hidden?: boolean;
  onClick: () => void;
}

@Component({
  selector: 'app-row-actions',
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './row-actions.component.html',
})
export class RowActionsComponent {
  actions = input.required<RowAction[]>();

  panelOpen     = signal(false);
  panelPosition = signal<{ top: number; right: number }>({ top: 0, right: 0 });

  visibleActions = computed(() => this.actions().filter(a => !a.hidden));

  togglePanel(event: MouseEvent): void {
    if (this.panelOpen()) {
      this.panelOpen.set(false);
      return;
    }
    const btn             = event.currentTarget as HTMLElement;
    const rect            = btn.getBoundingClientRect();
    const estimatedHeight = this.visibleActions().length * 36 + 8;
    const spaceBelow      = window.innerHeight - rect.bottom;
    const top             = spaceBelow >= estimatedHeight + 8
      ? rect.bottom + 4
      : rect.top - estimatedHeight - 4;
    this.panelPosition.set({ top, right: window.innerWidth - rect.right });
    this.panelOpen.set(true);
  }

  actionClass(style: RowActionStyle | undefined): string {
    if (style === 'primary') return 'text-primary hover:bg-primary-light';
    if (style === 'danger')  return 'text-danger hover:bg-danger/5';
    return 'text-text hover:bg-background';
  }

  execute(action: RowAction): void {
    if (action.disabled) return;
    this.panelOpen.set(false);
    action.onClick();
  }
}
