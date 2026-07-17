import {
  ChangeDetectionStrategy, Component, DestroyRef, inject, input, signal,
} from '@angular/core';
import { catchError, filter, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ModeloAnimalService } from '../../../core/services/modelo-animal.service';
import { CarsItemsResponse, ModeloAnimalReporte } from '../../../core/models/modelo-animal.model';
import { FechaPipe } from '../../../core/pipes/fecha.pipe';
import { USO_LABELS } from '../../../shared/utils/btu.utils';
import { BANDA_LABELS, SEXO_LABELS, SOCIALIZACION_LABELS } from '../../../shared/utils/modelo-animal.utils';

@Component({
  selector: 'app-modelo-animal-reporte',
  imports: [FechaPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './modelo-animal-reporte.component.html',
  styles: [`
    :host { display: block; }

    .reporte-pagina {
      width: 794px;
      min-height: 1123px;
      margin: 0 auto;
      padding: 48px 56px;
      background: white;
      color: #111;
      font-family: 'DM Sans', sans-serif;
      font-size: 12px;
      line-height: 1.5;
    }

    @media print {
      .controles-impresion { display: none !important; }
      .reporte-pagina {
        width: 100%;
        margin: 0;
        padding: 0;
        box-shadow: none;
      }
      footer {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 10px 0;
        background: white;
        border-top: 1px solid #e5e7eb;
        margin-top: 0;
      }
    }

    @media screen {
      :host {
        background: #f0f0f0;
        min-height: 100vh;
        padding: 76px 16px 32px; /* 44px barra fija + 32px espacio original */
      }
      .reporte-pagina {
        box-shadow: 0 4px 24px rgba(0,0,0,0.12);
      }
    }

    .page-num::after { content: ''; }
    @media print {
      @page { margin: 1cm 1.4cm; }
      .page-num::after { content: ' · Pág. ' counter(page) ' de ' counter(pages); }
    }
  `],
})
export class ModeloAnimalReporteComponent {
  readonly identificador = input.required<string>();

  private readonly service    = inject(ModeloAnimalService);
  private readonly destroyRef = inject(DestroyRef);

  reporte  = signal<ModeloAnimalReporte | null>(null);
  loading  = signal(true);
  error    = signal<string | null>(null);

  readonly fechaGeneracion = new Date().toLocaleDateString('es-AR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
  });

  readonly usoLabels = USO_LABELS;
  readonly sexoLabels = SEXO_LABELS;
  readonly tipoLabels: Record<string, string> = {
    CONTROL: 'Control', PROBLEMA: 'Caso problema',
  };
  readonly bandaLabels        = BANDA_LABELS;
  readonly socializacionLabels = SOCIALIZACION_LABELS;
  readonly mchatRiesgoLabels: Record<string, string> = {
    BAJO_RIESGO: 'Bajo riesgo', MEDIANO_RIESGO: 'Riesgo mediano', ALTO_RIESGO: 'Alto riesgo',
  };
  readonly mchatResultadoLabels: Record<string, string> = {
    POSITIVA: 'Positivo', NEGATIVA: 'Negativo',
  };
  readonly carsResultadoLabels: Record<string, string> = {
    MINIMO_NO_TEA: 'Mínimo / No TEA', LEVE_MODERADO: 'Leve-Moderado', SEVERO: 'Severo',
  };

  readonly mchatPreguntas: string[] = [
    'Si señalás algo al otro lado del cuarto, ¿lo mira?',
    '¿Alguna vez pensaste que podría ser sordo/a?',
    '¿Juega a "hacer como si..."; por ej. tomar de una taza vacía, hablar por teléfono?',
    '¿Le gusta trepar a las cosas? (muebles, columpios, toboganes)',
    '¿Hace movimientos inusuales con los dedos cerca de los ojos?',
    '¿Señala con el índice para pedir algo o conseguir ayuda?',
    '¿Señala con el índice para mostrarte algo interesante?',
    '¿Está interesado/a en otros niños?',
    '¿Te trae cosas para mostrártelas (no para conseguir ayuda, sino para compartir)?',
    '¿Responde cuando lo/a llamás por su nombre?',
    'Cuando le sonreís, ¿él/ella te sonríe?',
    '¿Se molesta con los ruidos cotidianos? (aspiradora, música alta)',
    '¿Camina?',
    '¿Te mira a los ojos cuando le hablás, jugás o lo/la vestís?',
    '¿Imita lo que hacés? (saludar con la mano, aplaudir, hacer ruidos)',
    'Si girás a mirar algo, ¿también lo mira para ver qué estás mirando?',
    '¿Intenta que lo/la mires cuando hace algo?',
    '¿Comprende cuando le pedís que haga algo (sin señalar)?',
    'Si algo es nuevo, ¿mira tu cara para ver si es seguro?',
    '¿Le gustan las actividades de movimiento? (que lo/la balanceen, saltar en las rodillas)',
  ];

  readonly carsCategorias: string[] = [
    'Relación con las personas',
    'Imitación',
    'Respuesta emocional',
    'Uso del cuerpo',
    'Uso de los objetos',
    'Adaptación al cambio',
    'Respuesta visual',
    'Respuesta auditiva',
    'Respuesta al tacto, olfato y gusto',
    'Miedo y ansiedad',
    'Comunicación verbal',
    'Comunicación no verbal',
    'Nivel de actividad',
    'Nivel e integración de respuestas intelectuales',
    'Impresión general',
  ];

  readonly vinelandDominios: { key: keyof Pick<import('../../../core/models/modelo-animal.model').PacienteReporteItem,
    'vinelandComunicacion'|'vinelandAutovalimiento'|'vinelandSocial'|'vinelandMotor'|
    'vinelandCocienteFinal'|'vinelandConductaDesadaptativa'|'vinelandInternalizante'|'vinelandExternalizante'>;
    label: string }[] = [
    { key: 'vinelandComunicacion',          label: 'Comunicación' },
    { key: 'vinelandAutovalimiento',        label: 'Autovalimiento' },
    { key: 'vinelandSocial',                label: 'Socialización' },
    { key: 'vinelandMotor',                 label: 'Habilidades motoras' },
    { key: 'vinelandCocienteFinal',         label: 'Cociente adaptativo (CCA)' },
    { key: 'vinelandConductaDesadaptativa', label: 'Conducta desadaptativa' },
    { key: 'vinelandInternalizante',        label: 'Internalizante' },
    { key: 'vinelandExternalizante',        label: 'Externalizante' },
  ];

  constructor() {
    toObservable(this.identificador).pipe(
      filter(id => !!id),
      tap(() => { this.loading.set(true); this.error.set(null); }),
      switchMap(id => this.service.getReporte(id).pipe(catchError(() => of(null)))),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(r => {
      this.reporte.set(r);
      this.loading.set(false);
      if (!r) this.error.set('No se pudo cargar el reporte.');
    });
  }

  cerrar(): void { window.close(); }

  print(): void {
    const r = this.reporte();
    const titulo = r ? `ficha_${r.identificador}` : 'ficha_reporte';
    const tituloAnterior = document.title;
    document.title = titulo;
    window.print();
    document.title = tituloAnterior;
  }

  carsToArray(items: CarsItemsResponse): { puntaje: number | null; obs: string | null }[] {
    return Array.from({ length: 15 }, (_, i) => ({
      puntaje: (items as unknown as Record<string, number | null>)[`item${i + 1}`],
      obs:     (items as unknown as Record<string, string | null>)[`obs${i + 1}`],
    }));
  }

  // Convención seguimiento: true = Pasa EXCEPTO ítems 2, 5, 12 (invertidos)
  seguimientoPasa(valor: boolean, itemIndex: number): boolean {
    const invertidos = [1, 4, 11]; // índices 0-based para ítems 2, 5, 12
    return invertidos.includes(itemIndex) ? !valor : valor;
  }

  familiaEsProblematica(valor: boolean, itemIndex: number): boolean {
    const invertidos = [1, 4, 11];
    return invertidos.includes(itemIndex) ? valor : !valor;
  }

  tieneObservacionesCars(items: CarsItemsResponse): boolean {
    return this.carsToArray(items).some(f => f.obs && f.obs.trim() !== '' && f.obs !== 'Obs');
  }

  mchatBadgeStyle(riesgo: string | undefined): string {
    const base = 'display:inline-block;font-size:9px;padding:2px 7px;border-radius:999px;font-weight:700;margin-left:6px;';
    if (riesgo === 'ALTO_RIESGO')    return base + 'background:#fee2e2;color:#991b1b;';
    if (riesgo === 'MEDIANO_RIESGO') return base + 'background:#fef9c3;color:#854d0e;';
    if (riesgo === 'BAJO_RIESGO')    return base + 'background:#dcfce7;color:#166534;';
    return base + 'background:#f3f4f6;color:#374151;';
  }

  mchatResultadoBadgeStyle(resultado: string | undefined): string {
    const base = 'display:inline-block;font-size:9px;padding:2px 7px;border-radius:999px;font-weight:700;margin-left:4px;';
    if (resultado === 'POSITIVA') return base + 'background:#fee2e2;color:#991b1b;';
    if (resultado === 'NEGATIVA') return base + 'background:#dcfce7;color:#166534;';
    return '';
  }

  carsBadgeStyle(resultado: string | undefined): string {
    const base = 'display:inline-block;font-size:9px;padding:2px 7px;border-radius:999px;font-weight:700;margin-left:6px;';
    if (resultado === 'SEVERO')        return base + 'background:#fee2e2;color:#991b1b;';
    if (resultado === 'LEVE_MODERADO') return base + 'background:#fef9c3;color:#854d0e;';
    if (resultado === 'MINIMO_NO_TEA') return base + 'background:#dcfce7;color:#166534;';
    return base + 'background:#f3f4f6;color:#374151;';
  }
}
