package com.eurekabank.api.data.repository

import com.eurekabank.api.data.models.*
import com.eurekabank.api.data.network.ApiClient

class EurekaBankRepository {

    private val api = ApiClient.api

    suspend fun ping(): Result<String> = runCatching {
        val response = api.ping()
        if (response.isSuccessful && response.body() != null) response.body()!!.msg
        else throw Exception("Error del servidor: ${response.code()}")
    }

    suspend fun login(usuario: String, password: String): Result<LoginResponse> = runCatching {
        val response = api.login(LoginRequest(usuario, password))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.resultado.equals("Exitoso", ignoreCase = true)) body
            else throw Exception("Login fallido: ${body.resultado}")
        } else throw Exception(response.errorBody()?.string() ?: "Error de autenticación")
    }

    suspend fun deposito(cuenta: String, importe: Double): Result<OperacionCuentaResponse> = runCatching {
        val response = api.deposito(OperacionCuentaRequest(cuenta, importe))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.estado == -1) throw Exception("Cuenta no encontrada o error en depósito")
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en depósito")
    }

    suspend fun retiro(cuenta: String, importe: Double): Result<OperacionCuentaResponse> = runCatching {
        val response = api.retiro(OperacionCuentaRequest(cuenta, importe))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.estado == -1) throw Exception("Cuenta no encontrada o fondos insuficientes")
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en retiro")
    }

    suspend fun transferencia(
        cuentaOrigen: String,
        cuentaDestino: String,
        importe: Double
    ): Result<OperacionCuentaResponse> = runCatching {
        val response = api.transferencia(TransferenciaRequest(cuentaOrigen, cuentaDestino, importe))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.estado == -1) throw Exception("Error en transferencia")
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en transferencia")
    }

    suspend fun movimientos(cuenta: String): Result<List<Movimiento>> = runCatching {
        val response = api.movimientos(cuenta)
        if (response.isSuccessful && response.body() != null) response.body()!!
        else throw Exception(response.errorBody()?.string() ?: "Error al obtener movimientos")
    }
}
