import { ComoConocioProyecto } from './formulario-interes.model';

export type Sexo = 'FEMENINO' | 'MASCULINO';
export type MchatResultadoFinal = 'POSITIVA' | 'NEGATIVA';
export type MchatRiesgo = 'BAJO_RIESGO' | 'MEDIANO_RIESGO' | 'ALTO_RIESGO';
export type CarsResultado = 'MINIMO_NO_TEA' | 'LEVE_MODERADO' | 'SEVERO';
export type MchatEstado = 'PENDIENTE' | 'COMPLETADO' | 'EXPIRADO' | 'NO_ENVIADO';
export type PacienteEstado = 'ADMITIDO' | 'MCHAT_RESPONDIDO' | 'EXTRACCION_PENDIENTE' | 'EXTRACCION_REALIZADA';

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
  consentimientoFirmado: boolean;
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
}

export interface PacienteVineland {
  vinelandComunicacion: number | null;
  vinelandAutovalimiento: number | null;
  vinelandSocial: number | null;
  vinelandMotor: number | null;
  vinelandCocienteFinal: number | null;
  vinelandConductaDesadaptativa: number | null;
  vinelandInternalizante: number | null;
  vinelandExternalizante: number | null;
}

export interface PacienteSegundaVisita {
  fechaExtraccion: string;
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
  comoConocioProyecto: ComoConocioProyecto | null;
  sexo: Sexo;
  fechaPrimeraVisita: string | null;
  consentimientoFirmado: boolean;
  notas: string | null;
  pacienteEstado: PacienteEstado;
  mchatEstado: MchatEstado;
  criteriosRegistrados: boolean;
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
  carsResultado: CarsResultado | null;
  vinelandComunicacion: number | null;
  vinelandAutovalimiento: number | null;
  vinelandSocial: number | null;
  vinelandMotor: number | null;
  vinelandCocienteFinal: number | null;
  vinelandConductaDesadaptativa: number | null;
  vinelandInternalizante: number | null;
  vinelandExternalizante: number | null;
  fechaExtraccion: string | null;
  activo: boolean;
  createdAt: string;
}
