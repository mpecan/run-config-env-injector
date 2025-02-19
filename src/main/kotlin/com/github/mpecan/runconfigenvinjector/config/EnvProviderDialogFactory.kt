package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig

interface EnvProviderDialogFactory {
    fun createDialog(config: EnvProviderConfig): ConfigurationDialog
}