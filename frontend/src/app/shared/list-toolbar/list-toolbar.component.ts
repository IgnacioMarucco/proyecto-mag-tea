import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';

export interface ToolbarFilter {
  key: string;
  label: string;
  count?: number;
}

@Component({
  selector: 'app-list-toolbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './list-toolbar.component.html',
})
export class ListToolbarComponent {
  placeholder = input('Buscar…');
  filters     = input<ToolbarFilter[]>([]);
  activeFilter = input<string>('');

  searchChange = output<string>();
  filterChange = output<string>();

  searchValue = signal('');

  onInput(value: string): void {
    this.searchValue.set(value);
    this.searchChange.emit(value);
  }

  clearSearch(): void {
    this.searchValue.set('');
    this.searchChange.emit('');
  }
}
