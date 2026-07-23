package ec.edu.monster.modelo;

public class OperacionCuentaRequest {
    private String cuenta;
    private double importe;

    public OperacionCuentaRequest() {
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }
}
