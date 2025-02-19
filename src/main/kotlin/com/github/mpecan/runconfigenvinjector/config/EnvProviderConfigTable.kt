package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.BaseEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderSettings
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel

class EnvProviderConfigTable(
    private val dialogFactory: EnvProviderDialogFactory = DefaultEnvProviderDialogFactory()
) : JTable() {
    private val tableModel = EnvProviderTableModel()

    init {
        model = tableModel
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    editSelectedRow()
                }
            }
        })
    }

    fun editSelectedRow() {
        val row = selectedRow
        if (row >= 0) {
            val config = tableModel.getConfigAt(row)
            val dialog = dialogFactory.createDialog(config)
            if (dialog.showAndGet()) {
                tableModel.updateConfig(row, dialog.getUpdatedConfig())
            }
        }
    }

    fun addNewConfig() {
        val dialog = dialogFactory.createDialog(BaseEnvProviderConfig(type = "CodeArtifact"))
        if (dialog.showAndGet()) {
            tableModel.addConfig(dialog.getUpdatedConfig())
        }
    }

    fun removeSelectedRows() {
        selectedRows.sortedDescending().forEach(tableModel::removeRow)
    }

    fun isModified(): Boolean {
        return tableModel.isModified()
    }

    fun applyChanges() {
        tableModel.applySettings()
    }
}

class EnvProviderTableModel : AbstractTableModel() {
    private val configs = mutableListOf<EnvProviderConfig>()
    private val columnNames = arrayOf(
        "Environment Variable", "Type", "Active Configurations", "Enabled"
    )

    init {
        // validate and discard invalid configurations on load
        val validConfigurations = EnvProviderSettings.getInstance().state.getFromStoredConfigurations().filter {
           try {
               it.environmentVariable.isNotBlank() && it.type.isNotBlank()
           } catch (e: Exception) {
               false
           }
        }
        configs.addAll(validConfigurations)
    }

    override fun getRowCount(): Int = configs.size
    override fun getColumnCount(): Int = columnNames.size
    override fun getColumnName(column: Int): String = columnNames[column]
    override fun isCellEditable(row: Int, column: Int): Boolean = false

    val availableRunConfigurations = mapOf(
        "MavenRunConfiguration" to "Maven",
        "GradleRunConfiguration" to "Gradle"
    )

    override fun getValueAt(row: Int, col: Int): Any = with(configs[row]) {
        when(col) {
            0 -> environmentVariable
            1 -> type
            2 -> enabledRunConfigurations.map { availableRunConfigurations[it] }.joinToString(", ")
            3 -> enabled
            else -> throw IllegalStateException("Invalid column: $col")
        }
    }

    fun getConfigAt(row: Int): EnvProviderConfig = configs[row]

    fun updateConfig(row: Int, config: EnvProviderConfig) {
        configs[row] = config
        fireTableRowsUpdated(row, row)
    }

    fun addConfig(config: EnvProviderConfig) {
        configs.add(config)
        fireTableRowsInserted(configs.size - 1, configs.size - 1)
    }

    fun removeRow(row: Int) {
        configs.removeAt(row)
        fireTableRowsDeleted(row, row)
    }

    fun applySettings() {
        EnvProviderSettings.getInstance().state.setToStoredConfigurations(configs)
    }

    fun isModified() = EnvProviderSettings.getInstance().state.getFromStoredConfigurations() != configs
}