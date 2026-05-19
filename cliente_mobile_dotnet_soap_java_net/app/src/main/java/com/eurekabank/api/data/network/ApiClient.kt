package com.eurekabank.api.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getRestApi(): EurekaBankApi = Retrofit.Builder()
        .baseUrl(EnvironmentManager.currentServer.baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(EurekaBankApi::class.java)

    fun getSoapApi(): EurekaBankSoapApi = Retrofit.Builder()
        .baseUrl(EnvironmentManager.currentServer.baseUrl)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(EurekaBankSoapApi::class.java)

    fun getSoapEndpointUrl(): String =
        EnvironmentManager.currentServer.baseUrl + EnvironmentManager.currentServer.soapPath
}
