export interface InoculacionSemanalItem {
  modeloAnimalId: number;
  identificador: string;
  camadaNombre: string;
  poolCodigo: string;
  fechaDia1: string;
  diasPendientes: number[];
  diasHechos: number[];
}

export interface AlertaConductualItem {
  modeloAnimalId: number;
  identificador: string;
  camadaNombre: string;
  tipoTest: 'VOCALIZACIONES' | 'TRES_CAMARAS' | 'SACRIFICIO';
  fechaTest: string;
  diasRestantes: number;
}

export interface ActividadRecienteItem {
  tipo: 'SUERO' | 'POOL' | 'MODELO_ANIMAL' | 'INOCULACION' | 'PACIENTE' | 'MCHAT';
  descripcion: string;
  fecha: string;
  entityId: number;
  identificador: string;
  entityPath: string;
  nombreProfesional: string | null;
  rol: string | null;
}

export interface AgendaEvento {
  fecha: string;
  categoria: 'PRIMERA_VISITA' | 'EXTRACCION' | 'INOCULACION' | 'VOCALIZACIONES' | 'TRES_CAMARAS' | 'MICROSCOPIA';
  identificador: string;
  entityId: number;
  entityPath: string;
}

export interface InicioResponse {
  formulariosPendientes: number | null;
  agendaSemana: AgendaEvento[] | null;
  inoculacionesSemana: InoculacionSemanalItem[] | null;
  alertasConductuales: AlertaConductualItem[] | null;
  actividadReciente: ActividadRecienteItem[] | null;
}
