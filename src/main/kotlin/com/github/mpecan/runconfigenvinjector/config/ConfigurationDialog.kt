package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.intellij.openapi.ui.ValidationInfo

interface ConfigurationDialog {
    fun show()
    fun showAndGet(): Boolean
    fun getUpdatedConfig(): EnvProviderConfig
    fun validateDialog(): List<ValidationInfo>
    fun close(exitCode: Int)
    fun setProviderType(providerType: String)
}