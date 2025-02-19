package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig

class DefaultEnvProviderDialogFactory : EnvProviderDialogFactory {
    override fun createDialog(config: EnvProviderConfig): ConfigurationDialog {
        return EnvProviderConfigDialog(config)
    }
}