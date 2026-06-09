export type ComoConocioProyecto =
  | 'INSTAGRAM'
  | 'SUGERIDO_PARTICIPANTE'
  | 'SUGERIDO_EQUIPO_TERAPEUTICO'
  | 'SUGERIDO_MEDICO'
  | 'OTRO';

export type EstadoFormulario = 'PENDIENTE' | 'CONTACTADO' | 'ADMITIDO' | 'DESCARTADO';

export interface FormularioInteresCreate {
  apellidoTutor: string;
  nombreTutor: string;
  correoTutor: string;
  telefono?: string;
  apellidoNino: string;
  nombreNino: string;
  fechaNacimientoNino?: string;
  comoConocioProyecto?: ComoConocioProyecto;
  otroComoConocio?: string;
  diasDisponibles?: string;
}

export interface FormularioInteresResponse {
  id: number;
  fechaContacto: string;
  apellidoTutor: string;
  nombreTutor: string;
  correoTutor: string;
  telefono: string;
  apellidoNino: string;
  nombreNino: string;
  fechaNacimientoNino: string;
  edadActual: number | null;
  edadMeses: number | null;
  comoConocioProyecto: ComoConocioProyecto;
  otroComoConocio: string;
  diasDisponibles: string;
  estado: EstadoFormulario;
  activo: boolean;
  createdAt: string;
}
