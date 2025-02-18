package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.*
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.kotlin.*

class DialogInteractionTest : BasePlatformTestCase() {
    private lateinit var mockDialog: ConfigurationDialog
    private lateinit var config: EnvProviderConfig

    override fun setUp() {
        super.setUp()
        mockDialog = mock()
        config = BaseEnvProviderConfig(
            environmentVariable = "TEST_VAR",
            type = "CodeArtifact",
            enabled = true,
            enabledRunConfigurations = setOf()
        )
        EnvProviderSettings.getInstance().state.configurations = mutableListOf(config)
    }

    fun testDialogValidationFailureFlow() {
        val validationError = ValidationInfo("Test error")
        whenever(mockDialog.validateDialog()).thenReturn(listOf(validationError))
        whenever(mockDialog.showAndGet()).thenReturn(false)

        val table = EnvProviderConfigTable(object : EnvProviderDialogFactory {
            override fun createDialog(config: EnvProviderConfig) = mockDialog
        })

        table.addNewConfig()

        verify(mockDialog).showAndGet()
        verify(mockDialog, never()).getUpdatedConfig()
    }

    fun testDialogTypeChangeInteraction() {
        whenever(mockDialog.showAndGet()).thenReturn(true)
        whenever(mockDialog.getUpdatedConfig()).thenReturn(
            FileEnvProviderConfig(
                environmentVariable = "UPDATED_TEST_VAR",
                enabled = true,
                enabledRunConfigurations = setOf()
            )
        )

        val table = EnvProviderConfigTable(object : EnvProviderDialogFactory {
            override fun createDialog(config: EnvProviderConfig) = mockDialog
        })

        table.selectionModel.setSelectionInterval(0,0)
        table.editSelectedRow()

        val inOrder = inOrder(mockDialog)
        inOrder.verify(mockDialog).showAndGet()
        inOrder.verify(mockDialog).getUpdatedConfig()
    }

    fun testDialogCancellation() {
        whenever(mockDialog.showAndGet()).thenReturn(false)

        val table = EnvProviderConfigTable(object : EnvProviderDialogFactory {
            override fun createDialog(config: EnvProviderConfig) = mockDialog
        })

        table.addNewConfig()

        verify(mockDialog).showAndGet()
        verify(mockDialog, never()).getUpdatedConfig()
        verify(mockDialog, never()).close(any())
    }

    fun testProviderTypeChangeSequence() {
        whenever(mockDialog.showAndGet()).thenReturn(true)
        whenever(mockDialog.getUpdatedConfig()).thenReturn(config)
        val table = EnvProviderConfigTable(object : EnvProviderDialogFactory {
            override fun createDialog(config: EnvProviderConfig) = mockDialog
        })

        table.selectionModel.setSelectionInterval(0,0)
        table.editSelectedRow()

        val inOrder = inOrder(mockDialog)
        inOrder.verify(mockDialog).showAndGet()
        inOrder.verify(mockDialog).getUpdatedConfig()
    }

    fun testMultipleEditsInteraction() {
        whenever(mockDialog.showAndGet()).thenReturn(true)
        var currentConfig = config

        whenever(mockDialog.getUpdatedConfig()).thenAnswer {
            currentConfig = currentConfig.copy(
                environmentVariable = "UPDATED_${currentConfig.environmentVariable}"
            )
            currentConfig
        }

        val table = EnvProviderConfigTable(object : EnvProviderDialogFactory {
            override fun createDialog(config: EnvProviderConfig) = mockDialog
        })

        table.selectionModel.setSelectionInterval(0,0)
        // Simulate multiple edits
        repeat(3) {
            table.editSelectedRow()
        }

        verify(mockDialog, times(3)).showAndGet()
        verify(mockDialog, times(3)).getUpdatedConfig()
    }
}