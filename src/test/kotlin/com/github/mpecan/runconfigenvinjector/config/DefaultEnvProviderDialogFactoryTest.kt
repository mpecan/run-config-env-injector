package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.BaseEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DefaultEnvProviderDialogFactoryTest : BasePlatformTestCase() {
    private lateinit var factory: DefaultEnvProviderDialogFactory
    private lateinit var testConfig: EnvProviderConfig

    override fun setUp() {
        super.setUp()
        factory = DefaultEnvProviderDialogFactory()
        testConfig = BaseEnvProviderConfig(
            environmentVariable = "TEST_VAR",
            type = "CodeArtifact",
            enabled = true,
            enabledRunConfigurations = setOf()
        )
    }

    fun testCreatesRealDialogInstance() {
        val dialog = factory.createDialog(testConfig)
        assertTrue("Should create EnvProviderConfigDialog instance", 
            dialog is EnvProviderConfigDialog)
    }

    fun testDialogInitializedWithConfig() {
        val dialog = factory.createDialog(testConfig)
        val updatedConfig = dialog.getUpdatedConfig()
        
        assertEquals("Environment variable should match", 
            testConfig.environmentVariable, updatedConfig.environmentVariable)
        assertEquals("Type should match", 
            testConfig.type, updatedConfig.type)
        assertEquals("Enabled state should match", 
            testConfig.enabled, updatedConfig.enabled)
        assertEquals("Default run configurations should be present",
            setOf("MavenRunConfiguration", "GradleRunConfiguration"), updatedConfig.enabledRunConfigurations)
    }

    fun testDialogValidationWithRealInstance() {
        val dialog = factory.createDialog(testConfig.copy(environmentVariable = ""))
        val validationResult = dialog.validateDialog()
        
        assertFalse("Should have validation errors for empty environment variable", 
            validationResult.isEmpty())
    }
}