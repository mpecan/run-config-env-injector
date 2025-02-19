package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.BaseEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.FileEnvProviderConfig
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DefaultProviderTesterTest : BasePlatformTestCase() {
    private lateinit var providerTester: DefaultProviderTester
    private lateinit var testConfig: EnvProviderConfig

    override fun setUp() {
        super.setUp()
        providerTester = DefaultProviderTester()
        testConfig = BaseEnvProviderConfig(
            environmentVariable = "TEST_VAR",
            type = "Invalid", // Use invalid type to test error handling
            enabled = true,
            enabledRunConfigurations = setOf()
        )
    }

    fun testInvalidProviderHandling() {
        val result = providerTester.testProvider(testConfig)
        
        assertTrue("Should return failure for invalid provider", result.isFailure)
        assertNotNull("Should have exception message", result.exceptionOrNull()?.message)
    }

    fun testValidProviderHandling() {
        val testFile = myFixture.tempDirFixture.createFile("test.txt")
        ApplicationManager.getApplication().runWriteAction {
            testFile.setBinaryContent("test content".toByteArray())
        }
        val validConfig = FileEnvProviderConfig(
            environmentVariable = "TEST_VAR",
            enabled = true,
            enabledRunConfigurations = setOf(),
            filePath = testFile.canonicalPath!!
        )
        
        val result = providerTester.testProvider(validConfig)
        
        assertTrue("Should return success for valid provider", result.isSuccess)
    }
}