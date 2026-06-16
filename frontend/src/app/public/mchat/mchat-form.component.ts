import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { MchatService, MchatPublicInfo, MchatSubmit } from '../../core/services/mchat.service';
import { MchatPreguntasComponent } from '../../shared/mchat-preguntas/mchat-preguntas.component';

@Component({
  selector: 'app-mchat-form',
  imports: [MchatPreguntasComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './mchat-form.component.html',
})
export class MchatFormComponent implements OnInit {
  private readonly service = inject(MchatService);

  token = input.required<string>();

  paciente    = signal<MchatPublicInfo | null>(null);
  loadError   = signal<string | null>(null);
  submitState = signal<'idle' | 'loading' | 'success' | 'error'>('idle');
  submitError = signal<string | null>(null);

  constructor() {
    inject(Title).setTitle('MAG-TEA — Cuestionario M-CHAT-R');
    inject(Meta).addTags([
      { name: 'description', content: 'Cuestionario M-CHAT-R para la detección temprana del Trastorno del Espectro Autista. Proyecto MAG-TEA.' },
      { property: 'og:title', content: 'MAG-TEA — Cuestionario M-CHAT-R' },
      { property: 'og:description', content: 'Completá el cuestionario enviado por el equipo MAG-TEA.' },
      { property: 'og:type', content: 'website' },
    ]);
  }

  ngOnInit(): void {
    const t = this.token();
    if (!t) return;
    this.service.getFormulario(t).subscribe({
      next:  p  => this.paciente.set(p),
      error: () => this.loadError.set('El enlace no es válido o ha expirado.'),
    });
  }

  onSubmit(respuestas: boolean[]): void {
    this.submitState.set('loading');
    const dto = Object.fromEntries(respuestas.map((v, i) => [`p${i + 1}`, v])) as unknown as MchatSubmit;
    this.service.submitRespuestas(this.token(), dto).subscribe({
      next:  () => this.submitState.set('success'),
      error: err => {
        this.submitError.set(err.error?.message ?? 'Error al enviar. Intentá de nuevo.');
        this.submitState.set('error');
      },
    });
  }
}
