export type SexoRaton = 'MACHO' | 'HEMBRA';
export type VusBanda = 'AVERSIVA' | 'APETITIVA';
export type SocializacionResultado = 'NORMAL' | 'FALTA_SOCIALIZACION';

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
  m1TiempoRatonNovedad: number;
  m1TiempoObjetoNovedoso: number;
  m2TiempoRatonDesconocido: number;
  m2TiempoRatonFamiliar: number;
}

export interface TresCamarasResponse {
  m1TiempoRatonNovedad: number;
  m1TiempoObjetoNovedoso: number;
  m2TiempoRatonDesconocido: number;
  m2TiempoRatonFamiliar: number;
  sociabilizacion1: SocializacionResultado;
  sociabilizacion2: SocializacionResultado;
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
  posicion: string;
  cantidadConsumida: number | null;
  dia: number | null;
}

export interface ModeloAnimalListItem {
  id: number;
  identificador: string;
  poolId: number;
  poolRango: number;
  poolCodigo: string;
  camadaNombre: string;
  fechaNacimiento: string;
  sexo: SexoRaton;
  necesitaVocalizaciones: boolean;
  necesitaTresCamaras: boolean;
}

export interface ModeloAnimalResponse {
  id: number;
  identificador: string;
  poolId: number;
  poolRango: number;
  poolCodigo: string;
  camadaId: number;
  camadaNombre: string;
  fechaNacimiento: string;
  sexo: SexoRaton;
  fechaDia1Inoculacion: string;
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
  identificador: string;
  poolId: number;
  camadaId: number;
  fechaNacimiento: string;
  sexo: SexoRaton;
  fechaDia1Inoculacion: string;
  aportes?: ModeloAnimalPoolAporteInput[];
}

export interface ModeloAnimalUpdate extends ModeloAnimalCreate {}
