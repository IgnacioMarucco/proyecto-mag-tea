import { ComoConocioProyecto } from './formulario-interes.model';

export type Sexo = 'FEMENINO' | 'MASCULINO';
export type TipoPaciente = 'CONTROL' | 'PROBLEMA';
export type MchatResultadoFinal = 'POSITIVA' | 'NEGATIVA';
export type MchatRiesgo = 'BAJO_RIESGO' | 'MEDIANO_RIESGO' | 'ALTO_RIESGO';
export type CarsResultado = 'MINIMO_NO_TEA' | 'LEVE_MODERADO' | 'SEVERO';
export type MchatEstado = 'PENDIENTE' | 'COMPLETADO' | 'EXPIRADO' | 'NO_ENVIADO';
export type PacienteEstado = 'ADMITIDO' | 'MCHAT_RESPONDIDO' | 'EXTRACCION_PENDIENTE' | 'EXTRACCION_REALIZADA';
export type CriteriosAptitud = 'APTO' | 'EXCLUIDO' | 'INCOMPLETO';

export interface PacientePorCodigo {
  id: number;
  codigoNumerico: string;
  nombreNino: string;
  apellidoNino: string;
  fechaTurnoExtraccion: string | null;
  tipoPaciente: TipoPaciente;
}

export interface PacienteListItem {
  id: number;
  codigoNumerico: string;
  apellidoTutor: string;
  nombreTutor: string;
  apellidoNino: string;
  nombreNino: string;
  fechaNacimientoNino: string | null;
  tipoPaciente: TipoPaciente;
  pacienteEstado: PacienteEstado;
  fechaPrimeraVisita: string | null;
  fechaTurnoExtraccion: string | null;
  proximaFechaEvento: string | null;
}

export interface PacienteCreate {
  formularioInteresId?: number;
  apellidoTutor: string;
  nombreTutor: string;
  correoTutor: string;
  telefono?: string;
  apellidoNino: string;
  nombreNino: string;
  fechaNacimientoNino?: string;
  sexo: Sexo;
  fechaPrimeraVisita: string;
  notas?: string;
  tipoPaciente: TipoPaciente;
  criterioTEADSMV: boolean;
  criterioTGDDSMIV: boolean;
  criterioEdad: boolean;
  epilepsia: boolean;
  paralisisCerebral: boolean;
  infeccionesCongenitas: boolean;
  lesionesEstructuralesSNC: boolean;
  facomatosis: boolean;
  patologiasNeurometabolicas: boolean;
  lesionesOcupantesEspacioSNC: boolean;
  patologiaPsiquiatrica: boolean;
  otrosSindromesGeneticos: boolean;
  pubertadPrecoz: boolean;
  consentimientoFirmado: boolean;
}

export interface PacienteUpdate {
  apellidoTutor: string;
  nombreTutor: string;
  correoTutor: string;
  telefono?: string;
  apellidoNino: string;
  nombreNino: string;
  fechaNacimientoNino?: string;
  sexo: Sexo;
  notas?: string;
  fechaPrimeraVisita?: string;
  fechaTurnoExtraccion?: string;
}

export interface PacienteCriterios {
  criterioTEADSMV: boolean;
  criterioTGDDSMIV: boolean;
  criterioEdad: boolean;
  epilepsia: boolean;
  paralisisCerebral: boolean;
  infeccionesCongenitas: boolean;
  lesionesEstructuralesSNC: boolean;
  facomatosis: boolean;
  patologiasNeurometabolicas: boolean;
  lesionesOcupantesEspacioSNC: boolean;
  patologiaPsiquiatrica: boolean;
  otrosSindromesGeneticos: boolean;
  pubertadPrecoz: boolean;
}

export interface PacienteMchatSeguimiento {
  item1: boolean;  item2: boolean;  item3: boolean;  item4: boolean;  item5: boolean;
  item6: boolean;  item7: boolean;  item8: boolean;  item9: boolean;  item10: boolean;
  item11: boolean; item12: boolean; item13: boolean; item14: boolean; item15: boolean;
  item16: boolean; item17: boolean; item18: boolean; item19: boolean; item20: boolean;
}

export interface PacienteCars {
  item1: number;  item2: number;  item3: number;  item4: number;  item5: number;
  item6: number;  item7: number;  item8: number;  item9: number;  item10: number;
  item11: number; item12: number; item13: number; item14: number; item15: number;
  obs1?: string;  obs2?: string;  obs3?: string;  obs4?: string;  obs5?: string;
  obs6?: string;  obs7?: string;  obs8?: string;  obs9?: string;  obs10?: string;
  obs11?: string; obs12?: string; obs13?: string; obs14?: string; obs15?: string;
  tScore?: number | null;
  percentil?: number | null;
}

