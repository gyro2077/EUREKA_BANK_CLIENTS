package com.eurekabank.api.data.network

enum class ServerType(
    val label: String,
    val baseUrl: String,
    val soapPath: String,
    val isSoap: Boolean,
    val isDotNet: Boolean
) {
    JAVA_REST("Java REST (8090)", "http://209.145.48.25:8090/", "", false, false),
    JAVA_SOAP("Java SOAP (8091)", "http://209.145.48.25:8091/", "ROOT/CoreBancarioWS", true, false),
    DOTNET_SOAP(".NET SOAP (8092)", "http://209.145.48.25:8092/", "CoreBancarioWS", true, true),
    DOTNET_REST(".NET REST (8093)", "http://209.145.48.25:8093/", "", false, true)
}

object EnvironmentManager {
    var currentServer: ServerType = ServerType.JAVA_REST
}
