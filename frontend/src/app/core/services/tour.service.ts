import { Injectable } from '@angular/core';
import { driver } from 'driver.js';
import { TOUR_STEPS } from '../tour-steps';

@Injectable({ providedIn: 'root' })
export class TourService {
  start(route: string): void {
    const steps = TOUR_STEPS[route];
    if (!steps?.length) return;
    const visible = steps.filter(s => !s.element || !!document.querySelector(s.element as string));
    if (!visible.length) return;
    driver({
      showProgress: true,
      nextBtnText: 'Siguiente',
      prevBtnText: 'Anterior',
      doneBtnText: 'Listo',
      steps: visible,
    }).drive();
  }
}
