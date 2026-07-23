import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { BackendProvider } from './context/BackendContext';
import LoginPage from './presentation/pages/LoginPage';
import MainView from './presentation/pages/MainView';
import ConsultaView from './presentation/pages/ConsultaView';
import DepositoView from './presentation/pages/DepositoView';
import TransferenciaView from './presentation/pages/TransferenciaView';

function App() {
  return (
    <BackendProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/principal" element={<MainView />} />
          <Route path="/consulta" element={<ConsultaView />} />
          <Route path="/deposito" element={<DepositoView />} />
          <Route path="/transferencia" element={<TransferenciaView />} />
        </Routes>
      </BrowserRouter>
    </BackendProvider>
  );
}

export default App;