package com.eurekabank.api.data.network

import com.eurekabank.api.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EurekaBankApi {

    @GET("resources/corebancario/ping")
    suspend fun ping(): Response<PingResponse>

    @POST("resources/corebancario/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("resources/corebancario/deposito")
    suspend fun deposito(@Body request: OperacionCuentaRequest): Response<OperacionCuentaResponse>

    @POST("resources/corebancario/retiro")
    suspend fun retiro(@Body request: OperacionCuentaRequest): Response<OperacionCuentaResponse>

    @POST("resources/corebancario/transferencia")
    suspend fun transferencia(@Body request: TransferenciaRequest): Response<OperacionCuentaResponse>

    @GET("resources/corebancario/movimientos/{cuenta}")
    suspend fun movimientos(@Path("cuenta") cuenta: String): Response<List<Movimiento>>
}
