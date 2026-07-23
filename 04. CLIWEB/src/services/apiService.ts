import { soapService } from './soap/soapService';
import { restService } from './rest/restService';
import { Movimiento } from '../models/index';
import { ConfiguracionBackend } from '../config/backendConfig';

export const apiService = {
  async autenticar(usuario: string, contrasena: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    if (backendActual.protocolo === 'SOAP') {
      return soapService.autenticar(usuario, contrasena, backendActual);
    }
    return restService.autenticar(usuario, contrasena, backendActual.urlBase);
  },

  async registrarDeposito(cuenta: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    if (backendActual.protocolo === 'SOAP') {
      return soapService.registrarDeposito(cuenta, importe, backendActual);
    }
    return restService.registrarDeposito(cuenta, parseFloat(importe), backendActual.urlBase);
  },

  async registrarRetiro(cuenta: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    if (backendActual.protocolo === 'SOAP') {
      return soapService.registrarRetiro(cuenta, importe, backendActual);
    }
    return restService.registrarRetiro(cuenta, parseFloat(importe), backendActual.urlBase);
  },

  async registrarTransferencia(cuentaOrigen: string, cuentaDestino: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    if (backendActual.protocolo === 'SOAP') {
      return soapService.registrarTransferencia(cuentaOrigen, cuentaDestino, importe, backendActual);
    }
    return restService.registrarTransferencia(cuentaOrigen, cuentaDestino, parseFloat(importe), backendActual.urlBase);
  },

  async obtenerMovimientos(cuenta: string, backendActual: ConfiguracionBackend): Promise<{ movimientos: Movimiento[]; mensaje: string }> {
    if (backendActual.protocolo === 'SOAP') {
      return soapService.obtenerMovimientos(cuenta, backendActual);
    }
    return restService.obtenerMovimientos(cuenta, backendActual.urlBase);
  },

  async ping(backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    if (backendActual.protocolo === 'SOAP') {
      return soapService.ping(backendActual);
    }
    return restService.ping(backendActual.urlBase);
  }
};