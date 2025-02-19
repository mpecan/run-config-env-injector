package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.service.EnvProviderFactory
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig

class DefaultProviderTester : ProviderTester {
    override fun testProvider(config: EnvProviderConfig): Result<Unit> {
        return try {
            EnvProviderFactory.createProvider(config).getValue()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}