package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.intellij.openapi.ui.ValidationInfo

class DefaultDialogPresenter(
    private val messageDisplay: MessageDisplay = DefaultMessageDisplay(),
    private val providerTester: ProviderTester = DefaultProviderTester()
) : DialogPresenter {
    override fun showDialog(dialog: ConfigurationDialog): Boolean {
        val validationResult = validateDialog(dialog)
        if (validationResult.isNotEmpty()) {
            return false
        }
        return dialog.showAndGet()
    }

    override fun validateDialog(dialog: ConfigurationDialog): List<ValidationInfo> {
        return dialog.validateDialog()
    }

    override fun getUpdatedConfig(dialog: ConfigurationDialog): EnvProviderConfig {
        return dialog.getUpdatedConfig()
    }

    override fun testConfiguration(dialog: ConfigurationDialog) {
        val validationResult = validateDialog(dialog)
        if (validationResult.isNotEmpty()) {
            return
        }
        
        val config = getUpdatedConfig(dialog)
        providerTester.testProvider(config)
            .onSuccess {
                messageDisplay.showInfo(
                    "${config.type} token retrieved successfully",
                    "Success"
                )
            }
            .onFailure { e ->
                messageDisplay.showError(
                    "Failed to get ${config.type} token: ${e.message}",
                    "Error"
                )
            }
    }
}