package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.BaseEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderSettings
import com.github.mpecan.runconfigenvinjector.state.FileEnvProviderConfig
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ui.UIUtil
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.awt.event.MouseEvent

class EnvProviderConfigTableTest : BasePlatformTestCase() {
    private lateinit var table: EnvProviderConfigTable
    private lateinit var testConfigs: List<EnvProviderConfig>

    private lateinit var configurationDialog: ConfigurationDialog

    override fun setUp() {
        super.setUp()
        UIUtil.dispatchAllInvocationEvents()

        // Create test configurations
        testConfigs = listOf(
            FileEnvProviderConfig(
                environmentVariable = "TEST_VAR_1",
                enabled = true,
                enabledRunConfigurations = setOf("MavenRunConfiguration"),
                filePath = "/path/to/secrets.txt"
            ),
            FileEnvProviderConfig(
                environmentVariable = "TEST_VAR_2",
                enabled = false,
                enabledRunConfigurations = setOf("GradleRunConfiguration"),
                filePath = "/path/to/secrets2.txt"
            )
        )

        // Initialize settings with test data
        val settings = EnvProviderSettings.getInstance().state
        settings.setToStoredConfigurations(testConfigs)

        // Create table
        configurationDialog = mock<ConfigurationDialog>()
        table = EnvProviderConfigTable(
            dialogFactory = mock {
                on { createDialog(any()) } doReturn configurationDialog
            }
        )
    }

    fun testTableModelColumns() {
        val model = table.model
        assertEquals("Table should have 4 columns", 4, model.columnCount)
        assertEquals("Environment Variable", model.getColumnName(0))
        assertEquals("Type", model.getColumnName(1))
        assertEquals("Active Configurations", model.getColumnName(2))
        assertEquals("Enabled", model.getColumnName(3))
    }

    fun testTableInitialData() {
        val model = table.model
        assertEquals("Table should have correct number of rows", testConfigs.size, model.rowCount)

        // Test first row
        assertEquals("TEST_VAR_1", model.getValueAt(0, 0))
        assertEquals("File", model.getValueAt(0, 1))
        assertEquals("Maven", model.getValueAt(0, 2))
        assertEquals(true, model.getValueAt(0, 3))

        // Test second row
        assertEquals("TEST_VAR_2", model.getValueAt(1, 0))
        assertEquals("File", model.getValueAt(1, 1))
        assertEquals("Gradle", model.getValueAt(1, 2))
        assertEquals(false, model.getValueAt(1, 3))
    }

    fun testAddNewConfig() {
        val initialRowCount = table.model.rowCount

        whenever(configurationDialog.showAndGet()).thenReturn(true)
        whenever(configurationDialog.getUpdatedConfig()).thenReturn(
            BaseEnvProviderConfig(
                environmentVariable = "NEW_VAR",
                type = "CodeArtifact",
                enabled = true,
                enabledRunConfigurations = setOf()
            )
        )
        // Create dialog but don't show it
        table.addNewConfig()

        // Simulate dialog confirmation and config addition

        assertEquals(
            "Row count should increase by 1",
            initialRowCount + 1, table.model.rowCount
        )

        // Verify the new config
        val newConfig = (table.model as EnvProviderTableModel).getConfigAt(initialRowCount)
        assertEquals("CodeArtifact", newConfig.type)
        assertTrue("New config should be enabled", newConfig.enabled)
    }

    fun testRemoveSelectedRows() {
        table.setRowSelectionInterval(0, 0)
        val initialRowCount = table.model.rowCount

        table.removeSelectedRows()

        assertEquals(
            "One row should be removed",
            initialRowCount - 1, table.model.rowCount
        )
        assertEquals(
            "Remaining row should be TEST_VAR_2",
            "TEST_VAR_2", table.model.getValueAt(0, 0)
        )
    }

    fun testIsModified() {
        assertFalse("Table should not be modified initially", table.isModified())

        // Remove a row to modify the table
        table.setRowSelectionInterval(0, 0)
        table.removeSelectedRows()

        assertTrue("Table should be modified after removing row", table.isModified())
    }

    fun testApplyChanges() {
        // Remove a row
        table.setRowSelectionInterval(0, 0)
        table.removeSelectedRows()

        // Apply changes
        table.applyChanges()

        // Verify changes are persisted to settings
        val settings = EnvProviderSettings.getInstance().state
        assertEquals(
            "Settings should have one configuration",
            1, settings.configurations.size
        )
        assertEquals(
            "Remaining config should be TEST_VAR_2",
            "TEST_VAR_2", settings.configurations[0].environmentVariable
        )
    }

    fun testDoubleClickRowEditing() {
        // Select first row
        table.setRowSelectionInterval(0, 0)
        val initialConfig = (table.model as EnvProviderTableModel).getConfigAt(0)

        // Create a point in the center of the first row
        val rect = table.getCellRect(0, 0, true)
        val point = java.awt.Point(rect.x + rect.width / 2, rect.y + rect.height / 2)

        // Simulate double click
        UIUtil.invokeAndWaitIfNeeded {
            val clickEvent = MouseEvent(
                table,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                point.x,
                point.y,
                2, // click count
                false
            )
            table.mouseListeners.forEach { it.mouseClicked(clickEvent) }
        }

        // Since we can't interact with dialog in test, config should remain unchanged
        val currentConfig = (table.model as EnvProviderTableModel).getConfigAt(0)
        assertEquals(
            "Config should remain unchanged",
            initialConfig.environmentVariable, currentConfig.environmentVariable
        )
    }

    override fun tearDown() {
        // Clear settings
        EnvProviderSettings.getInstance().state.configurations = ArrayList()
        UIUtil.dispatchAllInvocationEvents()
        super.tearDown()
    }
}