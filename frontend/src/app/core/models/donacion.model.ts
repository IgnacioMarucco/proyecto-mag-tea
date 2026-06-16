export interface DonacionCreate {
  monto: number;
  donante?: string;
  correo?: string;
}

export interface DonacionInitPoint {
  initPoint: string;
}
