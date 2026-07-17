export type SueroUso = 'CONTROL' | 'PROBLEMA';

export type MotivoVaciado = 'CONSUMIDO' | 'PERDIDO' | 'VENCIDO' | 'CONTAMINADO' | 'OTRO';

export interface VaciarTuboPayload {
  motivo: MotivoVaciado;
  notas?: string;
}

export interface SueroTuboInput {
  posicion: string;
  /** Cantidad ACTUAL del tubo (lo que contiene ahora). El backend preserva lo consumido. */
  cantidad: number;
}

export interface SueroTuboItem {
  id: number;
  posicion: string;
  cantidadInicial: number;
  cantidadRestante: number;
  motivoVaciado?: MotivoVaciado;
}

export interface SueroListItem {
  id: number;
  pacienteId: number;
  codigoNumerico: string;
  fechaExtraccion: string;
  valorAnticuerpos: number | null;
  rango: number | null;
  uso: SueroUso | null;
  cantidadRestante: number;
  cantidadTotal: number;
}

export interface SueroResponse {
  id: number;
  pacienteId: number;
  codigoNumerico: string;
  cajaId: number;
  freezer: string;
  cajon: number;
  cajaNumero: number;
  tubos: SueroTuboItem[];
  fechaExtraccion: string;
  cantidadTotal: number;
  cantidadRestante: number;
  valorAnticuerpos: number | null;
  rango: number | null;
  uso: SueroUso | null;
  activo: boolean;
  createdAt: string;
}

export interface SueroCreate {
  pacienteId: number;
  cajaId: number;
  tubos: SueroTuboInput[];
  fechaExtraccion: string;
  valorAnticuerpos: number | null;
}

export interface SueroUpdate {
  cajaId: number;
  tubos: SueroTuboInput[];
  fechaExtraccion: string;
  valorAnticuerpos: number | null;
}

export interface SueroDisponibilidad {
  uso: SueroUso;
  rango: number;
  cantidadSueros: number;
  mlDisponibles: number;
  ratonesPosibles: number;
}
