export interface CajaListItem {
  id: number;
  freezer: string;
  cajon: number;
  numero: number;
  activo: boolean;
}

export interface CajaResponse extends CajaListItem {
  createdAt: string;
}

export interface CajaCreate {
  freezer: string;
  cajon: number;
  numero: number;
}

export interface CajaUpdate extends CajaCreate {}

export interface CajaOcupacion {
  ocupadas: string[];
}
