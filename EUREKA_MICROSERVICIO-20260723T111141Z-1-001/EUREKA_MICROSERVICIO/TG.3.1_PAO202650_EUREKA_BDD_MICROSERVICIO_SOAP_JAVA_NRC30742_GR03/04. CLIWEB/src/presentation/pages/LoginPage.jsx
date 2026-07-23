import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBackend } from '../../context/BackendContext';
import { backends } from '../../config/backendConfig';
import { apiService } from '../../services/apiService';
import './LoginPage.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const { backendActual, seleccionarBackend } = useBackend();
  const [usuario, setUsuario] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  const iniciarSesion = async (e) => {
    e.preventDefault();
    setError('');
    setCargando(true);

    const resultado = await apiService.autenticar(usuario, contrasena, backendActual);

    if (resultado.exito) {
      navigate('/principal');
    } else {
      setError(resultado.mensaje);
    }

    setCargando(false);
  };

  const manejarCambioBackend = (e) => {
    seleccionarBackend(e.target.value);
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>{backendActual.titulo}</h1>
          <p>Universidad de las Fuerzas Armadas</p>
        </div>

        <div className="form-group">
          <label htmlFor="backend">Backend</label>
          <select
            id="backend"
            value={backendActual.id}
            onChange={manejarCambioBackend}
            className="backend-select"
          >
            {backends.map((backend) => (
              <option key={backend.id} value={backend.id}>
                {backend.nombre}
              </option>
            ))}
          </select>
        </div>
        
        <form onSubmit={iniciarSesion} className="login-form">
          <div className="form-group">
            <label htmlFor="usuario">Usuario</label>
            <input
              type="text"
              id="usuario"
              value={usuario}
              onChange={(e) => setUsuario(e.target.value)}
              placeholder="Ingrese su usuario"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="contrasena">Contraseña</label>
            <input
              type="password"
              id="contrasena"
              value={contrasena}
              onChange={(e) => setContrasena(e.target.value)}
              placeholder="Ingrese su contraseña"
              required
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className="login-button" disabled={cargando}>
            {cargando ? 'Iniciando...' : 'Iniciar Sesión'}
          </button>
        </form>
      </div>
    </div>
  );
}