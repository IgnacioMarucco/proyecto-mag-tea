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
}

export interface MchatData {
  distribucionScores: Distribucion[];
  resultadoFinal: Distribucion[];
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

export interface CorrelacionPunto {
  x: number;
  y: number;
  codigoNumerico: string;
}

export type EjeCorrelacion = 'MCHAT_SCORE' | 'CARS_RAW' | 'VINELAND_COCIENTE';

export const EJE_LABELS: Record<EjeCorrelacion, string> = {
  MCHAT_SCORE: 'M-CHAT Score',
  CARS_RAW: 'CARS-2 Raw Score',
  VINELAND_COCIENTE: 'Vineland Cociente',
};

export const PARES_CORRELACION: Array<{ x: EjeCorrelacion; y: EjeCorrelacion }> = [
  { x: 'MCHAT_SCORE', y: 'CARS_RAW' },
  { x: 'MCHAT_SCORE', y: 'VINELAND_COCIENTE' },
  { x: 'CARS_RAW',    y: 'VINELAND_COCIENTE' },
];
