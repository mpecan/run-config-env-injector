package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.BaseEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class EnvProviderConfigDialogTest : BasePlatformTestCase() {
    private lateinit var mockPresenter: DialogPresenter
    private lateinit var testConfig: EnvProviderConfig
    private lateinit var dialog: EnvProviderConfigDialog

    override fun setUp() {
        super.setUp()
        mockPresenter = mock<DialogPresenter>()
        testConfig = BaseEnvProviderConfig(
            environmentVariable = "TEST_VAR",
            type = "CodeArtifact",
            enabled = true,
            enabledRunConfigurations = setOf("MavenRunConfiguration")
        )
        dialog = EnvProviderConfigDialog(testConfig, mockPresenter)
    }

    fun testDialogInitialization() {
        val config = dialog.getUpdatedConfig()
        assertEquals("Environment variable should match", testConfig.environmentVariable, config.environmentVariable)
        assertEquals("Type should match", testConfig.type, config.type)
        assertEquals("Enabled state should match", testConfig.enabled, config.enabled)
        assertEquals("Run configurations should match", testConfig.enabledRunConfigurations, config.enabledRunConfigurations)
    }

    fun testRunConfigurationToggle() {
        whenever(mockPresenter.validateDialog(any())).thenReturn(emptyList())

        dialog.enabledRunConfigurations.set(dialog.enabledRunConfigurations.get() + "GradleRunConfiguration")
        val updatedConfig = dialog.getUpdatedConfig()

        assertTrue("Should have both run configurations", 
            updatedConfig.enabledRunConfigurations.containsAll(
                setOf("MavenRunConfiguration", "GradleRunConfiguration")
            )
        )

        dialog.enabledRunConfigurations.set(dialog.enabledRunConfigurations.get() -"MavenRunConfiguration")
        val finalConfig = dialog.getUpdatedConfig()

        assertTrue("Should only have Gradle configuration",
            finalConfig.enabledRunConfigurations.contains("GradleRunConfiguration")
        )
        assertFalse("Should not have Maven configuration",
            finalConfig.enabledRunConfigurations.contains("MavenRunConfiguration")
        )
    }

    fun testEnableDisableToggle() {
        whenever(mockPresenter.validateDialog(any())).thenReturn(emptyList())

        dialog.enabled.set(false)
        val updatedConfig = dialog.getUpdatedConfig()

        assertFalse("Config should be disabled", updatedConfig.enabled)

        dialog.enabled.set(true)
        val finalConfig = dialog.getUpdatedConfig()

        assertTrue("Config should be enabled", finalConfig.enabled)
    }

    fun testValidationWithEmptyEnvironmentVariable() {
        dialog.envVarField.set("")
        val validationResults = dialog.validateDialog()

        assertFalse("Should have validation errors", validationResults.isEmpty())
        assertTrue("Should contain environment variable error", 
            validationResults.any { it.message.contains("environment variable") })
    }

    fun testSequentialDialogStateChanges() {
        whenever(mockPresenter.validateDialog(any())).thenReturn(emptyList())
        
        // Change multiple properties in sequence
        dialog.envVarField.set("NEW_VAR")
        dialog.setProviderType("File")
        dialog.enabled.set(false)
        dialog.enabledRunConfigurations.set(setOf("GradleRunConfiguration"))
        
        val finalConfig = dialog.getUpdatedConfig()
        
        assertEquals("NEW_VAR", finalConfig.environmentVariable)
        assertEquals("File", finalConfig.type)
        assertFalse(finalConfig.enabled)
        assertTrue(finalConfig.enabledRunConfigurations.contains("GradleRunConfiguration"))
    }
}