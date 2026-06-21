import { ChangeDetectionStrategy, Component, input, signal } from '@angular/core';

@Component({
  selector: 'app-copy-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button type="button"
            (click)="copiar()"
            class="group relative inline-flex items-center gap-1.5 font-mono text-xs text-text-muted bg-surface border border-border rounded px-2 py-0.5 hover:bg-background hover:text-text cursor-pointer transition-all active:scale-95"
            [title]="copiado() ? '¡Copiado!' : 'Copiar al portapapeles'">
      {{ value() }}

      @if (copiado()) {
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5" stroke="currentColor" class="w-3.5 h-3.5 text-success">
          <path stroke-linecap="round" stroke-linejoin="round" d="m4.5 12.75 6 6 9-13.5" />
        </svg>
      } @else {
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-3.5 h-3.5 text-text-muted/60 group-hover:text-text transition-colors">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 17.25v3.375c0 .621-.504 1.125-1.125 1.125h-9.75a1.125 1.125 0 0 1-1.125-1.125V7.875c0-.621.504-1.125 1.125-1.125H5.25m11.9-3.664A2.251 2.251 0 0 0 15 2.25h-3a2.251 2.251 0 0 0-2.15 1.586m5.8 0c.065.21.1.433.1.664v.75h-6V4.5c0-.231.035-.454.1-.664M6.75 7.375c0-.621.504-1.125 1.125-1.125h6.75c.621 0 1.125.504 1.125 1.125v1.875m-7.5-1.875v1.875m6-1.875v1.875m-6 3.75h6.75M9 13.5h3" />
        </svg>
      }

      @if (copiado()) {
        <span class="absolute bottom-full left-1/2 -translate-x-1/2 mb-1.5 px-2 py-0.5 text-[10px] font-sans font-medium text-white bg-black/85 rounded shadow-sm whitespace-nowrap">
          ¡Copiado!
        </span>
      }
    </button>
  `,
})
export class CopyBadgeComponent {
  readonly value = input.required<string>();

  copiado = signal(false);

  copiar(): void {
    navigator.clipboard.writeText(this.value()).then(() => {
      this.copiado.set(true);
      setTimeout(() => this.copiado.set(false), 2000);
    });
  }
}
