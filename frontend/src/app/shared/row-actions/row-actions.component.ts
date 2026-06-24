import { ChangeDetectionStrategy, Component, ElementRef, computed, effect, input, signal, viewChild, viewChildren } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter, fromEvent, merge } from 'rxjs';
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
  host: { '(keydown)': 'onKeydown($event)' },
})
export class RowActionsComponent {
  private readonly triggerRef  = viewChild<ElementRef<HTMLButtonElement>>('triggerBtn');
  private readonly actionItems = viewChildren<ElementRef<HTMLButtonElement>>('actionItem');

  actions = input.required<RowAction[]>();

  panelOpen     = signal(false);
  panelPosition = signal<{ top: number; right: number }>({ top: 0, right: 0 });

  visibleActions = computed(() => this.actions().filter(a => !a.hidden));

  constructor() {
    effect(() => {
      if (!this.panelOpen() || this.actionItems().length === 0) return;
      this.actionItems().find(r => !r.nativeElement.disabled)?.nativeElement.focus();
    });

    merge(
      fromEvent(window, 'resize'),
      fromEvent(window, 'scroll', { capture: true }),
    ).pipe(
      filter(() => this.panelOpen()),
      takeUntilDestroyed(),
    ).subscribe(() => this.panelOpen.set(false));
  }

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

  onKeydown(event: KeyboardEvent): void {
    if (!this.panelOpen()) return;
    const items = this.actionItems()
      .filter(r => !r.nativeElement.disabled)
      .map(r => r.nativeElement);
    const idx = items.indexOf(document.activeElement as HTMLButtonElement);
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      items[Math.min(idx + 1, items.length - 1)]?.focus();
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      items[Math.max(idx - 1, 0)]?.focus();
    } else if (event.key === 'Escape') {
      this.panelOpen.set(false);
      this.triggerRef()?.nativeElement.focus();
    }
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
