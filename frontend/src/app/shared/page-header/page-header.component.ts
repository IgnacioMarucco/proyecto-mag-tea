import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface Crumb { label: string; path?: string; }

@Component({
  selector: 'app-page-header',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './page-header.component.html',
})
export class PageHeaderComponent {
  readonly crumbs = input<Crumb[]>([]);
}
