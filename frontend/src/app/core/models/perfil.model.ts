export interface PerfilUpdate {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
}

export interface CambiarPasswordRequest {
  passwordActual: string;
  passwordNueva: string;
}
