import { Movimiento } from '../../models/index';
import { ConfiguracionBackend } from '../../config/backendConfig';

let credencialesActuales = { usuario: '', password: '' };

const construirEnvelope = (metodo: string, parametros: Record<string, string>, esDotNet: boolean): string => {
  let paramsXml = '';
  
  if (esDotNet) {
    for (const [clave, valor] of Object.entries(parametros)) {
      paramsXml += `<ws:${clave}>${valor}</ws:${clave}>`;
    }
    return `<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body><ws:${metodo}>${paramsXml}</ws:${metodo}></soapenv:Body>
</soapenv:Envelope>`;
  }

  for (const [clave, valor] of Object.entries(parametros)) {
    paramsXml += `<${clave}>${valor}</${clave}>`;
  }

  return `<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.monster.edu.ec/">
  <soapenv:Body>
    <ws:${metodo}>
      ${paramsXml}
    </ws:${metodo}>
  </soapenv:Body>
</soapenv:Envelope>`;
};

const realizarSolicitudSoap = async (backendActual: ConfiguracionBackend, metodo: string, parametros: Record<string, string>): Promise<string> => {
  const esDotNet = backendActual.id === 'soap-dotnet';
  const xml = construirEnvelope(metodo, parametros, esDotNet);

  console.log(`[SOAP] POST ${backendActual.urlBase}`);
  console.log('[SOAP] XML Enviado:', xml);

  const headers: Record<string, string> = {
    'Content-Type': 'text/xml; charset=utf-8'
  };

  if (esDotNet) {
    headers['SOAPAction'] = `"http://ws.monster.edu.ec/CoreBancarioWS/${metodo}"`;
  } else {
    headers['SOAPAction'] = `${backendActual.urlBase}/${metodo}`;
  }

  const respuesta = await fetch(backendActual.urlBase, {
    method: 'POST',
    headers,
    body: xml
  });

  const contenido = await respuesta.text();
  console.log('[SOAP] Respuesta:', contenido);

  return contenido;
};

