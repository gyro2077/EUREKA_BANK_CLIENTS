package ec.edu.espe.eurekadesktop.models;

import com.google.gson.annotations.SerializedName;

public class Movimiento {
    @SerializedName("cuenta")
    private String cuenta;
    
    @SerializedName("tipo")
    private String tipo;
    
    @SerializedName("importe")
    private double importe;
    
    @SerializedName("fechaAsString")
    private String fechaAsString;
    
    @SerializedName("accion")
    private String accion;
    
    @SerializedName("nromov")
    private int nromov;

    public Movimiento() {}

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getMonto() {
        return importe;
    }

    public void setMonto(double monto) {
        this.importe = monto;
    }

    public String getFecha() {
        return fechaAsString;
    }

    public void setFecha(String fecha) {
        this.fechaAsString = fecha;
    }

    public String getDescripcion() {
        return accion != null ? accion : tipo;
    }

    public void setDescripcion(String descripcion) {
        this.accion = descripcion;
    }

    public int getNromov() {
        return nromov;
    }

    public void setNromov(int nromov) {
        this.nromov = nromov;
    }
}