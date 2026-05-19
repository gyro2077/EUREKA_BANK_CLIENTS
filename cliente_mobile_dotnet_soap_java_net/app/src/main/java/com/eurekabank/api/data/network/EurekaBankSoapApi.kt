package com.eurekabank.api.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

interface EurekaBankSoapApi {
    @Headers("Content-Type: text/xml")
    @POST
    suspend fun executeSoapRequest(@Url url: String, @Body soapEnvelope: String): Response<String>
}