export interface CarsItemsResponse {
  item1: number | null;  item2: number | null;  item3: number | null;
  item4: number | null;  item5: number | null;  item6: number | null;
  item7: number | null;  item8: number | null;  item9: number | null;
  item10: number | null; item11: number | null; item12: number | null;
  item13: number | null; item14: number | null; item15: number | null;
  obs1: string | null;   obs2: string | null;   obs3: string | null;
  obs4: string | null;   obs5: string | null;   obs6: string | null;
  obs7: string | null;   obs8: string | null;   obs9: string | null;
  obs10: string | null;  obs11: string | null;  obs12: string | null;
  obs13: string | null;  obs14: string | null;  obs15: string | null;
}

export interface PacienteVineland {
  comunicacion: number | null;
  autovalimiento: number | null;
  social: number | null;
  motor: number | null;
  cocienteFinal: number | null;
  conductaDesadaptativa: number | null;
  internalizante: number | null;
  externalizante: number | null;
}

export interface PacienteSegundaVisita {
  fechaTurnoExtraccion: string;
}

export interface PacienteConsentimiento {
  consentimientoFirmado: boolean;
}

export interface PacientePrimeraVisita {
  fechaPrimeraVisita: string;
}

export interface PacienteResponse {
  id: number;
  formularioInteresId: number | null;
  codigoNumerico: string;
  fechaContacto: string;
  apellidoTutor: string;
  nombreTutor: string;
  correoTutor: string;
  telefono: string | null;
  apellidoNino: string;
  nombreNino: string;
  fechaNacimientoNino: string | null;
  edadActual: number | null;
  edadMeses: number | null;
  comoConocioProyecto: ComoConocioProyecto | null;
  otroComoConocio: string | null;
  sexo: Sexo;
  tipoPaciente: TipoPaciente;
  fechaPrimeraVisita: string | null;
  consentimientoFirmado: boolean;
  notas: string | null;
  pacienteEstado: PacienteEstado;
  mchatEstado: MchatEstado;
  criteriosRegistrados: boolean;
  criteriosAptitud: CriteriosAptitud | null;
  criterioTEADSMV: boolean;
  criterioTGDDSMIV: boolean;
  criterioEdad: boolean;
  epilepsia: boolean;
  paralisisCerebral: boolean;
  infeccionesCongenitas: boolean;
  lesionesEstructuralesSNC: boolean;
  facomatosis: boolean;
  patologiasNeurometabolicas: boolean;
  lesionesOcupantesEspacioSNC: boolean;
  patologiaPsiquiatrica: boolean;
  otrosSindromesGeneticos: boolean;
  pubertadPrecoz: boolean;
  mchatScoreTotal: number | null;
  mchatSeguimientoFallas: number | null;
  mchatResultadoFinal: MchatResultadoFinal | null;
  mchatResultado: MchatRiesgo | null;
  seguimientoItem1:  boolean | null; seguimientoItem2:  boolean | null; seguimientoItem3:  boolean | null;
  seguimientoItem4:  boolean | null; seguimientoItem5:  boolean | null; seguimientoItem6:  boolean | null;
  seguimientoItem7:  boolean | null; seguimientoItem8:  boolean | null; seguimientoItem9:  boolean | null;
  seguimientoItem10: boolean | null; seguimientoItem11: boolean | null; seguimientoItem12: boolean | null;
  seguimientoItem13: boolean | null; seguimientoItem14: boolean | null; seguimientoItem15: boolean | null;
  seguimientoItem16: boolean | null; seguimientoItem17: boolean | null; seguimientoItem18: boolean | null;
  seguimientoItem19: boolean | null; seguimientoItem20: boolean | null;
  carsRawScore: number | null;
  carsTScore: number | null;
  carsPercentil: number | null;
  carsResultado: CarsResultado | null;
  carsItems: CarsItemsResponse | null;
  vinelandComunicacion: number | null;
  vinelandAutovalimiento: number | null;
  vinelandSocial: number | null;
  vinelandMotor: number | null;
  vinelandCocienteFinal: number | null;
  vinelandConductaDesadaptativa: number | null;
  vinelandInternalizante: number | null;
  vinelandExternalizante: number | null;
  fechaTurnoExtraccion: string | null;
  activo: boolean;
  createdAt: string;
}
