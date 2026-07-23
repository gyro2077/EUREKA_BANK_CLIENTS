package ec.edu.monster.ws;

import ec.edu.monster.modelo.Movimiento;
import ec.edu.monster.modelo.OperacionCuentaResponse;
import ec.edu.monster.servicio.EurekaService;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import java.util.List;

@WebService(serviceName = "CoreBancarioWS")
public class CoreBancarioWS {

    private final EurekaService service = new EurekaService();

    @WebMethod(operationName = "login")
    public String login(@WebParam(name = "usuario") String usuario,
                        @WebParam(name = "password") String password) {
        boolean ok = service.validarIngreso(usuario, password);
        return ok ? "Exitoso" : "Denegado";
    }

    @WebMethod(operationName = "registrarDeposito")
    public OperacionCuentaResponse registrarDeposito(@WebParam(name = "cuenta") String cuenta,
                                                     @WebParam(name = "importe") double importe) throws Exception {
        String codEmp = "0001";
        OperacionCuentaResponse resp = new OperacionCuentaResponse();
        try {
            double saldo = service.registrarDeposito(cuenta, importe, codEmp);
            resp.setEstado(1);
            resp.setSaldo(saldo);
        } catch (Exception e) {
            resp.setEstado(-1);
            resp.setSaldo(-1);
            throw new Exception("Error en deposito: " + e.getMessage());
        }
        return resp;
    }

    @WebMethod(operationName = "registrarRetiro")
    public OperacionCuentaResponse registrarRetiro(@WebParam(name = "cuenta") String cuenta,
                                                   @WebParam(name = "importe") double importe) throws Exception {
        String codEmp = "0004";
        OperacionCuentaResponse resp = new OperacionCuentaResponse();
        try {
            double saldo = service.registrarRetiro(cuenta, importe, codEmp);
            resp.setEstado(1);
            resp.setSaldo(saldo);
        } catch (Exception e) {
            resp.setEstado(-1);
            resp.setSaldo(-1);
            throw new Exception("Error en retiro: " + e.getMessage());
        }
        return resp;
    }

    @WebMethod(operationName = "registrarTransferencia")
    public OperacionCuentaResponse registrarTransferencia(@WebParam(name = "cuentaOrigen") String cuentaOrigen,
                                                          @WebParam(name = "cuentaDestino") String cuentaDestino,
                                                          @WebParam(name = "importe") double importe) throws Exception {
        String codEmp = "0004";
        OperacionCuentaResponse resp = new OperacionCuentaResponse();
        try {
            double saldoOrigen = service.registrarTransferencia(cuentaOrigen, cuentaDestino, importe, codEmp);
            resp.setEstado(1);
            resp.setSaldo(saldoOrigen);
        } catch (Exception e) {
            resp.setEstado(-1);
            resp.setSaldo(-1);
            throw new Exception("Error en transferencia: " + e.getMessage());
        }
        return resp;
    }

    @WebMethod(operationName = "obtenerMovimientos")
    public List<Movimiento> obtenerMovimientos(@WebParam(name = "cuenta") String cuenta) throws Exception {
        try {
            return service.leerMovimientos(cuenta);
        } catch (Exception e) {
            throw new Exception("Error al obtener movimientos: " + e.getMessage());
        }
    }

    @WebMethod(operationName = "ping")
    public String ping() {
        return "ok";
    }
}
