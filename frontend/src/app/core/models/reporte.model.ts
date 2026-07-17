export interface ResumenGeneral {
  totalFormularios: number;
  formulariosContactados: number;
  formulariosAdmitidos: number;
  pacientesTotal: number;
  pacientesProblema: number;
  pacientesControl: number;
  mchatCompletados: number;
  extraccionesRealizadas: number;
}

export interface EtapaEmbudo {
  nombre: string;
  n: number;
  porcentajeRespecto1raEtapa: number;
}

export interface EmbudoData {
  etapas: EtapaEmbudo[];
}

export interface Distribucion {
  label: string;
  n: number;
  porcentaje: number;
}

export interface DemograficoData {
  sexo: Distribucion[];
  fuenteDerivacion: Distribucion[];
  distribucionEtaria: Distribucion[];
}

export interface MchatData {
  distribucionScores: Distribucion[];
  resultadoFinal: Distribucion[];
  mediaScore: number;
  sdScore: number;
  totalConMchat: number;
  riesgoMedio: number;
  riesgoMedioConSeguimiento: number;
  riesgoMedioPositiva: number;
  riesgoMedioNegativa: number;
  itemsFalladosTamizaje: Distribucion[];
  totalConSeguimiento: number;
  itemsFalladosSeguimiento: Distribucion[];
}

export interface CarsData {
  distribucionRawScore: Distribucion[];
  minimoNoTea: number;
  leveModerado: number;
  severo: number;
  mediaRawScore: number;
  sdRawScore: number;
  totalConCars: number;
}

export interface VinelandData {
  mediaComunicacion: number;
  mediaAutovalimiento: number;
  mediaSocial: number;
  mediaMotor: number;
  mediaCocienteFinal: number;
  sdCocienteFinal: number;
  mediaConductaDesadaptativa: number | null;
  mediaInternalizante: number | null;
  mediaExternalizante: number | null;
  totalConVineland: number;
}

export interface AnticuerposData {
  distribucionRangos: Distribucion[];
}

export interface CorrelacionPunto {
  x: number;
  y: number;
  codigoNumerico: string;
  tipoPaciente: 'PROBLEMA' | 'CONTROL';
}

export interface CorrelacionResponse {
  puntos: CorrelacionPunto[];
  r: number | null;
  n: number;
}

export interface EstadisticasGrupo {
  nTotal: number;
  nConSuero: number;
  pctConSuero: number;
  mediaBtu: number;
  sdBtu: number;
}

export interface ComparacionGrupos {
  problema: EstadisticasGrupo;
  control: EstadisticasGrupo;
}

export type EjeCorrelacion = 'MCHAT_SCORE' | 'CARS_RAW' | 'VINELAND_COCIENTE' | 'BTU_VALUE';

export const EJE_LABELS: Record<EjeCorrelacion, string> = {
  MCHAT_SCORE:       'M-CHAT (score final)',
  CARS_RAW:          'CARS-2 Raw Score',
  VINELAND_COCIENTE: 'Vineland CCA',
  BTU_VALUE:         'Anticuerpos BTU',
};

export const PARES_CORRELACION: Array<{ x: EjeCorrelacion; y: EjeCorrelacion }> = [
  { x: 'BTU_VALUE', y: 'MCHAT_SCORE' },
  { x: 'BTU_VALUE', y: 'CARS_RAW' },
  { x: 'BTU_VALUE', y: 'VINELAND_COCIENTE' },
];

export interface PuntoTemporal {
  periodo: string;
  monto: number;
}

export interface Donante {
  donante: string | null;
  correo: string | null;
}

export interface DonacionesAnalitica {
  totalRecaudado: number;
  cantidadAprobadas: number;
  montoPromedio: number;
  recaudacionPorMes: PuntoTemporal[];
  porEstado: Distribucion[];
  donantes: Donante[];
}

export interface DashboardAnalitica {
  resumen: ResumenGeneral;
  embudo: EmbudoData;
  demografico: DemograficoData;
  mchat: MchatData;
  cars: CarsData;
  vineland: VinelandData;
  anticuerpos: AnticuerposData;
  comparacion: ComparacionGrupos;
}
