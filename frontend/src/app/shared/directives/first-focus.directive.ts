import { Directive, ElementRef, afterNextRender, inject } from '@angular/core';

@Directive({ selector: '[appFirstFocus]' })
export class FirstFocusDirective {
  private readonly elRef = inject<ElementRef<HTMLElement>>(ElementRef);

  constructor() {
    afterNextRender(() => {
      this.elRef.nativeElement
        .querySelector<HTMLElement>('input:not([type=hidden]), select, textarea')
        ?.focus();
    });
  }
}
