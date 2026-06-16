import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

type ResultadoStatus = 'approved' | 'pending' | 'failure' | 'unknown';

@Component({
  selector: 'app-donacion-resultado',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './donacion-resultado.component.html',
})
export class DonacionResultadoComponent {
  private readonly route = inject(ActivatedRoute);

  private readonly queryParams = toSignal(
    this.route.queryParamMap.pipe(map(p => p.get('status') ?? 'unknown')),
    { initialValue: 'unknown' }
  );

  readonly status = computed<ResultadoStatus>(() => {
    const s = this.queryParams();
    return (s === 'approved' || s === 'pending' || s === 'failure') ? s : 'unknown';
  });

  readonly config = computed(() => {
    switch (this.status()) {
      case 'approved': return {
        icon: 'check',
        iconBg: 'bg-success/10',
        iconColor: 'text-success',
        title: '¡Gracias por tu donación!',
        message: 'Tu pago fue aprobado. Agradecemos tu apoyo. Si tenés alguna pregunta, no dudes en contactarnos.',
      };
      case 'pending': return {
        icon: 'clock',
        iconBg: 'bg-warning/10',
        iconColor: 'text-warning',
        title: 'Pago en proceso',
        message: 'Tu pago está siendo procesado.',
      };
      default: return {
        icon: 'x',
        iconBg: 'bg-danger/10',
        iconColor: 'text-danger',
        title: 'El pago no se completó',
        message: 'Hubo un problema con el pago. Podés intentarlo de nuevo cuando quieras.',
      };
    }
  });
}
