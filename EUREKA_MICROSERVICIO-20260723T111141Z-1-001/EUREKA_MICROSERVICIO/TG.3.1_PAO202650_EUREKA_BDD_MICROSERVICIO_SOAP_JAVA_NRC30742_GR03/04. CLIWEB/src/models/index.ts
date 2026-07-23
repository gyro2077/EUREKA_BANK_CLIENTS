export interface Movimiento {
  cuenta: string;
  nromov: string;
  fecha: string;
  tipo: string;
  accion: string;
  importe: string;
}

export interface ResultadoDeposito {
  exito: boolean;
  mensaje: string;
}

export interface CredencialesLogin {
  usuario: string;
  contrasena: string;
}

export interface ResultadoLogin {
  exito: boolean;
  mensaje: string;
  token?: string;
}