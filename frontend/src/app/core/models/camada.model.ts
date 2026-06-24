export interface CamadaListItem {
  id: number;
  nombre: string;
  fechaNacimiento: string | null;
  activo: boolean;
}

export interface CamadaResponse extends CamadaListItem {
  createdAt: string;
}

export interface CamadaCreate {
  nombre: string;
  fechaNacimiento: string;
}

export interface CamadaUpdate extends CamadaCreate {}