export const soapService = {
  async autenticar(usuario: string, contrasena: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    try {
      credencialesActuales = { usuario, password: contrasena };
      const metodoLogin = backendActual.id === 'soap-java' ? 'login' : 'Login';
      
      const respuestaXml = await realizarSolicitudSoap(backendActual, metodoLogin, {
        usuario,
        password: contrasena  // ← El servidor espera "password", no "contrasena"
      });

      console.log('[LOGIN] Buscando indicadores de éxito en XML');

      const exitoso = 
        respuestaXml.includes('<return>1</return>') || 
        respuestaXml.includes('<return>true</return>') ||
        respuestaXml.includes('<return>Exitoso</return>') ||
        respuestaXml.includes('<return>exitoso</return>') ||
        respuestaXml.includes('<ns2:return>1</ns2:return>') ||
        respuestaXml.includes('<ns2:return>true</ns2:return>') ||
        respuestaXml.includes('<ns2:return>Exitoso</ns2:return>') ||
        respuestaXml.includes('<ns2:return>exitoso</ns2:return>') ||
        respuestaXml.includes('<LoginResult>1</LoginResult>') ||
        respuestaXml.includes('<LoginResult>true</LoginResult>') ||
        respuestaXml.includes('<LoginResult>Exitoso</LoginResult>') ||
        respuestaXml.includes('<LoginResult>exitoso</LoginResult>') ||
        respuestaXml.includes('<LoginResult>OK</LoginResult>') ||
        respuestaXml.includes('success') ||
        respuestaXml.includes('OK');

      console.log('[LOGIN] ¿Es exitoso?', exitoso);

      if (exitoso) {
        console.log('[LOGIN] ✓ Autenticación exitosa');
        return { exito: true, mensaje: 'Autenticación exitosa' };
      }
      
      console.warn('[LOGIN] ✗ No se encontraron indicadores de éxito');
      return { exito: false, mensaje: 'Credenciales inválidas' };
    } catch (error) {
      console.error('[LOGIN] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  },

  async obtenerMovimientos(cuenta: string, backendActual: ConfiguracionBackend): Promise<{ movimientos: Movimiento[]; mensaje: string }> {
    try {
      const metodo = backendActual.id === 'soap-java' ? 'obtenerMovimientos' : 'ObtenerMovimientos';
      
      const respuestaXml = await realizarSolicitudSoap(backendActual, metodo, { cuenta });
      
      console.log('[MOVIMIENTOS] Respuesta XML:', respuestaXml);
      
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(respuestaXml, 'text/xml');
      
      const movimientos: Movimiento[] = [];
      
      const esDotNet = backendActual.id === 'soap-dotnet';
      
      if (esDotNet) {
        console.log('[MOVIMIENTOS] Parser .NET - buscando diferentes formatos');
        
        const posiblesNombres = ['ObtenerMovimientosResult', 'Movimiento', 'movimiento', 'item', 'ArrayOfMovimiento'];
        let movimientosNodes: NodeListOf<Element> = xmlDoc.getElementsByTagName('ObtenerMovimientosResult');
        
        if (movimientosNodes.length === 0) {
          console.log('[MOVIMIENTOS] No encontró ObtenerMovimientosResult, probando otros tags...');
          movimientosNodes = xmlDoc.getElementsByTagName('Movimiento');
        }
        
        console.log('[MOVIMIENTOS] Tags encontrados:', movimientosNodes.length);
        
        if (movimientosNodes.length === 0) {
          console.log('[MOVIMIENTOS] Respuesta XML completa:', respuestaXml);
          const allElements = xmlDoc.getElementsByTagName('*');
          console.log('[MOVIMIENTOS] Todos los tags en el XML:', Array.from(allElements).map(e => e.tagName).join(', '));
        }
        
        for (let i = 0; i < movimientosNodes.length; i++) {
          const nodo = movimientosNodes[i];
          const cuenta = nodo.getElementsByTagName('cuenta')[0]?.textContent || nodo.getElementsByTagName('Cuenta')[0]?.textContent;
          const nromov = nodo.getElementsByTagName('nromov')[0]?.textContent || nodo.getElementsByTagName('NroMov')[0]?.textContent;
          
          if (cuenta || nromov) {
            movimientos.push({
              cuenta: cuenta || '',
              nromov: nromov || '',
              fecha: nodo.getElementsByTagName('fecha')[0]?.textContent || nodo.getElementsByTagName('Fecha')[0]?.textContent || '',
              tipo: nodo.getElementsByTagName('tipo')[0]?.textContent || nodo.getElementsByTagName('Tipo')[0]?.textContent || '',
              accion: nodo.getElementsByTagName('accion')[0]?.textContent || nodo.getElementsByTagName('Accion')[0]?.textContent || '',
              importe: nodo.getElementsByTagName('importe')[0]?.textContent || nodo.getElementsByTagName('Importe')[0]?.textContent || ''
            });
          }
        }
      } else {
        const returnNodes = xmlDoc.getElementsByTagName('return');
        console.log('[MOVIMIENTOS] Total de <return> tags:', returnNodes.length);
        
        for (let i = 0; i < returnNodes.length; i++) {
          const nodo = returnNodes[i];
          
          const cuenta = nodo.getElementsByTagName('cuenta')[0]?.textContent;
          const nromov = nodo.getElementsByTagName('nromov')[0]?.textContent;
          
          if (cuenta && nromov) {
            movimientos.push({
              cuenta: cuenta || '',
              nromov: nromov || '',
              fecha: nodo.getElementsByTagName('fecha')[0]?.textContent || '',
              tipo: nodo.getElementsByTagName('tipo')[0]?.textContent || '',
              accion: nodo.getElementsByTagName('accion')[0]?.textContent || '',
              importe: nodo.getElementsByTagName('importe')[0]?.textContent || ''
            });
          }
        }
      }
      
      console.log('[MOVIMIENTOS] Se encontraron', movimientos.length, 'movimientos');
      console.log('[MOVIMIENTOS] Movimientos:', movimientos);
      return { movimientos, mensaje: '' };
    } catch (error) {
      console.error('[MOVIMIENTOS] Error:', error);
      return { movimientos: [], mensaje: 'Error al consultar movimientos' };
    }
  },

  async registrarDeposito(cuenta: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const metodo = backendActual.id === 'soap-java' ? 'registrarDeposito' : 'RegistrarDeposito';
      
      const respuestaXml = await realizarSolicitudSoap(backendActual, metodo, {
        cuenta,
        importe
      });

      console.log('[DEPOSITO] Buscando indicadores de éxito en XML');

      const exitoso = 
          respuestaXml.includes('<return>1</return>') || 
          respuestaXml.includes('<return>true</return>') ||
          respuestaXml.includes('<return>Exitoso</return>') ||
          respuestaXml.includes('<return>exitoso</return>') ||
          respuestaXml.includes('<ns2:return>1</ns2:return>') ||
          respuestaXml.includes('<ns2:return>true</ns2:return>') ||
          respuestaXml.includes('<ns2:return>Exitoso</ns2:return>') ||
          respuestaXml.includes('<ns2:return>exitoso</ns2:return>') ||
          respuestaXml.includes('<RegistrarDepositoResult>1</RegistrarDepositoResult>') ||
          respuestaXml.includes('<RegistrarDepositoResult>true</RegistrarDepositoResult>') ||
          respuestaXml.includes('<RegistrarDepositoResult>Exitoso</RegistrarDepositoResult>') ||
          respuestaXml.includes('<Estado>1</Estado>') ||
          respuestaXml.includes('<estado>1</estado>') ||
          respuestaXml.includes('success') ||
          respuestaXml.includes('OK');

      console.log('[DEPOSITO] ¿Es exitoso?', exitoso);

      if (exitoso) {
        console.log('[DEPOSITO] ✓ Depósito exitoso');
        return { 
          exito: true, 
          mensaje: `Proceso completado, se hizo un depósito de ${importe} a la cuenta ${cuenta}` 
        };
      }
      
      console.warn('[DEPOSITO] ✗ No se encontraron indicadores de éxito');
      return { exito: false, mensaje: 'Error al procesar el depósito' };
    } catch (error) {
      console.error('[DEPOSITO] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  },

  async registrarRetiro(cuenta: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const metodo = backendActual.id === 'soap-java' ? 'registrarRetiro' : 'RegistrarRetiro';
      
      const respuestaXml = await realizarSolicitudSoap(backendActual, metodo, {
        cuenta,
        importe
      });

      console.log('[RETIRO] Buscando indicadores de éxito en XML');

      const exitoso = 
          respuestaXml.includes('<return>1</return>') || 
          respuestaXml.includes('<return>true</return>') ||
          respuestaXml.includes('<return>Exitoso</return>') ||
          respuestaXml.includes('<return>exitoso</return>') ||
          respuestaXml.includes('<ns2:return>1</ns2:return>') ||
          respuestaXml.includes('<ns2:return>true</ns2:return>') ||
          respuestaXml.includes('<ns2:return>Exitoso</ns2:return>') ||
          respuestaXml.includes('<ns2:return>exitoso</ns2:return>') ||
          respuestaXml.includes('<RegistrarRetiroResult>1</RegistrarRetiroResult>') ||
          respuestaXml.includes('<RegistrarRetiroResult>true</RegistrarRetiroResult>') ||
          respuestaXml.includes('<RegistrarRetiroResult>Exitoso</RegistrarRetiroResult>') ||
          respuestaXml.includes('<Estado>1</Estado>') ||
          respuestaXml.includes('<estado>1</estado>') ||
          respuestaXml.includes('success') ||
          respuestaXml.includes('OK');

      console.log('[RETIRO] ¿Es exitoso?', exitoso);

      if (exitoso) {
        console.log('[RETIRO] ✓ Retiro exitoso');
        return { 
          exito: true, 
          mensaje: `Proceso completado, se hizo un retiro de ${importe} de la cuenta ${cuenta}` 
        };
      }
      
      console.warn('[RETIRO] ✗ No se encontraron indicadores de éxito');
      return { exito: false, mensaje: 'Error al procesar el retiro' };
    } catch (error) {
      console.error('[RETIRO] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  },

  async registrarTransferencia(cuentaOrigen: string, cuentaDestino: string, importe: string, backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const metodo = backendActual.id === 'soap-java' ? 'registrarTransferencia' : 'RegistrarTransferencia';
      
      const respuestaXml = await realizarSolicitudSoap(backendActual, metodo, {
        cuentaOrigen,
        cuentaDestino,
        importe
      });

      console.log('[TRANSFERENCIA] Buscando indicadores de éxito en XML');

      const exitoso = 
          respuestaXml.includes('<return>1</return>') || 
          respuestaXml.includes('<return>true</return>') ||
          respuestaXml.includes('<return>Exitoso</return>') ||
          respuestaXml.includes('<return>exitoso</return>') ||
          respuestaXml.includes('<ns2:return>1</ns2:return>') ||
          respuestaXml.includes('<ns2:return>true</ns2:return>') ||
          respuestaXml.includes('<ns2:return>Exitoso</ns2:return>') ||
          respuestaXml.includes('<ns2:return>exitoso</ns2:return>') ||
          respuestaXml.includes('<RegistrarTransferenciaResult>1</RegistrarTransferenciaResult>') ||
          respuestaXml.includes('<RegistrarTransferenciaResult>true</RegistrarTransferenciaResult>') ||
          respuestaXml.includes('<RegistrarTransferenciaResult>Exitoso</RegistrarTransferenciaResult>') ||
          respuestaXml.includes('<Estado>1</Estado>') ||
          respuestaXml.includes('<estado>1</estado>') ||
          respuestaXml.includes('success') ||
          respuestaXml.includes('OK');

      console.log('[TRANSFERENCIA] ¿Es exitoso?', exitoso);

      if (exitoso) {
        console.log('[TRANSFERENCIA] ✓ Transferencia exitosa');
        return { 
          exito: true, 
          mensaje: `Proceso completado, se transfirió ${importe} de la cuenta ${cuentaOrigen} a ${cuentaDestino}` 
        };
      }
      
      console.warn('[TRANSFERENCIA] ✗ No se encontraron indicadores de éxito');
      return { exito: false, mensaje: 'Error al procesar la transferencia' };
    } catch (error) {
      console.error('[TRANSFERENCIA] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  },

  async ping(backendActual: ConfiguracionBackend): Promise<{ exito: boolean; mensaje: string }> {
    try {
      const metodo = backendActual.id === 'soap-java' ? 'ping' : 'Ping';
      
      const respuestaXml = await realizarSolicitudSoap(backendActual, metodo, {});

      console.log('[PING] Verificando respuesta');

      if (respuestaXml.includes('pong') || 
          respuestaXml.includes('Pong') ||
          respuestaXml.includes('OK') ||
          !respuestaXml.includes('Fault')) {
        console.log('[PING] ✓ Servidor disponible');
        return { exito: true, mensaje: 'Servidor disponible' };
      }
      
      console.warn('[PING] ✗ Sin respuesta del servidor');
      return { exito: false, mensaje: 'Servidor no disponible' };
    } catch (error) {
      console.error('[PING] Error:', error);
      return { exito: false, mensaje: 'Error de conexión con el servidor' };
    }
  }
};