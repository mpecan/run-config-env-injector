package com.github.mpecan.runconfigenvinjector.config

import com.intellij.openapi.options.Configurable
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class EnvProviderSettingsConfigurable : Configurable {
    private var mainPanel: JPanel? = null
    private var configTable: EnvProviderConfigTable? = null

    override fun createComponent(): JComponent = JPanel(BorderLayout()).apply {
        mainPanel = this

        val table = EnvProviderConfigTable()
            .also { configTable = it }
        add(JPanel().apply {
            add(JButton("Add").apply {
                addActionListener { table.addNewConfig() }
            })
            add(JButton("Edit").apply {
                addActionListener { table.editSelectedRow() }
            })
            add(JButton("Remove").apply {
                addActionListener { table.removeSelectedRows() }
            })
        }, BorderLayout.NORTH)

        add(JScrollPane(table), BorderLayout.CENTER)
    }

    override fun isModified(): Boolean {
        return configTable?.isModified() ?: false
    }

    override fun apply() {
        return configTable?.applyChanges() ?: Unit
    }

    override fun getDisplayName(): String {
        return "CodeArtifact Settings"
    }
}