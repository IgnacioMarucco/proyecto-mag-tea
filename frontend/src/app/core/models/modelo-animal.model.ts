export type SexoRaton = 'MACHO' | 'HEMBRA';
export type VusBanda = 'AVERSIVA' | 'APETITIVA';
export type SocializacionResultado = 'NORMAL' | 'FALTA_SOCIALIZACION';
export type SueroUso = 'PROBLEMA' | 'CONTROL';

export interface VocalizacionesCreate {
  muestra1Khz: number;
  muestra2Khz: number;
}

export interface VocalizacionesResponse {
  muestra1Khz: number;
  muestra2Khz: number;
  vusBanda1: VusBanda;
  vusBanda2: VusBanda;
}

export interface TresCamarasCreate {
  m1TiempoRatonNovedad?: number;
  m1TiempoObjetoNovedoso?: number;
  m2TiempoRatonDesconocido?: number;
  m2TiempoRatonFamiliar?: number;
}

export interface TresCamarasResponse {
  m1TiempoRatonNovedad: number;
  m1TiempoObjetoNovedoso: number;
  m2TiempoRatonDesconocido: number | null;
  m2TiempoRatonFamiliar: number | null;
  sociabilizacion1: SocializacionResultado;
  sociabilizacion2: SocializacionResultado | null;
}

export interface MicroscopiaCreate {
  numCelulasGanglionares: number;
  numCelulasPurkinje: number;
}

export interface ModeloAnimalPoolAporteInput {
  poolTuboId: number;
  cantidadConsumida?: number;
  dia?: number;
}

export interface ModeloAnimalPoolAporteItem {
  poolTuboId: number;
  posicion: string | null;
  cantidadConsumida: number | null;
  dia: number | null;
}

export interface ModeloAnimalInoculacionCreate {
  fechaDia1Inoculacion: string;
  aportes?: ModeloAnimalPoolAporteInput[];
}

export interface ModeloAnimalListItem {
  id: number;
  identificador: string;
  poolId: number;
  poolRango: number;
  poolCodigo: string;
  poolUso: SueroUso;
  camadaNombre: string;
  fechaNacimiento: string;
  sexo: SexoRaton;
  aportesCount: number;
  necesitaVocalizaciones: boolean;
  necesitaTresCamaras: boolean;
}

export interface ModeloAnimalResponse {
  id: number;
  identificador: string;
  poolId: number;
  poolRango: number;
  poolCodigo: string;
  poolUso: SueroUso;
  camadaId: number;
  camadaNombre: string;
  fechaNacimiento: string;
  sexo: SexoRaton;
  fechaDia1Inoculacion: string | null;
  necesitaVocalizaciones: boolean;
  necesitaTresCamaras: boolean;
  vocalizaciones: VocalizacionesResponse | null;
  tresCamaras: TresCamarasResponse | null;
  numCelulasGanglionares: number | null;
  numCelulasPurkinje: number | null;
  aportes: ModeloAnimalPoolAporteItem[];
  activo: boolean;
  createdAt: string;
}

export interface ModeloAnimalCreate {
  poolId: number;
  camadaId: number;
  sexo: SexoRaton;
}

export interface ModeloAnimalUpdate extends ModeloAnimalCreate {}
