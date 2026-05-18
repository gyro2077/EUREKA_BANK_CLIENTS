package com.eurekabank.api.data.models

data class LoginRequest(
    val usuario: String,
    val password: String
)

data class OperacionCuentaRequest(
    val cuenta: String,
    val importe: Double
)

data class TransferenciaRequest(
    val cuentaOrigen: String,
    val cuentaDestino: String,
    val importe: Double
)
