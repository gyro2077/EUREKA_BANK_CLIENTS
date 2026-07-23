package ec.edu.espe.eurekadesktop.context;

import ec.edu.espe.eurekadesktop.models.Usuario;
import ec.edu.espe.eurekadesktop.services.interfaces.ServicioBancario;

public class ContextoBackend {
    private static ContextoBackend instance;
    private Usuario usuario;
    private ServicioBancario servicio;

    private ContextoBackend() {}

    public static void setInstance(Usuario usuario, ServicioBancario servicio) {
        if (instance == null) {
            instance = new ContextoBackend();
        }
        instance.usuario = usuario;
        instance.servicio = servicio;
    }

    public static ContextoBackend getInstance() {
        return instance;
    }

    public static void limpiar() {
        instance = null;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public ServicioBancario getServicio() {
        return servicio;
    }
}