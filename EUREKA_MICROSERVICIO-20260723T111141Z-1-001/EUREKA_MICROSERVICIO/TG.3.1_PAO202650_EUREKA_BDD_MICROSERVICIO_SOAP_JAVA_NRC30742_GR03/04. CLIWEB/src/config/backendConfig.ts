export type TipoBackend = 'rest-dotnet' | 'soap-java' | 'rest-java' | 'soap-dotnet';

export const CLOUD_HOST_BASE = 'http://209.145.48.25';

export interface ConfiguracionBackend {
  id: TipoBackend;
  nombre: string;
  titulo: string;
  protocolo: 'SOAP' | 'REST';
  tecnologia: 'Java' | '.NET';
}

export const backends: ConfiguracionBackend[] = [
  {
    id: 'rest-dotnet',
    nombre: 'REST .NET (Microservicios)',
    titulo: 'EUREKA BANK - REST .NET Microservicios',
    protocolo: 'REST',
    tecnologia: '.NET'
  },
  {
    id: 'soap-java',
    nombre: 'SOAP Java (Microservicios)',
    titulo: 'EUREKA BANK - SOAP Java Microservicios',
    protocolo: 'SOAP',
    tecnologia: 'Java'
  },
  {
    id: 'rest-java',
    nombre: 'REST Java (Monolito)',
    titulo: 'EUREKA BANK - REST Java Monolito',
    protocolo: 'REST',
    tecnologia: 'Java'
  },
  {
    id: 'soap-dotnet',
    nombre: 'SOAP .NET (Monolito)',
    titulo: 'EUREKA BANK - SOAP .NET Monolito',
    protocolo: 'SOAP',
    tecnologia: '.NET'
  }
];

export const obtenerBackendPorId = (id: TipoBackend): ConfiguracionBackend => {
  return backends.find(b => b.id === id) || backends[0];
};

export const getMicroserviceUrl = (
  backendId: TipoBackend,
  service: 'login' | 'cuentas' | 'movimientos' | 'transferencia' | 'ping'
): string => {
  const isDev = import.meta.env.DEV;

  if (backendId === 'rest-dotnet') {
    switch (service) {
      case 'login':
        return isDev ? '/api/rest-dotnet-login/resources/corebancario/login' : `${CLOUD_HOST_BASE}:8093/resources/corebancario/login`;
      case 'cuentas':
        return isDev ? '/api/rest-dotnet-cuentas/resources/corebancario' : `${CLOUD_HOST_BASE}:8097/resources/corebancario`;
      case 'movimientos':
        return isDev ? '/api/rest-dotnet-movimientos/resources/corebancario' : `${CLOUD_HOST_BASE}:8098/resources/corebancario`;
      case 'transferencia':
        return isDev ? '/api/rest-dotnet-transferencias/resources/corebancario/transferencia' : `${CLOUD_HOST_BASE}:8099/resources/corebancario/transferencia`;
      case 'ping':
        return isDev ? '/api/rest-dotnet-login/resources/corebancario/ping' : `${CLOUD_HOST_BASE}:8093/resources/corebancario/ping`;
    }
  }

  if (backendId === 'soap-java') {
    switch (service) {
      case 'login':
        return isDev ? '/api/soap-java-login/ROOT/CoreBancarioWS' : `${CLOUD_HOST_BASE}:8091/ROOT/CoreBancarioWS`;
      case 'cuentas':
        return isDev ? '/api/soap-java-cuentas/ROOT/CoreBancarioWS' : `${CLOUD_HOST_BASE}:8094/ROOT/CoreBancarioWS`;
      case 'movimientos':
        return isDev ? '/api/soap-java-movimientos/ROOT/CoreBancarioWS' : `${CLOUD_HOST_BASE}:8095/ROOT/CoreBancarioWS`;
      case 'transferencia':
        return isDev ? '/api/soap-java-transferencias/ROOT/CoreBancarioWS' : `${CLOUD_HOST_BASE}:8096/ROOT/CoreBancarioWS`;
      case 'ping':
        return isDev ? '/api/soap-java-login/ROOT/CoreBancarioWS' : `${CLOUD_HOST_BASE}:8091/ROOT/CoreBancarioWS`;
    }
  }

  if (backendId === 'rest-java') {
    return isDev ? '/api/rest-java/resources/corebancario' : `${CLOUD_HOST_BASE}:8090/resources/corebancario`;
  }

  if (backendId === 'soap-dotnet') {
    return isDev ? '/api/soap-dotnet/CoreBancarioWS' : `${CLOUD_HOST_BASE}:8092/CoreBancarioWS`;
  }

  return isDev ? '/api/rest-dotnet-login/resources/corebancario' : `${CLOUD_HOST_BASE}:8093/resources/corebancario`;
};