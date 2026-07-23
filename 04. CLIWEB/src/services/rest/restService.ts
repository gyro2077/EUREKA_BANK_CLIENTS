import { Movimiento } from '../../models/index';

let tokenAutenticacion = '';

const realizarSolicitud = async <T>(url: string, endpoint: string, datos?: object): Promise<T> => {
  const urlCompleta = `${url}${endpoint}`;

  const opciones: RequestInit = {
    method: datos ? 'POST' : 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Agregar token si existe (para requests autenticadas)
  if (tokenAutenticacion) {
    opciones.headers = {
      ...opciones.headers,
      'Authorization': `Bearer ${tokenAutenticacion}`
    };
  }

  if (datos) {
    opciones.body = JSON.stringify(datos);
  }

  console.log(`[REST] ${opciones.method} ${urlCompleta}`, datos);

  const respuesta = await fetch(urlCompleta, opciones);
  const contenido = await respuesta.text();
  
  console.log(`[REST] Respuesta (${respuesta.status}):`, contenido);

  try {
    const jsonParse = JSON.parse(contenido);
    // Si es JSON válido, devolverlo sin importar el status HTTP
    // (algunos servidores devuelven 500 pero con datos)
    return jsonParse;
  } catch (e) {
    // Si no es JSON, verificar si el status está bien
    if (!respuesta.ok) {
      throw new Error(`Error ${respuesta.status}: ${respuesta.statusText}`);
    }
    // Si el status está bien pero no es JSON, devolverlo como está
    return contenido as any;
  }
};

export const restService = {
  async autenticar(usuario: string, contrasena: string, urlBase: string): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const respuesta = await realizarSolicitud<any>(urlBase, '/login', {
        usuario,
        password: contrasena
      });

      console.log('[LOGIN] Respuesta completa:', respuesta);

      // Guardar token si existe
      if (respuesta?.token) {
        tokenAutenticacion = respuesta.token;
        console.log('[LOGIN] Token almacenado');
      }

      // Validar de múltiples formas para soportar diferentes servidores
      const exitoso = 
        respuesta?.token ||  // Si tiene token
        respuesta?.mensaje === 'OK' ||  // Si dice OK
        respuesta?.success === true ||  // Si tiene campo success
        respuesta?.exito === true ||  // Si tiene campo exito
        respuesta?.resultado?.toLowerCase() === 'exitoso' ||  // Si resultado es "Exitoso"
        respuesta?.resultado?.toLowerCase() === 'ok' ||  // Si resultado es "OK"
        (respuesta && typeof respuesta === 'string' && respuesta.toLowerCase() === 'ok') ||  // Si es string OK
        respuesta?.id ||  // Si tiene ID de usuario
        respuesta?.usuario;  // Si devuelve datos del usuario

      if (exitoso) {
        console.log('[LOGIN] ✓ Autenticación exitosa');
        return { exito: true, mensaje: 'Autenticación exitosa' };
      }

      console.warn('[LOGIN] No se encontraron indicadores de éxito en:', respuesta);
      return { exito: false, mensaje: 'Credenciales inválidas' };
    } catch (error) {
      console.error('[LOGIN] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  },

  async registrarDeposito(cuenta: string, importe: number, urlBase: string): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const datosDeposito = {
        cuenta,
        importe
      };
      
      console.log('[DEPOSITO] Datos a enviar:', datosDeposito);
      console.log('[DEPOSITO] URL base:', urlBase);
      console.log('[DEPOSITO] Token actual:', tokenAutenticacion ? 'Sí existe' : 'No existe');
      
      const respuesta = await realizarSolicitud<any>(urlBase, '/deposito', datosDeposito);

      console.log('[DEPOSITO] Respuesta completa:', respuesta);

      if (respuesta?.estado === 1) {
        return {
          exito: true,
          mensaje: `Proceso completado, se hizo un depósito de ${importe} a la cuenta ${cuenta}`
        };
      }

      if (respuesta?.estado === -1) {
        console.warn('[DEPOSITO] Backend rechazó la operación:', respuesta);
        return { exito: false, mensaje: 'Error al procesar el depósito (cuenta no existe o datos inválidos)' };
      }
      
      console.warn('[DEPOSITO] Respuesta inesperada:', respuesta);
      return { exito: false, mensaje: 'Error al procesar el depósito' };
    } catch (error) {
      console.error('[DEPOSITO] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  },

  async obtenerMovimientos(cuenta: string, urlBase: string): Promise<{ movimientos: Movimiento[]; mensaje: string }> {
    try {
      const respuesta = await realizarSolicitud<any>(urlBase, `/movimientos/${cuenta}`);
      
      console.log('[MOVIMIENTOS] Respuesta completa:', respuesta);

      let movimientos: Movimiento[] = [];
      
      if (Array.isArray(respuesta)) {
        movimientos = respuesta;
      } else {
        movimientos = respuesta?.movimientos || respuesta?.data || respuesta?.items || [];
      }
      
      console.log('[MOVIMIENTOS] Total movimientos encontrados:', movimientos.length);
      
      return { movimientos, mensaje: '' };
    } catch (error) {
      console.error('[MOVIMIENTOS] Error:', error);
      return { movimientos: [], mensaje: 'Error al consultar movimientos' };
    }
  },

  async registrarRetiro(cuenta: string, importe: number, urlBase: string): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const respuesta = await realizarSolicitud<any>(urlBase, '/retiro', {
        cuenta,
        importe
      });

      console.log('[RETIRO] Respuesta completa:', respuesta);

      if (respuesta?.estado === 1 || respuesta?.resultado === 1 || respuesta?.mensaje?.includes('éxito') || respuesta?.success === true || respuesta?.exito === true) {
        return {
          exito: true,
          mensaje: `Proceso completado, se hizo un retiro de ${importe} de la cuenta ${cuenta}`
        };
      }
      return { exito: false, mensaje: 'Error al procesar el retiro' };
    } catch (error) {
      console.error('[RETIRO] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  },

  async registrarTransferencia(cuentaOrigen: string, cuentaDestino: string, importe: number, urlBase: string): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const respuesta = await realizarSolicitud<any>(urlBase, '/transferencia', {
        cuentaOrigen,
        cuentaDestino,
        importe
      });

      console.log('[TRANSFERENCIA] Respuesta completa:', respuesta);

      if (respuesta?.estado === 1 || respuesta?.resultado === 1 || respuesta?.mensaje?.includes('éxito') || respuesta?.success === true || respuesta?.exito === true) {
        return {
          exito: true,
          mensaje: `Proceso completado, se transfirió ${importe} de la cuenta ${cuentaOrigen} a ${cuentaDestino}`
        };
      }
      return { exito: false, mensaje: 'Error al procesar la transferencia' };
    } catch (error) {
      console.error('[TRANSFERENCIA] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  },

  async ping(urlBase: string): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const respuesta = await realizarSolicitud<any>(urlBase, '/ping');

      console.log('[PING] Respuesta completa:', respuesta);

      return { exito: true, mensaje: 'Servidor disponible' };
    } catch (error) {
      console.error('[PING] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  }
};