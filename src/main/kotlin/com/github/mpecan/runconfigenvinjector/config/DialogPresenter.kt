package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.intellij.openapi.ui.ValidationInfo

interface DialogPresenter {
    fun showDialog(dialog: ConfigurationDialog): Boolean
    fun validateDialog(dialog: ConfigurationDialog): List<ValidationInfo>
    fun getUpdatedConfig(dialog: ConfigurationDialog): EnvProviderConfig
    fun testConfiguration(dialog: ConfigurationDialog)
}