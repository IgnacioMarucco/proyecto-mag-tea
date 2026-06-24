import {
  ChangeDetectionStrategy, Component, OnInit, computed, inject, input, output, signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, of } from 'rxjs';
import { ModeloAnimalService } from '../../../../core/services/modelo-animal.service';
import { PoolService } from '../../../../core/services/pool.service';
import { ModeloAnimalResponse } from '../../../../core/models/modelo-animal.model';
import { PoolTuboItem } from '../../../../core/models/pool.model';
import { extractErrorMessage } from '../../../../shared/utils/error.utils';
import { ToastService } from '../../../../core/services/toast.service';

interface DiaRow {
  dia: number;
  fecha: string | null;
  tuboId: number | null;
  cantidadConsumida: number | null;
  posicion: string | null;
}

@Component({
  selector: 'app-inoculacion-section',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './inoculacion-section.component.html',
})
export class InoculacionSectionComponent implements OnInit {
  private readonly service     = inject(ModeloAnimalService);
  private readonly poolService = inject(PoolService);
  private readonly fb          = inject(FormBuilder);
  private readonly toast       = inject(ToastService);

  modeloAnimal = input.required<ModeloAnimalResponse>();
  updated      = output<ModeloAnimalResponse>();

  savingDia    = signal<number | null>(null);
  saveError    = signal<string | null>(null);
  tubosPools   = signal<PoolTuboItem[]>([]);

  // Form control solo para fecha del día 1
  formFechaDia1 = this.fb.control('', Validators.required);

  // Estado local de selección por día (índice 0 = día 1, etc.)
  private perDiaState = signal<{ tuboId: number | null; cantidad: number | null }[]>([
    { tuboId: null, cantidad: null },
    { tuboId: null, cantidad: null },
    { tuboId: null, cantidad: null },
    { tuboId: null, cantidad: null },
  ]);

  readonly diaRows = computed<DiaRow[]>(() => {
    const ma = this.modeloAnimal();
    const fecha1 = ma.fechaDia1Inoculacion;
    return [1, 2, 3, 4].map(dia => {
      const aporte = ma.aportes.find(a => a.dia === dia);
      const fecha = fecha1
        ? new Date(new Date(fecha1 + 'T00:00:00').getTime() + (dia - 1) * 86400000)
            .toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' })
        : null;
      return {
        dia,
        fecha,
        tuboId: aporte?.poolTuboId ?? null,
        cantidadConsumida: aporte?.cantidadConsumida ?? null,
        posicion: aporte?.posicion ?? null,
      };
    });
  });

  // Día N (N > 1) visible si el aporte del día N-1 existe
  aporteExiste(dia: number): boolean {
    return this.modeloAnimal().aportes.some(a => a.dia === dia);
  }

  ngOnInit(): void {
    this.poolService.findById(this.modeloAnimal().poolId)
      .pipe(catchError(() => of(null)))
      .subscribe(pool => {
        if (pool) this.tubosPools.set(pool.tubos.filter(t => t.cantidadRestante > 0));
      });
  }

  setTubo(dia: number, event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    this.perDiaState.update(arr => {
      const next = [...arr];
      next[dia - 1] = { ...next[dia - 1], tuboId: val ? Number(val) : null };
      return next;
    });
  }

  setCantidad(dia: number, event: Event): void {
    const val = (event.target as HTMLInputElement).value;
    this.perDiaState.update(arr => {
      const next = [...arr];
      next[dia - 1] = { ...next[dia - 1], cantidad: val ? Number(val) : null };
      return next;
    });
  }

  saveDia(dia: number): void {
    if (this.savingDia() !== null) return;

    const state = this.perDiaState()[dia - 1];

    if (dia === 1) {
      if (this.formFechaDia1.invalid) { this.formFechaDia1.markAsTouched(); return; }
      if (!state.tuboId) { this.saveError.set('Seleccioná un tubo para el día 1'); return; }
    } else {
      if (!state.tuboId) { this.saveError.set(`Seleccioná un tubo para el día ${dia}`); return; }
    }

    this.savingDia.set(dia);
    this.saveError.set(null);

    const fechaDia1 = dia === 1
      ? this.formFechaDia1.value!
      : this.modeloAnimal().fechaDia1Inoculacion!;

    this.service.patchInoculacion(this.modeloAnimal().id, {
      fechaDia1Inoculacion: fechaDia1,
      aportes: [{
        poolTuboId:        state.tuboId!,
        cantidadConsumida: state.cantidad ?? undefined,
        dia,
      }],
    }).subscribe({
      next: ma => {
        this.toast.show('Inoculación guardada');
        this.updated.emit(ma);
        this.savingDia.set(null);
        // Limpiar estado local del día guardado
        this.perDiaState.update(arr => {
          const next = [...arr];
          next[dia - 1] = { tuboId: null, cantidad: null };
          return next;
        });
      },
      error: err => {
        this.saveError.set(extractErrorMessage(err, 'Error al guardar'));
        this.savingDia.set(null);
      },
    });
  }
}
