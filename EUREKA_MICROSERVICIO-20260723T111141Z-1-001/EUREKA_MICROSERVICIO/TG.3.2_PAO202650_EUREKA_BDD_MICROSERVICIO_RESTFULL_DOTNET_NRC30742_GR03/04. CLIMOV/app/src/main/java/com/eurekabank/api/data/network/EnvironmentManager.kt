package com.eurekabank.api.data.network

enum class HostType(val label: String, val baseUrl: String) {
    NUBE("Nube (209.145.48.25)", "http://209.145.48.25"),
    LOCAL("Localhost (10.0.2.2 / 127.0.0.1)", "http://10.0.2.2")
}

enum class ServerType(
    val label: String,
    val isSoap: Boolean,
    val isDotNet: Boolean,
    val isMicroservice: Boolean,
    val ports: Map<String, Int> = emptyMap(),
    val port: Int = 8090,
    val soapPath: String = ""
) {
    DOTNET_REST(
        "REST .NET (Microservicios)",
        isSoap = false,
        isDotNet = true,
        isMicroservice = true,
        ports = mapOf("login" to 8093, "cuentas" to 8097, "movimientos" to 8098, "transferencia" to 8099, "ping" to 8093)
    ),
    JAVA_SOAP(
        "SOAP Java (Microservicios)",
        isSoap = true,
        isDotNet = false,
        isMicroservice = true,
        ports = mapOf("login" to 8091, "cuentas" to 8094, "movimientos" to 8095, "transferencia" to 8096, "ping" to 8091),
        soapPath = "ROOT/CoreBancarioWS"
    ),
    JAVA_REST(
        "REST Java (Monolito)",
        isSoap = false,
        isDotNet = false,
        isMicroservice = false,
        port = 8090
    ),
    DOTNET_SOAP(
        "SOAP .NET (Monolito)",
        isSoap = true,
        isDotNet = true,
        isMicroservice = false,
        port = 8092,
        soapPath = "CoreBancarioWS"
    );

    fun getUrl(service: String): String {
        val cleanHost = EnvironmentManager.currentHost.baseUrl.removeSuffix("/")
        return if (isMicroservice) {
            val p = ports[service] ?: 8093
            if (isSoap) {
                "$cleanHost:$p/$soapPath"
            } else {
                when (service) {
                    "login" -> "$cleanHost:$p/resources/corebancario/login"
                    "deposito" -> "$cleanHost:$p/resources/corebancario/deposito"
                    "retiro" -> "$cleanHost:$p/resources/corebancario/retiro"
                    "movimientos" -> "$cleanHost:$p/resources/corebancario/movimientos"
                    "transferencia" -> "$cleanHost:$p/resources/corebancario/transferencia"
                    "ping" -> "$cleanHost:$p/resources/corebancario/ping"
                    else -> "$cleanHost:$p/resources/corebancario"
                }
            }
        } else {
            if (isSoap) {
                "$cleanHost:$port/$soapPath"
            } else {
                "$cleanHost:$port/resources/corebancario"
            }
        }
    }
}

object EnvironmentManager {
    var currentHost: HostType = HostType.NUBE
    var currentServer: ServerType = ServerType.DOTNET_REST
}
