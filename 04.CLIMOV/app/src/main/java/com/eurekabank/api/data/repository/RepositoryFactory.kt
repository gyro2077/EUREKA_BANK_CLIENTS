package com.eurekabank.api.data.repository

import com.eurekabank.api.data.network.EnvironmentManager

object RepositoryFactory {
    fun getRepository(): IEurekaBankRepository {
        return if (EnvironmentManager.currentServer.isSoap) SoapRepositoryImpl() else RestRepositoryImpl()
    }
}
