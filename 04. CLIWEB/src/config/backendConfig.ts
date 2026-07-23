export type TipoBackend = 'soap-java' | 'rest-java' | 'soap-dotnet' | 'rest-dotnet';

export interface ConfiguracionBackend {
  id: TipoBackend;
  nombre: string;
  titulo: string;
  protocolo: 'SOAP' | 'REST';
  tecnologia: 'Java' | '.NET';
  urlBase: string;
  endpoints: {
    login: string;
    deposito: string;
    movimientos: string;
    ping?: string;
  };
}

// Detectar si estamos en desarrollo o producción
const isProduction = import.meta.env.PROD;
const baseUrlSoapJava = isProduction ? 'http://209.145.48.25:8091' : '/api/soap-java';
const baseUrlRestJava = isProduction ? 'http://209.145.48.25:8090' : '/api/rest-java';
const baseUrlSoapDotnet = isProduction ? 'http://209.145.48.25:8092' : '/api/soap-dotnet';
const baseUrlRestDotnet = isProduction ? 'http://209.145.48.25:8093' : '/api/rest-dotnet';

export const backends: ConfiguracionBackend[] = [
  {
    id: 'soap-java',
    nombre: 'SOAP Java',
    titulo: 'EUREKA BANK - SOAP Java',
    protocolo: 'SOAP',
    tecnologia: 'Java',
    urlBase: `${baseUrlSoapJava}/ROOT/CoreBancarioWS`,
    endpoints: {
      login: 'login',
      deposito: 'registrarDeposito',
      movimientos: 'obtenerMovimientos'
    }
  },
  {
    id: 'rest-java',
    nombre: 'REST Java',
    titulo: 'EUREKA BANK - REST Java',
    protocolo: 'REST',
    tecnologia: 'Java',
    urlBase: `${baseUrlRestJava}/resources/corebancario`,
    endpoints: {
      login: '/login',
      deposito: '/deposito',
      movimientos: '/movimientos',
      ping: '/ping'
    }
  },
  {
    id: 'soap-dotnet',
    nombre: 'SOAP .NET',
    titulo: 'EUREKA BANK - SOAP .NET',
    protocolo: 'SOAP',
    tecnologia: '.NET',
    urlBase: `${baseUrlSoapDotnet}/CoreBancarioWS`,
    endpoints: {
      login: 'Login',
      deposito: 'RegistrarDeposito',
      movimientos: 'ObtenerMovimientos'
    }
  },
  {
    id: 'rest-dotnet',
    nombre: 'REST .NET',
    titulo: 'EUREKA BANK - REST .NET',
    protocolo: 'REST',
    tecnologia: '.NET',
    urlBase: `${baseUrlRestDotnet}/resources/corebancario`,
    endpoints: {
      login: '/login',
      deposito: '/deposito',
      movimientos: '/movimientos',
      ping: '/ping'
    }
  }
];

export const obtenerBackendPorId = (id: TipoBackend): ConfiguracionBackend | undefined => {
  return backends.find(b => b.id === id);
};