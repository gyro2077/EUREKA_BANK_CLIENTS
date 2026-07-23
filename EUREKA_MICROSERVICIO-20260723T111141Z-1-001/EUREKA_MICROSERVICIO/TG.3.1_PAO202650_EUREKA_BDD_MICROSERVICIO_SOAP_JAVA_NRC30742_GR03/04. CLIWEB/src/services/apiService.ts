import { soapService } from './soap/soapService';
import { restService } from './rest/restService';
import { Movimiento } from '../models/index';
import { ConfiguracionBackend, getMicroserviceUrl } from '../config/backendConfig';

export const apiService = {
  async autenticar(usuario: string, contrasena: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    const url = getMicroserviceUrl(backendActual.id, 'login');
    if (backendActual.protocolo === 'SOAP') {
      return soapService.autenticar(usuario, contrasena, backendActual, url);
    }
    return restService.autenticar(usuario, contrasena, url);
  },

  async registrarDeposito(cuenta: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    const url = getMicroserviceUrl(backendActual.id, 'cuentas');
    if (backendActual.protocolo === 'SOAP') {
      return soapService.registrarDeposito(cuenta, importe, backendActual, url);
    }
    return restService.registrarDeposito(cuenta, parseFloat(importe), url);
  },

  async registrarRetiro(cuenta: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    const url = getMicroserviceUrl(backendActual.id, 'cuentas');
    if (backendActual.protocolo === 'SOAP') {
      return soapService.registrarRetiro(cuenta, importe, backendActual, url);
    }
    return restService.registrarRetiro(cuenta, parseFloat(importe), url);
  },

  async registrarTransferencia(cuentaOrigen: string, cuentaDestino: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    const url = getMicroserviceUrl(backendActual.id, 'transferencia');
    if (backendActual.protocolo === 'SOAP') {
      return soapService.registrarTransferencia(cuentaOrigen, cuentaDestino, importe, backendActual, url);
    }
    return restService.registrarTransferencia(cuentaOrigen, cuentaDestino, parseFloat(importe), url);
  },

  async obtenerMovimientos(cuenta: string, backendActual: ConfiguracionBackend): Promise<{ movimientos: Movimiento[]; mensaje: string }> {
    const url = getMicroserviceUrl(backendActual.id, 'movimientos');
    if (backendActual.protocolo === 'SOAP') {
      return soapService.obtenerMovimientos(cuenta, backendActual, url);
    }
    return restService.obtenerMovimientos(cuenta, url);
  },

  async ping(backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    const url = getMicroserviceUrl(backendActual.id, 'ping');
    if (backendActual.protocolo === 'SOAP') {
      return soapService.ping(backendActual, url);
    }
    return restService.ping(url);
  }
};