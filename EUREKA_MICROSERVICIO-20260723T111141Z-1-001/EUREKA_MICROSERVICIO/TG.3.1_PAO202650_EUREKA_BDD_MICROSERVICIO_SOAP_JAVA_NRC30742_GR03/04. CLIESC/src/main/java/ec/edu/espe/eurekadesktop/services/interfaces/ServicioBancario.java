package ec.edu.espe.eurekadesktop.services.interfaces;

import ec.edu.espe.eurekadesktop.models.Backend;
import ec.edu.espe.eurekadesktop.models.Deposito;
import ec.edu.espe.eurekadesktop.models.Movimiento;
import ec.edu.espe.eurekadesktop.models.Usuario;
import java.util.List;

public interface ServicioBancario {
    Usuario login(String username, String password) throws Exception;
    List<Movimiento> obtenerMovimientos(String token, String cuenta) throws Exception;
    Deposito registrarDeposito(String token, String cuenta, double importe) throws Exception;
    String transferencia(String token, String cuentaOrigen, String cuentaDestino, double importe) throws Exception;
    String getEndpoint();
    Backend getBackend();
}