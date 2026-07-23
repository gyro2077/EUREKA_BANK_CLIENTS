import { createContext, useContext, useState, ReactNode } from 'react';
import { TipoBackend, ConfiguracionBackend, backends, obtenerBackendPorId } from '../config/backendConfig';

interface BackendContextType {
  backendActual: ConfiguracionBackend;
  seleccionarBackend: (id: TipoBackend) => void;
  esSoap: boolean;
}

const BackendContext = createContext<BackendContextType | undefined>(undefined);

export function BackendProvider({ children }: { children: ReactNode }) {
  const [backendId, setBackendId] = useState<TipoBackend>('soap-java');
  const backendActual = obtenerBackendPorId(backendId) || backends[0];

  const seleccionarBackend = (id: TipoBackend) => {
    setBackendId(id);
  };

  const esSoap = backendActual.protocolo === 'SOAP';

  return (
    <BackendContext.Provider value={{ backendActual, seleccionarBackend, esSoap }}>
      {children}
    </BackendContext.Provider>
  );
}

export function useBackend() {
  const contexto = useContext(BackendContext);
  if (!contexto) {
    throw new Error('useBackend debe usarse dentro de un BackendProvider');
  }
  return contexto;
}