package com.eurekabank.api.data.repository

import com.eurekabank.api.data.models.LoginResponse
import com.eurekabank.api.data.models.Movimiento
import com.eurekabank.api.data.models.OperacionCuentaResponse
import com.eurekabank.api.data.network.ApiClient
import com.eurekabank.api.data.network.EnvironmentManager
import com.eurekabank.api.data.network.SoapEnvelopeBuilder

class SoapRepositoryImpl : IEurekaBankRepository {

    private val soapApi = ApiClient.getSoapApi()
    private val isDotNet: Boolean get() = EnvironmentManager.currentServer.isDotNet

    override suspend fun ping(): Result<String> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("ping")
        val envelope = SoapEnvelopeBuilder.buildPing(isDotNet)
        val response = soapApi.executeSoapRequest(url, envelope)
        if (response.isSuccessful && response.body() != null) {
            SoapEnvelopeBuilder.parsePingResponse(response.body()!!)
        } else throw Exception("Error del servidor: ${response.code()}")
    }

    override suspend fun login(usuario: String, password: String): Result<LoginResponse> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("login")
        val envelope = SoapEnvelopeBuilder.buildLogin(usuario, password, isDotNet)
        val response = soapApi.executeSoapRequest(url, envelope)
        if (response.isSuccessful && response.body() != null) {
            val body = SoapEnvelopeBuilder.parseLoginResponse(response.body()!!)
            if (body.resultado.equals("Exitoso", ignoreCase = true)) body
            else throw Exception("Login fallido: ${body.resultado}")
        } else throw Exception(response.errorBody()?.string() ?: "Error de autenticacion")
    }

    override suspend fun deposito(cuenta: String, importe: Double): Result<OperacionCuentaResponse> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("cuentas")
        val envelope = SoapEnvelopeBuilder.buildDeposito(cuenta, importe, isDotNet)
        val response = soapApi.executeSoapRequest(url, envelope)
        if (response.isSuccessful && response.body() != null) {
            val body = SoapEnvelopeBuilder.parseOperacionResponse(response.body()!!)
            if (body.codigo == -1) throw Exception(body.mensaje)
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en deposito")
    }

    override suspend fun retiro(cuenta: String, importe: Double): Result<OperacionCuentaResponse> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("cuentas")
        val envelope = SoapEnvelopeBuilder.buildRetiro(cuenta, importe, isDotNet)
        val response = soapApi.executeSoapRequest(url, envelope)
        if (response.isSuccessful && response.body() != null) {
            val body = SoapEnvelopeBuilder.parseOperacionResponse(response.body()!!)
            if (body.codigo == -1) throw Exception(body.mensaje)
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en retiro")
    }

    override suspend fun transferencia(
        cuentaOrigen: String,
        cuentaDestino: String,
        importe: Double
    ): Result<OperacionCuentaResponse> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("transferencia")
        val envelope = SoapEnvelopeBuilder.buildTransferencia(cuentaOrigen, cuentaDestino, importe, isDotNet)
        val response = soapApi.executeSoapRequest(url, envelope)
        if (response.isSuccessful && response.body() != null) {
            val body = SoapEnvelopeBuilder.parseOperacionResponse(response.body()!!)
            if (body.codigo == -1) throw Exception(body.mensaje)
            body
        } else throw Exception(response.errorBody()?.string() ?: "Error en transferencia")
    }

    override suspend fun movimientos(cuenta: String): Result<List<Movimiento>> = runCatching {
        val url = EnvironmentManager.currentServer.getUrl("movimientos")
        val envelope = SoapEnvelopeBuilder.buildMovimientos(cuenta, isDotNet)
        val response = soapApi.executeSoapRequest(url, envelope)
        if (response.isSuccessful && response.body() != null) {
            SoapEnvelopeBuilder.parseMovimientosResponse(response.body()!!)
        } else throw Exception(response.errorBody()?.string() ?: "Error al obtener movimientos")
    }
}
