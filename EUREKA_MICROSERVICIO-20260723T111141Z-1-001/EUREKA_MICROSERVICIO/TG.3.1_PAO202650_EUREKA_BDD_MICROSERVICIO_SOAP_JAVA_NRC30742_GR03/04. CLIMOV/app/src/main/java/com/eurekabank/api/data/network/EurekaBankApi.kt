package com.eurekabank.api.data.network

import com.eurekabank.api.data.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface EurekaBankApi {

    @GET
    suspend fun ping(@Url url: String): Response<PingResponse>

    @POST
    suspend fun login(@Url url: String, @Body request: LoginRequest): Response<LoginResponse>

    @POST
    suspend fun deposito(@Url url: String, @Body request: OperacionCuentaRequest): Response<OperacionCuentaResponse>

    @POST
    suspend fun retiro(@Url url: String, @Body request: OperacionCuentaRequest): Response<OperacionCuentaResponse>

    @POST
    suspend fun transferencia(@Url url: String, @Body request: TransferenciaRequest): Response<OperacionCuentaResponse>

    @GET
    suspend fun movimientos(@Url url: String): Response<List<Movimiento>>
}
