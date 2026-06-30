export type SexoRaton = 'MACHO' | 'HEMBRA';
export type VusBanda = 'AVERSIVA' | 'APETITIVA';
export type SocializacionResultado = 'NORMAL' | 'FALTA_SOCIALIZACION';
export type SueroUso = 'PROBLEMA' | 'CONTROL';
export type TipoImagenMicroscopia = 'GANGLIONAR' | 'PURKINJE';
export type EstadoProtocolo =
  | 'PENDIENTE_INOCULACION'
  | 'INOCULACION_EN_CURSO'
  | 'PENDIENTE_VOCALIZACIONES'
  | 'PENDIENTE_TRES_CAMARAS'
  | 'PENDIENTE_MICROSCOPIA'
  | 'COMPLETO';

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

export interface ImagenMicroscopiaResponse {
  id: number;
  tipo: TipoImagenMicroscopia;
  documentoId: number | null;
  urlExterna: string | null;
  descripcion: string | null;
  createdAt: string;
}

export interface ImagenMicroscopiaCreate {
  tipo: TipoImagenMicroscopia;
  documentoId?: number;
  urlExterna?: string;
  descripcion?: string;
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
  estadoProtocolo: EstadoProtocolo;
  fechaProximoEvento: string | null;
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
  estadoProtocolo: EstadoProtocolo;
  fechaProximoEvento: string | null;
  vocalizaciones: VocalizacionesResponse | null;
  tresCamaras: TresCamarasResponse | null;
  numCelulasGanglionares: number | null;
  numCelulasPurkinje: number | null;
  imagenesMicroscopia: ImagenMicroscopiaResponse[];
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

export type TipoPaciente = 'CONTROL' | 'PROBLEMA';
export type MchatResultadoFinal = 'POSITIVA' | 'NEGATIVA';
export type MchatRiesgo = 'BAJO_RIESGO' | 'MEDIANO_RIESGO' | 'ALTO_RIESGO';
export type CarsResultado = 'MINIMO_NO_TEA' | 'LEVE_MODERADO' | 'SEVERO';

export interface InoculacionReporteItem {
  dia: number;
  fecha: string | null;
  cantidadConsumida: number | null;
}

export interface PoolReporteItem {
  codigo: string;
  rango: number;
  uso: SueroUso;
  fechaCreacion: string;
  freezer: string;
  cajon: number;
  numeroCaja: number;
  poolActivo: boolean;
}

export interface CarsItemsResponse {
  item1: number | null;  item2: number | null;  item3: number | null;
  item4: number | null;  item5: number | null;  item6: number | null;
  item7: number | null;  item8: number | null;  item9: number | null;
  item10: number | null; item11: number | null; item12: number | null;
  item13: number | null; item14: number | null; item15: number | null;
  obs1: string | null;  obs2: string | null;  obs3: string | null;
  obs4: string | null;  obs5: string | null;  obs6: string | null;
  obs7: string | null;  obs8: string | null;  obs9: string | null;
  obs10: string | null; obs11: string | null; obs12: string | null;
  obs13: string | null; obs14: string | null; obs15: string | null;
}

export interface PacienteReporteItem {
  codigoNumerico: string;
  tipoPaciente: TipoPaciente;
  fechaPrimeraVisita: string | null;
  mchatFamiliaItems: boolean[] | null;
  mchatScoreTotal: number | null;
  mchatRiesgo: MchatRiesgo | null;
  mchatResultadoFinal: MchatResultadoFinal | null;
  mchatSeguimientoItems: boolean[] | null;
  mchatSeguimientoFallas: number | null;
  carsItems: CarsItemsResponse | null;
  carsRawScore: number | null;
  carsTScore: number | null;
  carsPercentil: number | null;
  carsResultado: CarsResultado | null;
  vinelandComunicacion: number | null;
  vinelandAutovalimiento: number | null;
  vinelandSocial: number | null;
  vinelandMotor: number | null;
  vinelandCocienteFinal: number | null;
  vinelandConductaDesadaptativa: number | null;
  vinelandInternalizante: number | null;
  vinelandExternalizante: number | null;
}

export interface SueroReporteItem {
  valorAnticuerpos: number | null;
  rango: number | null;
  fechaExtraccion: string;
  paciente: PacienteReporteItem | null;
}

export interface ModeloAnimalReporte {
  id: number;
  identificador: string;
  sexo: SexoRaton;
  camadaNombre: string;
  fechaNacimiento: string;
  fechaDia1Inoculacion: string | null;
  inoculaciones: InoculacionReporteItem[];
  pool: PoolReporteItem;
  sueros: SueroReporteItem[];
  vocalizaciones: VocalizacionesResponse | null;
  tresCamaras: TresCamarasResponse | null;
  numCelulasGanglionares: number | null;
  numCelulasPurkinje: number | null;
  createdAt: string;
}
