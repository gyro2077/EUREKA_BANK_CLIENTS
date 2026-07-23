package ec.edu.espe.eurekadesktop.models;

public enum Backend {
    REST_DOTNET("REST .NET (Microservicios)", "rest.dotnet.url"),
    SOAP_JAVA("SOAP Java (Microservicios)", "soap.java.url"),
    REST_JAVA("REST Java (Monolito)", "rest.java.url"),
    SOAP_DOTNET("SOAP .NET (Monolito)", "soap.dotnet.url");

    private final String displayName;
    private final String configKey;

    Backend(String displayName, String configKey) {
        this.displayName = displayName;
        this.configKey = configKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public boolean isSoap() {
        return this == SOAP_JAVA || this == SOAP_DOTNET;
    }

    public boolean isRest() {
        return this == REST_JAVA || this == REST_DOTNET;
    }

    public boolean isJava() {
        return this == SOAP_JAVA || this == REST_JAVA;
    }

    public boolean isDotNet() {
        return this == SOAP_DOTNET || this == REST_DOTNET;
    }

    @Override
    public String toString() {
        return displayName;
    }
}