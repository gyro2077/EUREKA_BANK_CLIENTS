package ec.edu.espe.eurekadesktop.models;

public class Deposito {
    private String cuenta;
    private double importe;
    private String resultado;
    private boolean exitoso;
    private int estado;
    private double saldo;

    public Deposito() {}

    public Deposito(String cuenta, double importe) {
        this.cuenta = cuenta;
        this.importe = importe;
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

    public String getResultado() {
        return resultado != null ? resultado : (estado == 1 ? "Depósito exitoso" : "Error");
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public boolean isExitoso() {
        return exitoso || estado == 1;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }
    
    public int getEstado() {
        return estado;
    }
    
    public void setEstado(int estado) {
        this.estado = estado;
    }
    
    public double getSaldo() {
        return saldo;
    }
    
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}