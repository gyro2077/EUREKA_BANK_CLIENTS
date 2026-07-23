package com.eurekabank.api.data.models

import com.google.gson.annotations.SerializedName

data class PingResponse(
    val msg: String
)

data class LoginResponse(
    val resultado: String
)

data class OperacionCuentaResponse(
    @SerializedName("estado")
    val codigo: Int,
    val mensaje: String = "",
    val saldo: Double
)

data class Movimiento(
    val cuenta: String,
    val nromov: Int,
    val fecha: String,
    val tipo: String,
    val accion: String,
    val importe: Double,
    val referencia: String?
)
