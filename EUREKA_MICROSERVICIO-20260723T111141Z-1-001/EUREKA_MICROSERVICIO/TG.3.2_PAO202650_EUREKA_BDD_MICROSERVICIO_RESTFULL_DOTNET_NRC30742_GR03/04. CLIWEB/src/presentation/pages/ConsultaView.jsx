import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBackend } from '../../context/BackendContext';
import { apiService } from '../../services/apiService';
import './ConsultaView.css';

export default function ConsultaView() {
  const navigate = useNavigate();
  const { backendActual } = useBackend();
  const [cuenta, setCuenta] = useState('');
  const [movimientos, setMovimientos] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState('');

  const validarSoloNumeros = (e) => {
    if (!/[0-9]/.test(e.key) && e.key !== 'Backspace' && e.key !== 'Tab') {
      e.preventDefault();
    }
  };

  const handleCuentaChange = (e) => {
    setCuenta(e.target.value);
  };

  const handleConsultar = async () => {
    if (!cuenta.trim()) {
      setMensaje('Por favor ingrese un número de cuenta');
      return;
    }

    setCargando(true);
    setMensaje('');

    const resultado = await apiService.obtenerMovimientos(cuenta, backendActual);
    setMovimientos(resultado.movimientos);
    
    if (resultado.movimientos.length === 0) {
      setMensaje('No se encontraron movimientos para esta cuenta');
    }

    setCargando(false);
  };

  const handleSalir = () => {
    navigate('/principal');
  };

  return (
    <div className="consulta-container">
      <div className="consulta-box">
        <h1>Consulta de Movimientos</h1>

        <div className="consulta-form">
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

          <button 
            className="consulta-button" 
            onClick={handleConsultar}
            disabled={cargando}
          >
            {cargando ? 'Consultando...' : 'CONSULTAR'}
          </button>
        </div>

        {mensaje && <div className="mensaje">{mensaje}</div>}

        <div className="tabla-container">
          <table className="movimientos-table">
            <thead>
              <tr>
                <th>CUENTA</th>
                <th>NROMOV</th>
                <th>FECHA</th>
                <th>TIPO</th>
                <th>ACCIÓN</th>
                <th>IMPORTE</th>
              </tr>
            </thead>
            <tbody>
              {movimientos.length > 0 ? (
                movimientos.map((mov, index) => (
                  <tr key={index}>
                    <td>{mov.cuenta}</td>
                    <td>{mov.nromov}</td>
                    <td>{mov.fecha}</td>
                    <td>{mov.tipo}</td>
                    <td>{mov.accion}</td>
                    <td>{mov.importe}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="sin-datos">
                    {cargando ? 'Cargando...' : 'No hay datos'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <button className="salir-button" onClick={handleSalir}>
          SALIR
        </button>
      </div>
    </div>
  );
}