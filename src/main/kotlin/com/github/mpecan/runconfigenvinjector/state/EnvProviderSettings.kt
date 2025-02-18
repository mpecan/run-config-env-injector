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

class EnvProviderState: BaseState() {
    var configurations by list<EnvProviderConfig>()
}

