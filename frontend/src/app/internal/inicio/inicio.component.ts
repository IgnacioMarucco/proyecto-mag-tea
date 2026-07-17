import {
  ChangeDetectionStrategy, Component, computed, inject, signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LucideAngularModule, AlertTriangle, CheckCircle, Activity, Inbox } from 'lucide-angular';
import { InicioService } from '../../core/services/inicio.service';
import { AgendaEvento } from '../../core/models/inicio.model';
import { PageHeaderComponent } from '../../shared/page-header/page-header.component';
import { FechaPipe } from '../../core/pipes/fecha.pipe';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink, FechaPipe, LucideAngularModule, PageHeaderComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './inicio.component.html',
})
export class InicioComponent {
  private readonly service = inject(InicioService);

  readonly AlertTriangleIcon = AlertTriangle;
  readonly CheckCircleIcon   = CheckCircle;
  readonly ActivityIcon      = Activity;
  readonly InboxIcon         = Inbox;

  readonly error   = signal(false);
  readonly loading = signal(true);

  readonly crumbs = [{ label: 'Inicio' }];

  private readonly data = toSignal(
    this.service.get().pipe(
      tap(() => this.loading.set(false)),
      catchError(() => {
        this.error.set(true);
        this.loading.set(false);
        return of(null);
      }),
    ),
    { initialValue: null },
  );

  readonly formulariosPendientes = computed(() => this.data()?.formulariosPendientes ?? null);
  readonly inoculaciones         = computed(() => this.data()?.inoculacionesSemana ?? []);
  readonly alertas               = computed(() => this.data()?.alertasConductuales ?? []);
  readonly actividad             = computed(() => this.data()?.actividadReciente ?? []);

  readonly alertasCriticas = computed(() =>
    this.alertas().filter(a => a.diasRestantes <= 0).length);

  // ── Agenda semanal ────────────────────────────────────────────────────────

  readonly CATEGORIAS: AgendaEvento['categoria'][] = [
    'PRIMERA_VISITA', 'EXTRACCION', 'INOCULACION',
    'VOCALIZACIONES', 'TRES_CAMARAS', 'MICROSCOPIA',
  ];

  readonly CATEGORIA_LABELS: Record<AgendaEvento['categoria'], string> = {
    PRIMERA_VISITA: 'Primera visita',
    EXTRACCION:     'Extracciones',
    INOCULACION:    'Inoculaciones',
    VOCALIZACIONES: 'Vocalizaciones',
    TRES_CAMARAS:   'Tres cámaras',
    MICROSCOPIA:    'Microscopía',
  };

  readonly semanaActual  = this.generarDias(0);
  readonly semanaProxima = this.generarDias(7);

  private generarDias(offsetDias: number) {
    const hoy = new Date();
    const toIso = (d: Date) =>
      `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    const todayIso = toIso(hoy);
    const domingo = new Date(hoy);
    domingo.setDate(hoy.getDate() - hoy.getDay() + offsetDias);
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(domingo);
      d.setDate(domingo.getDate() + i);
      const iso = toIso(d);
      return {
        iso,
        diaNombre: d.toLocaleDateString('es-AR', { weekday: 'short' }),
        diaNum: d.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit' }),
        isToday: iso === todayIso,
      };
    });
  }

  private readonly agendaMap = computed(() => {
    const map = new Map<string, AgendaEvento[]>();
    (this.data()?.agendaSemana ?? []).forEach(e => {
      const key = `${e.fecha}::${e.categoria}`;
      const list = map.get(key) ?? [];
      list.push(e);
      map.set(key, list);
    });
    return map;
  });

  getEventos(iso: string, categoria: AgendaEvento['categoria']): AgendaEvento[] {
    return this.agendaMap().get(`${iso}::${categoria}`) ?? [];
  }

  readonly agendaVacia = computed(() =>
    (this.data()?.agendaSemana ?? []).length === 0);

  rolLabel(rol: string | null): string {
    const labels: Record<string, string> = {
      INVESTIGADOR_PRINCIPAL: 'IP',
      CUERPO_MEDICO:          'Médico',
      CUERPO_TECNICO:         'Técnico',
    };
    return rol ? (labels[rol] ?? rol) : '';
  }

}
