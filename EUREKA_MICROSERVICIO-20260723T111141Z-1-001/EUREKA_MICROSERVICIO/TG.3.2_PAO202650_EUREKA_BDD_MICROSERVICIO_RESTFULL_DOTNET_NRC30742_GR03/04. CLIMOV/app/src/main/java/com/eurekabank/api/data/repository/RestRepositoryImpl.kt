package com.eurekabank.api.data.repository

import com.eurekabank.api.data.models.*
import com.eurekabank.api.data.network.ApiClient
import com.eurekabank.api.data.network.EnvironmentManager

class RestRepositoryImpl : IEurekaBankRepository {

    private val api = ApiClient.getRestApi()

    override suspend fun ping(): Result<String> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("ping")
        val response = api.ping(url)
        if (response.isSuccessful && response.body() != null) response.body()!!.msg
        else throw Exception("Error del servidor: ${response.code()}")
    }

    override suspend fun login(usuario: String, password: String): Result<LoginResponse> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("login")
        val response = api.login(url, LoginRequest(usuario, password))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.resultado.equals("Exitoso", ignoreCase = true)) body
            else throw Exception("Login fallido: ${body.resultado}")
        } else throw Exception(response.errorBody()?.string() ?: "Error de autenticacion")
    }

    override suspend fun deposito(cuenta: String, importe: Double): Result<OperacionCuentaResponse> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("deposito")
        val response = api.deposito(url, OperacionCuentaRequest(cuenta, importe))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.codigo == -1) throw Exception("Cuenta no encontrada o error en deposito")
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en deposito")
    }

    override suspend fun retiro(cuenta: String, importe: Double): Result<OperacionCuentaResponse> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("retiro")
        val response = api.retiro(url, OperacionCuentaRequest(cuenta, importe))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.codigo == -1) throw Exception("Cuenta no encontrada o fondos insuficientes")
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en retiro")
    }

    override suspend fun transferencia(
        cuentaOrigen: String,
        cuentaDestino: String,
        importe: Double
    ): Result<OperacionCuentaResponse> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("transferencia")
        val response = api.transferencia(url, TransferenciaRequest(cuentaOrigen, cuentaDestino, importe))
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.codigo == -1) throw Exception("Error en transferencia")
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en transferencia")
    }

    override suspend fun movimientos(cuenta: String): Result<List<Movimiento>> = runCatching {
        val baseUrl = EnvironmentManager.currentServer.getUrl("movimientos")
        val url = if (baseUrl.endsWith("/$cuenta")) baseUrl else "$baseUrl/$cuenta"
        val response = api.movimientos(url)
        if (response.isSuccessful && response.body() != null) response.body()!!
        else throw Exception(response.errorBody()?.string() ?: "Error al obtener movimientos")
    }
}
