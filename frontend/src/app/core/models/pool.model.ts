import { MotivoVaciado, SueroUso } from './suero.model';

export interface PoolTuboInput {
  posicion: string;
  /** Cantidad ACTUAL del tubo (lo que contiene ahora). El backend preserva lo consumido. */
  cantidad: number;
}

export interface PoolTuboItem {
  id: number;
  posicion: string;
  cantidadInicial: number;
  cantidadRestante: number;
  motivoVaciado?: MotivoVaciado;
}

export interface SueroTuboAporte {
  sueroTuboId: number;
  cantidadAportada: number;
}

export interface PoolSueroAporteItem {
  sueroId: number;
  codigoSuero: string;
  codigoPaciente: string;
  cantidadAportada: number;
  sueroActivo: boolean;
}

export interface PoolListItem {
  id: number;
  codigo: string;
  rango: number;
  uso: SueroUso;
  fechaCreacion: string;
  cantidadAportes: number;
  cantidadTotal: number;
  cantidadRestante: number;
  cajaDescripcion: string;
  modelosAnimalesCount: number;
}

export interface PoolResponse {
  id: number;
  codigo: string;
  rango: number;
  uso: SueroUso;
  fechaCreacion: string;
  cantidadTotal: number;
  cantidadRestante: number;
  cajaId: number;
  freezer: string;
  cajon: number;
  cajaNumero: number;
  tubos: PoolTuboItem[];
  aportes: PoolSueroAporteItem[];
  activo: boolean;
  createdAt: string;
}

export interface PoolCreate {
  cajaId: number;
  aportes: SueroTuboAporte[];
  tubos: PoolTuboInput[];
}

export interface PoolUpdate {
  cajaId: number;
  tubos: PoolTuboInput[];
  fechaCreacion: string;
}
