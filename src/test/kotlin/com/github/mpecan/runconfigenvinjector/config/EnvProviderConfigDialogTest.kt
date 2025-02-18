//package com.github.mpecan.runconfigenvinjector.config
//
//import com.github.mpecan.runconfigenvinjector.state.BaseEnvProviderConfig
//import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
//import com.intellij.openapi.ui.ValidationInfo
//import com.intellij.testFramework.fixtures.BasePlatformTestCase
//import org.mockito.kotlin.*
//
//class EnvProviderConfigDialogTest : BasePlatformTestCase() {
//    private lateinit var mockPresenter: DialogPresenter
//    private lateinit var testConfig: EnvProviderConfig
//
//    override fun setUp() {
//        super.setUp()
//        mockPresenter = mock()
//        testConfig = BaseEnvProviderConfig(
//            environmentVariable = "TEST_VAR",
//            type = "CodeArtifact",
//            enabled = true,
//            enabledRunConfigurations = setOf()
//        )
//    }
//
//    fun testDialogValidation() {
//        val validationError = ValidationInfo("Test error")
//        whenever(mockPresenter.validateDialog(any())).thenReturn(listOf(validationError))
//        whenever(mockPresenter.showDialog(any())).thenReturn(false)
//
//        val dialog = EnvProviderConfigDialog(testConfig, mockPresenter)
//        dialog.show()
//
//        verify(mockPresenter).validateDialog(dialog)
//        verify(mockPresenter, never()).getUpdatedConfig(any())
//    }
//
//    fun testProviderTypeSwitch() {
//        whenever(mockPresenter.showDialog(any())).thenReturn(true)
//        whenever(mockPresenter.validateDialog(any())).thenReturn(emptyList())
//
//        val dialog = EnvProviderConfigDialog(testConfig, mockPresenter)
//        dialog.setProviderType("File")
//
//        val captor = argumentCaptor<EnvProviderConfig>()
//        verify(mockPresenter).validateDialog(eq(dialog))
//
//        dialog.getUpdatedConfig()
//        verify(mockPresenter).getUpdatedConfig(eq(dialog))
//    }
//
//    fun testTestConfigurationAction() {
//        val dialog = EnvProviderConfigDialog(testConfig, mockPresenter)
//
//        // Simulate clicking test configuration button
//        dialog.createActions().first { it is EnvProviderConfigDialog.TestConfigurationAction }
//            .actionPerformed(null)
//
//        verify(mockPresenter).testConfiguration(eq(dialog))
//    }
//
//    fun testSuccessfulEdit() {
//        whenever(mockPresenter.showDialog(any())).thenReturn(true)
//        whenever(mockPresenter.validateDialog(any())).thenReturn(emptyList())
//        whenever(mockPresenter.getUpdatedConfig(any())).thenReturn(
//            testConfig.copy(environmentVariable = "UPDATED_VAR")
//        )
//
//        val dialog = EnvProviderConfigDialog(testConfig, mockPresenter)
//        dialog.doOKAction()
//
//        val inOrder = inOrder(mockPresenter)
//        inOrder.verify(mockPresenter).validateDialog(eq(dialog))
//        inOrder.verify(mockPresenter).showDialog(eq(dialog))
//        inOrder.verify(mockPresenter).getUpdatedConfig(eq(dialog))
//    }
//}