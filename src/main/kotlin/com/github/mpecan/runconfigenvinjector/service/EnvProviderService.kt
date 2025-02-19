package com.github.mpecan.runconfigenvinjector.service

import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderSettings
import com.intellij.openapi.components.Service

@Service
class EnvProviderService {

    fun getEnabledConfigurations(): List<EnvProviderConfig> {
        return EnvProviderSettings.getInstance().state.getFromStoredConfigurations().filter { it.enabled }
    }
}
