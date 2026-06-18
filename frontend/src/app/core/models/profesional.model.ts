export type Role =
  | 'CUERPO_MEDICO'
  | 'CUERPO_TECNICO'
  | 'INVESTIGADOR_PRINCIPAL';

export const ROLE_LABELS: Record<Role, string> = {
  CUERPO_MEDICO:          'Cuerpo Médico',
  CUERPO_TECNICO:         'Cuerpo Técnico',
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
