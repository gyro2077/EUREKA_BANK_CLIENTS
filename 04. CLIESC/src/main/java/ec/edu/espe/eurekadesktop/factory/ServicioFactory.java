package ec.edu.espe.eurekadesktop.factory;

import ec.edu.espe.eurekadesktop.models.Backend;
import ec.edu.espe.eurekadesktop.services.interfaces.ServicioBancario;
import ec.edu.espe.eurekadesktop.services.soap.ServicioSoapJava;
import ec.edu.espe.eurekadesktop.services.soap.ServicioSoapDotNet;
import ec.edu.espe.eurekadesktop.services.rest.ServicioRestJava;
import ec.edu.espe.eurekadesktop.services.rest.ServicioRestDotNet;
import java.util.Properties;

public class ServicioFactory {
    private static Properties config;

    public static void setConfig(Properties config) {
        ServicioFactory.config = config;
    }

    public static ServicioBancario crearServicio(Backend backend) {
        if (config == null) {
            throw new IllegalStateException("Configuración no inicializada");
        }

        switch (backend) {
            case SOAP_JAVA:
                return new ServicioSoapJava(config);
            case SOAP_DOTNET:
                return new ServicioSoapDotNet(config);
            case REST_JAVA:
                return new ServicioRestJava(config);
            case REST_DOTNET:
                return new ServicioRestDotNet(config);
            default:
                throw new UnsupportedOperationException("Backend no implementado: " + backend);
        }
    }
}