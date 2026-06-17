import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-kpi-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'flex h-full' },
  templateUrl: './kpi-card.component.html',
})
export class KpiCardComponent {
  label = input.required<string>();
  value = input<number | string | null>(null);
  subtext = input<string>('');
}
