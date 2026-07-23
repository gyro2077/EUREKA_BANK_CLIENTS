import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBackend } from '../../context/BackendContext';
import { apiService } from '../../services/apiService';
import './TransferenciaView.css';

export default function TransferenciaView() {
  const navigate = useNavigate();
  const { backendActual } = useBackend();
  const [cuentaOrigen, setCuentaOrigen] = useState('');
  const [cuentaDestino, setCuentaDestino] = useState('');
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

  const handleCuentaOrigenChange = (e) => {
    setCuentaOrigen(e.target.value);
  };

  const handleCuentaDestinoChange = (e) => {
    setCuentaDestino(e.target.value);
  };

  const handleImporteChange = (e) => {
    setImporte(e.target.value);
  };

  const handleProcesar = async () => {
    if (!cuentaOrigen.trim()) {
      setMensaje('Por favor ingrese la cuenta origen');
      setTipoMensaje('error');
      return;
    }

    if (!cuentaDestino.trim()) {
      setMensaje('Por favor ingrese la cuenta destino');
      setTipoMensaje('error');
      return;
    }

    if (cuentaOrigen === cuentaDestino) {
      setMensaje('La cuenta origen y destino no pueden ser iguales');
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

    const resultado = await apiService.registrarTransferencia(cuentaOrigen, cuentaDestino, importe, backendActual);

    if (resultado.exito) {
      setMensaje(resultado.mensaje);
      setTipoMensaje('exito');
      setCuentaOrigen('');
      setCuentaDestino('');
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
    <div className="transferencia-container">
      <div className="transferencia-box">
        <h1>Transferencia</h1>

        <div className="transferencia-form">
          <div className="form-group">
            <label htmlFor="cuentaOrigen">CUENTA ORIGEN</label>
            <input
              type="text"
              id="cuentaOrigen"
              value={cuentaOrigen}
              onChange={handleCuentaOrigenChange}
              onKeyDown={validarSoloNumeros}
              placeholder="Ingrese cuenta origen"
              maxLength={20}
            />
          </div>

          <div className="form-group">
            <label htmlFor="cuentaDestino">CUENTA DESTINO</label>
            <input
              type="text"
              id="cuentaDestino"
              value={cuentaDestino}
              onChange={handleCuentaDestinoChange}
              onKeyDown={validarSoloNumeros}
              placeholder="Ingrese cuenta destino"
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

        <div className="transferencia-buttons">
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