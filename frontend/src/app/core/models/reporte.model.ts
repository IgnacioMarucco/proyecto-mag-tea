export interface FiltroReportes {
  tipoPaciente: 'TODOS' | 'PROBLEMA' | 'CONTROL';
  edades: number[];   // grupos etarios seleccionados: 2 = 18-35m, 3 = 36-47m, 4 = 48-59m, 5 = 60-120m
}

export const FILTRO_DEFAULT: FiltroReportes = {
  tipoPaciente: 'TODOS',
  edades: [],
};

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
  totalConSuero: number;
  totalSinSuero: number;
  mediaBtu: number;
  sdBtu: number;
  medianaBtu: number;
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
  pValue: number | null;
  n: number;
}

export interface EstadisticasGrupo {
  nTotal: number;
  nConSuero: number;
  pctConSuero: number;
  mediaBtu: number;
  sdBtu: number;
  medianaBtu: number;
}

export interface ComparacionGrupos {
  problema: EstadisticasGrupo;
  control: EstadisticasGrupo;
  pValue: number | null;
  cohenD: number | null;
}

export type EjeCorrelacion = 'MCHAT_SCORE' | 'CARS_RAW' | 'VINELAND_COCIENTE' | 'BTU_VALUE';

export const EJE_LABELS: Record<EjeCorrelacion, string> = {
  MCHAT_SCORE:       'M-CHAT Tamizaje (score)',
  CARS_RAW:          'CARS-2 Raw Score',
  VINELAND_COCIENTE: 'Vineland Cociente',
  BTU_VALUE:         'Anticuerpos BTU',
};

export const PARES_CORRELACION: Array<{ x: EjeCorrelacion; y: EjeCorrelacion }> = [
  { x: 'MCHAT_SCORE',       y: 'CARS_RAW' },
  { x: 'MCHAT_SCORE',       y: 'VINELAND_COCIENTE' },
  { x: 'CARS_RAW',          y: 'VINELAND_COCIENTE' },
  { x: 'BTU_VALUE',         y: 'MCHAT_SCORE' },
  { x: 'BTU_VALUE',         y: 'CARS_RAW' },
  { x: 'BTU_VALUE',         y: 'VINELAND_COCIENTE' },
];

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
