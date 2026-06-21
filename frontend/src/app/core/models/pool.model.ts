import { SueroUso } from './suero.model';

export interface PoolTuboInput {
  posicion: string;
  cantidadInicial: number;
}

export interface PoolTuboItem {
  id: number;
  posicion: string;
  cantidadInicial: number;
  cantidadRestante: number;
}

export interface SueroTuboAporte {
  sueroTuboId: number;
  cantidadAportada: number;
}

export interface PoolSueroAporteItem {
  sueroTuboId: number;
  posicion: string;
  codigoSuero: string;
  codigoPaciente: string;
  cantidadAportada: number;
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
  tubos: PoolTuboItem[];
  aportes: PoolSueroAporteItem[];
  activo: boolean;
  createdAt: string;
}

export interface PoolCreate {
  cajaId: number;
  fechaCreacion: string;
  aportes: SueroTuboAporte[];
  tubos: PoolTuboInput[];
}

export interface PoolUpdate {
  cajaId: number;
  tubos: PoolTuboInput[];
  fechaCreacion: string;
}
