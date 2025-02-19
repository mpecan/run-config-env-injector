package com.github.mpecan.runconfigenvinjector.state

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EnvProviderSettingsTest : BasePlatformTestCase() {
    private lateinit var settings: EnvProviderSettings
    
    override fun setUp() {
        super.setUp()
        settings = EnvProviderSettings.getInstance()
    }
    
    fun testStoreAndRetrieveCodeArtifactConfig() {
        val config = CodeArtifactConfig(
            environmentVariable = "AWS_TOKEN",
            enabled = true,
            enabledRunConfigurations = setOf("MavenRunConfiguration", "GradleRunConfiguration"),
            region = "us-east-1",
            domain = "test-domain",
            domainOwner = "123456789"
        )
        
        settings.state.setToStoredConfigurations(listOf(config))
        val retrievedConfigs = settings.state.getFromStoredConfigurations()
        
        assertEquals(1, retrievedConfigs.size)
        val retrievedConfig = retrievedConfigs.first() as CodeArtifactConfig
        assertEquals(config.environmentVariable, retrievedConfig.environmentVariable)
        assertEquals(config.enabled, retrievedConfig.enabled)
        assertEquals(config.enabledRunConfigurations, retrievedConfig.enabledRunConfigurations)
        assertEquals(config.region, retrievedConfig.region)
        assertEquals(config.domain, retrievedConfig.domain)
        assertEquals(config.domainOwner, retrievedConfig.domainOwner)
    }
    
    fun testStoreAndRetrieveFileConfig() {
        val config = FileEnvProviderConfig(
            environmentVariable = "API_KEY",
            enabled = true,
            enabledRunConfigurations = setOf("SpringBootRunConfiguration"),
            filePath = "/path/to/secrets.txt"
        )
        
        settings.state.setToStoredConfigurations(listOf(config))
        val retrievedConfigs = settings.state.getFromStoredConfigurations()
        
        assertEquals(1, retrievedConfigs.size)
        val retrievedConfig = retrievedConfigs.first() as FileEnvProviderConfig
        assertEquals(config.environmentVariable, retrievedConfig.environmentVariable)
        assertEquals(config.enabled, retrievedConfig.enabled)
        assertEquals(config.enabledRunConfigurations, retrievedConfig.enabledRunConfigurations)
        assertEquals(config.filePath, retrievedConfig.filePath)
    }
    
    fun testStoreAndRetrieveStructuredFileConfig() {
        val config = StructuredFileEnvProviderConfig(
            environmentVariable = "DATABASE_URL",
            enabled = true,
            enabledRunConfigurations = setOf("DockerRunConfiguration"),
            filePath = "/path/to/config.yaml"
        )
        
        settings.state.setToStoredConfigurations(listOf(config))
        val retrievedConfigs = settings.state.getFromStoredConfigurations()
        
        assertEquals(1, retrievedConfigs.size)
        val retrievedConfig = retrievedConfigs.first() as StructuredFileEnvProviderConfig
        assertEquals(config.environmentVariable, retrievedConfig.environmentVariable)
        assertEquals(config.enabled, retrievedConfig.enabled)
        assertEquals(config.enabledRunConfigurations, retrievedConfig.enabledRunConfigurations)
        assertEquals(config.filePath, retrievedConfig.filePath)
    }
    
    fun testStoreAndRetrieveMultipleConfigs() {
        val configs = listOf(
            CodeArtifactConfig(
                environmentVariable = "AWS_TOKEN",
                enabled = true,
                enabledRunConfigurations = setOf("MavenRunConfiguration"),
                region = "us-east-1",
                domain = "test-domain",
                domainOwner = "123456789"
            ),
            FileEnvProviderConfig(
                environmentVariable = "API_KEY",
                enabled = false,
                enabledRunConfigurations = setOf("SpringBootRunConfiguration"),
                filePath = "/path/to/secrets.txt"
            ),
            StructuredFileEnvProviderConfig(
                environmentVariable = "DATABASE_URL",
                enabled = true,
                enabledRunConfigurations = setOf("DockerRunConfiguration"),
                filePath = "/path/to/config.yaml"
            )
        )
        
        settings.state.setToStoredConfigurations(configs)
        val retrievedConfigs = settings.state.getFromStoredConfigurations()
        
        assertEquals(3, retrievedConfigs.size)
        assertTrue(retrievedConfigs[0] is CodeArtifactConfig)
        assertTrue(retrievedConfigs[1] is FileEnvProviderConfig)
        assertTrue(retrievedConfigs[2] is StructuredFileEnvProviderConfig)
        
        // Verify each config maintains its specific type and properties
        retrievedConfigs.zip(configs).forEach { (retrieved, original) ->
            assertEquals(original.environmentVariable, retrieved.environmentVariable)
            assertEquals(original.enabled, retrieved.enabled)
            assertEquals(original.enabledRunConfigurations, retrieved.enabledRunConfigurations)
            assertEquals(original.type, retrieved.type)
        }
    }
}