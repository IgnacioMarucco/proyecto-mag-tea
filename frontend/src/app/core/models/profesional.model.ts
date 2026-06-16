export type Role =
  | 'SECRETARIA'
  | 'CUERPO_TECNICO'
  | 'CUERPO_MEDICO'
  | 'ROTANTE_CLINICA'
  | 'ROTANTE_BASICA'
  | 'INVESTIGADOR_PRINCIPAL';

export const ROLE_LABELS: Record<Role, string> = {
  SECRETARIA: 'Secretaría',
  CUERPO_TECNICO: 'Cuerpo Técnico',
  CUERPO_MEDICO: 'Cuerpo Médico',
  ROTANTE_CLINICA: 'Rotante Clínica',
  ROTANTE_BASICA: 'Rotante Básica',
  INVESTIGADOR_PRINCIPAL: 'Investigador Principal',
};

export const ROLES = Object.keys(ROLE_LABELS) as Role[];

export interface ProfesionalResponse {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string | null;
  role: Role;
  activo: boolean;
  createdAt: string;
}

export interface ProfesionalCreate {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  password: string;
  role: Role;
}

export interface ProfesionalUpdate {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  role: Role;
}
