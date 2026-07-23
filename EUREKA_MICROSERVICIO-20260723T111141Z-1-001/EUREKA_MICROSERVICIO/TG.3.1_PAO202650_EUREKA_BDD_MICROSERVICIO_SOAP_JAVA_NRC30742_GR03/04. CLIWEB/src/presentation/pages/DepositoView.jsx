import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBackend } from '../../context/BackendContext';
import { apiService } from '../../services/apiService';
import './DepositoView.css';

export default function DepositoView() {
  const navigate = useNavigate();
  const { backendActual } = useBackend();
  const [cuenta, setCuenta] = useState('');
  const [importe, setImporte] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [tipoMensaje, setTipoMensaje] = useState('');
  const [cargando, setCargando] = useState(false);

  const validarSoloNumeros = (e) => {
    if (!/[0-9]/.test(e.key) && e.key !== 'Backspace' && e.key !== 'Tab') {
      e.preventDefault();
    }
  };

  const validarImporte = (e) => {
    const valor = e.target.value;
    const esPunto = e.key === '.';
    const esNumero = /[0-9]/.test(e.key);
    const tienePunto = valor.includes('.');
    
    if (!esNumero && !esPunto && e.key !== 'Backspace' && e.key !== 'Tab') {
      e.preventDefault();
    }
    
    if (esPunto && tienePunto) {
      e.preventDefault();
    }
  };

  const handleCuentaChange = (e) => {
    setCuenta(e.target.value);
  };

  const handleImporteChange = (e) => {
    setImporte(e.target.value);
  };

  const handleProcesar = async () => {
    if (!cuenta.trim()) {
      setMensaje('Por favor ingrese un número de cuenta');
      setTipoMensaje('error');
      return;
    }

    if (!importe.trim() || parseFloat(importe) <= 0) {
      setMensaje('Por favor ingrese un importe válido');
      setTipoMensaje('error');
      return;
    }

    setCargando(true);
    setMensaje('');

    const resultado = await apiService.registrarDeposito(cuenta, importe, backendActual);

    if (resultado.exito) {
      setMensaje(resultado.mensaje);
      setTipoMensaje('exito');
      setCuenta('');
      setImporte('');
    } else {
      setMensaje(resultado.mensaje);
      setTipoMensaje('error');
    }

    setCargando(false);
  };

  const handleSalir = () => {
    navigate('/principal');
  };

  return (
    <div className="deposito-container">
      <div className="deposito-box">
        <h1>Depósito</h1>

        <div className="deposito-form">
          <div className="form-group">
            <label htmlFor="cuenta">CUENTA</label>
            <input
              type="text"
              id="cuenta"
              value={cuenta}
              onChange={handleCuentaChange}
              onKeyDown={validarSoloNumeros}
              placeholder="Ingrese número de cuenta"
              maxLength={20}
            />
          </div>

          <div className="form-group">
            <label htmlFor="importe">IMPORTE</label>
            <input
              type="text"
              id="importe"
              value={importe}
              onChange={handleImporteChange}
              onKeyDown={validarImporte}
              placeholder="Ej: 100.50"
            />
          </div>
        </div>

        {mensaje && (
          <div className={`mensaje ${tipoMensaje}`}>
            {mensaje}
          </div>
        )}

        <div className="deposito-buttons">
          <button 
            className="procesar-button" 
            onClick={handleProcesar}
            disabled={cargando}
          >
            {cargando ? 'Procesando...' : 'PROCESAR'}
          </button>
          
          <button className="salir-button" onClick={handleSalir}>
            SALIR
          </button>
        </div>
      </div>
    </div>
  );
}