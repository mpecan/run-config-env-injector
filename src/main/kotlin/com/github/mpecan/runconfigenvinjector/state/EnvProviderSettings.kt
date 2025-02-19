package com.github.mpecan.runconfigenvinjector.state

import com.intellij.openapi.components.*


@State(
    name = "EnvInjectorSettings",
    storages = [Storage("envInjector.xml")]
)
class EnvProviderSettings : SimplePersistentStateComponent<EnvProviderState>(EnvProviderState()) {
    companion object {
        fun getInstance() = service<EnvProviderSettings>()
    }
}

class StoredConfiguration : BaseState() {
    var environmentVariable by string()
    var enabled by property(true)
    var enabledRunConfigurations by list<String>()
    var type by string()
    var additionalSettings by map<String, String>()
}

class EnvProviderState : BaseState() {
    var configurations by list<StoredConfiguration>()

    fun setToStoredConfigurations(configs: List<EnvProviderConfig>) {
        configurations = configs.map { it.toStoredConfiguration() }.toMutableList()
    }

    fun getFromStoredConfigurations() = configurations.mapNotNull {
        when (it?.type) {
            "CodeArtifact" -> CodeArtifactConfig.fromStoredConfiguration(it)
            "File" -> FileEnvProviderConfig.fromStoredConfiguration(it)
            "StructuredFile" -> StructuredFileEnvProviderConfig.fromStoredConfiguration(it)
            else -> null
        }
    }
}

