package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.BaseEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.kotlin.*

class DefaultDialogPresenterTest : BasePlatformTestCase() {
    private lateinit var mockDialog: ConfigurationDialog
    private lateinit var mockMessageDisplay: MessageDisplay
    private lateinit var mockProviderTester: ProviderTester
    private lateinit var testConfig: EnvProviderConfig
    private lateinit var presenter: DefaultDialogPresenter

    override fun setUp() {
        super.setUp()
        mockDialog = mock()
        mockMessageDisplay = mock()
        mockProviderTester = mock()
        testConfig = BaseEnvProviderConfig(
            environmentVariable = "TEST_VAR",
            type = "CodeArtifact",
            enabled = true,
            enabledRunConfigurations = setOf()
        )
        presenter = DefaultDialogPresenter(mockMessageDisplay, mockProviderTester)
    }

    fun testShowDialogWithValidationFailure() {
        val validationError = ValidationInfo("Test error")
        whenever(mockDialog.validateDialog()).thenReturn(listOf(validationError))
        
        val result = presenter.showDialog(mockDialog)
        
        assertFalse("Dialog should not show when validation fails", result)
        verify(mockDialog, never()).showAndGet()
    }

    fun testShowDialogWithValidationSuccess() {
        whenever(mockDialog.validateDialog()).thenReturn(emptyList())
        whenever(mockDialog.showAndGet()).thenReturn(true)
        
        val result = presenter.showDialog(mockDialog)
        
        assertTrue("Dialog should show when validation passes", result)
        verify(mockDialog).showAndGet()
    }

    fun testTestConfigurationSuccess() {
        whenever(mockDialog.validateDialog()).thenReturn(emptyList())
        whenever(mockDialog.getUpdatedConfig()).thenReturn(testConfig)
        whenever(mockProviderTester.testProvider(any())).thenReturn(Result.success(Unit))
        
        presenter.testConfiguration(mockDialog)
        
        verify(mockProviderTester).testProvider(testConfig)
        verify(mockMessageDisplay).showInfo(
            "${testConfig.type} token retrieved successfully",
            "Success"
        )
    }

    fun testTestConfigurationFailure() {
        val testException = Exception("Test error")
        whenever(mockDialog.validateDialog()).thenReturn(emptyList())
        whenever(mockDialog.getUpdatedConfig()).thenReturn(testConfig)
        whenever(mockProviderTester.testProvider(any())).thenReturn(Result.failure(testException))
        
        presenter.testConfiguration(mockDialog)
        
        verify(mockProviderTester).testProvider(testConfig)
        verify(mockMessageDisplay).showError(
            "Failed to get ${testConfig.type} token: Test error",
            "Error"
        )
    }

    fun testTestConfigurationSkippedOnValidationFailure() {
        val validationError = ValidationInfo("Test error")
        whenever(mockDialog.validateDialog()).thenReturn(listOf(validationError))
        
        presenter.testConfiguration(mockDialog)
        
        verify(mockDialog).validateDialog()
        verify(mockProviderTester, never()).testProvider(any())
        verify(mockMessageDisplay, never()).showInfo(any(), any())
        verify(mockMessageDisplay, never()).showError(any(), any())
    }

    fun testSequentialOperations() {
        whenever(mockDialog.validateDialog()).thenReturn(emptyList())
        whenever(mockDialog.getUpdatedConfig()).thenReturn(testConfig)
        whenever(mockDialog.showAndGet()).thenReturn(true)
        
        // Test full sequence of operations
        val showResult = presenter.showDialog(mockDialog)
        val config = presenter.getUpdatedConfig(mockDialog)
        
        assertTrue("Dialog should be shown", showResult)
        assertEquals("Should return correct config", testConfig, config)
        
        val inOrder = inOrder(mockDialog)
        inOrder.verify(mockDialog).validateDialog()
        inOrder.verify(mockDialog).showAndGet()
        inOrder.verify(mockDialog).getUpdatedConfig()
    }
}