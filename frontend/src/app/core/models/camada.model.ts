export interface CamadaListItem {
  id: number;
  nombre: string;
  activo: boolean;
}

export interface CamadaResponse extends CamadaListItem {
  createdAt: string;
}

export interface CamadaCreate {
  nombre: string;
}

export interface CamadaUpdate extends CamadaCreate {}
