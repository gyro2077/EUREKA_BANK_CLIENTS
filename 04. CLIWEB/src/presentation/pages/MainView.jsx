import { useNavigate } from 'react-router-dom';
import { useBackend } from '../../context/BackendContext';
import './MainView.css';

export default function MainView() {
  const navigate = useNavigate();
  const { backendActual } = useBackend();

  const handleDeposito = () => {
    navigate('/deposito');
  };

  const handleConsulta = () => {
    navigate('/consulta');
  };

  const handleTransferencia = () => {
    navigate('/transferencia');
  };

  const handleSalir = () => {
    navigate('/');
  };

  return (
    <div className="main-container">
      <div className="main-content">
        <div className="main-header">
          <h1>UNIVERSIDAD DE LAS FUERZAS ARMADAS</h1>
          <h2>GRUPO 3</h2>
          <p className="backend-info">{backendActual.nombre}</p>
        </div>

        <div className="main-buttons">
          <button className="main-button deposito" onClick={handleDeposito}>
            <span className="button-icon">💰</span>
            DEPÓSITO
          </button>
          
          <button className="main-button consulta" onClick={handleConsulta}>
            <span className="button-icon">📋</span>
            CONSULTA
          </button>
          
          <button className="main-button transferencia" onClick={handleTransferencia}>
            <span className="button-icon">⇆</span>
            TRANSFERENCIA
          </button>
          
          <button className="main-button salir" onClick={handleSalir}>
            <span className="button-icon">🚪</span>
            SALIR
          </button>
        </div>

        <div className="main-logo">
          <div className="logo-circle">
            <span>ESPE</span>
          </div>
        </div>
      </div>
    </div>
  );
}