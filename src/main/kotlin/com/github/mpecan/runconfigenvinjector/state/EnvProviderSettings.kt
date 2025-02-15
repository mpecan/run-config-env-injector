package com.github.mpecan.runconfigenvinjector.state

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service


@State(
    name = "EnvInjectorSettings",
    storages = [Storage("envInjectorSettings.xml")]
)
class EnvProviderSettings : PersistentStateComponent<EnvProviderSettings> {
    var configurations: MutableList<EnvProviderConfig> = mutableListOf()

    override fun getState() = this

    override fun loadState(state: EnvProviderSettings) {
        configurations = state.configurations
    }

    companion object {
        fun getInstance() = service<EnvProviderSettings>()
    }
}