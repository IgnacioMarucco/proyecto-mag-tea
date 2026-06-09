import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-mono" [class]="badgeClass()">
      {{ label() }}
    </span>
  `,
})
export class StatusBadgeComponent {
  value        = input<string | null | undefined>();
  labels       = input<Record<string, string>>({});
  colorMap     = input<Record<string, string>>({});
  defaultClass = input('bg-background text-text-muted border border-border');

  label = computed(() => {
    const v = this.value();
    return v ? (this.labels()[v] ?? v) : '';
  });

  badgeClass = computed(() => {
    const v = this.value();
    return v ? (this.colorMap()[v] ?? this.defaultClass()) : this.defaultClass();
  });
}
