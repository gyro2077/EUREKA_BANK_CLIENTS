package com.eurekabank.api.data.repository

import com.eurekabank.api.data.models.LoginResponse
import com.eurekabank.api.data.models.Movimiento
import com.eurekabank.api.data.models.OperacionCuentaResponse

interface IEurekaBankRepository {
    suspend fun ping(): Result<String>
    suspend fun login(usuario: String, password: String): Result<LoginResponse>
    suspend fun deposito(cuenta: String, importe: Double): Result<OperacionCuentaResponse>
    suspend fun retiro(cuenta: String, importe: Double): Result<OperacionCuentaResponse>
    suspend fun transferencia(cuentaOrigen: String, cuentaDestino: String, importe: Double): Result<OperacionCuentaResponse>
    suspend fun movimientos(cuenta: String): Result<List<Movimiento>>
}
