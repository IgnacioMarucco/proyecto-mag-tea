import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-kpi-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <p class="text-xs font-medium uppercase tracking-wide text-slate-500">{{ label() }}</p>
      @if (value() !== null && value() !== undefined) {
        <p class="mt-1 text-3xl font-bold text-slate-800">{{ value() }}</p>
      } @else {
        <div class="mt-2 h-8 w-20 animate-pulse rounded bg-slate-100"></div>
      }
      @if (subtext()) {
        <p class="mt-1 text-xs text-slate-400">{{ subtext() }}</p>
      }
    </div>
  `,
})
export class KpiCardComponent {
  label = input.required<string>();
  value = input<number | string | null>(null);
  subtext = input<string>('');
}
