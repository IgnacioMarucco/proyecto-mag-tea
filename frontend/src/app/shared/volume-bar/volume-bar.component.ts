import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MlPipe } from '../../core/pipes/ml.pipe';

@Component({
  selector: 'app-volume-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MlPipe],
  template: `
    @let pct = total() > 0 ? value() / total() : 0;
    <div class="w-28">
      <div class="h-1.5 bg-border rounded-full overflow-hidden mb-1">
        <div class="h-full rounded-full"
             [style.width.%]="pct * 100"
             [class]="pct > 0.5 ? 'bg-accent' : pct > 0.2 ? 'bg-warning' : 'bg-danger'">
        </div>
      </div>
      <span class="text-xs font-mono text-text">{{ value() | ml }}</span>
      <span class="text-xs font-mono text-text-muted"> / {{ total() | ml }} ml</span>
    </div>
  `,
})
export class VolumeBarComponent {
  readonly value = input.required<number>();
  readonly total = input.required<number>();
}
