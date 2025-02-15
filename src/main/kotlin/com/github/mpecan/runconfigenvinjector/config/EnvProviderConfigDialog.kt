package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.CodeArtifactConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.FileEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.StructuredFileEnvProviderConfig
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import javax.swing.JCheckBox
import javax.swing.JComponent

class EnvProviderConfigDialog(
    config: EnvProviderConfig
) : DialogWrapper(true) {
    private val envVarField = AtomicProperty(config.environmentVariable)
    private val enabledCheckbox = JCheckBox("Enabled", config.enabled)
    private val enabledRunConfigurations = AtomicProperty(config.enabledRunConfigurations)
    private val providerTypeField = AtomicProperty(config.type)

    // CodeArtifact specific fields
    private val profileField = AtomicProperty((config as? CodeArtifactConfig)?.profile ?: "")
    private val domainField = AtomicProperty((config as? CodeArtifactConfig)?.domain ?: "")
    private val domainOwnerField =
        AtomicProperty((config as? CodeArtifactConfig)?.domainOwner ?: "")
    private val regionField = AtomicProperty((config as? CodeArtifactConfig)?.region ?: "")
    private val useAwsCliCheckbox =
        JCheckBox("Use AWS CLI", (config as? CodeArtifactConfig)?.useAwsCli ?: true)
    private val tokenDurationField =
        AtomicProperty((config as? CodeArtifactConfig)?.tokenDuration ?: 3600)

    // File specific fields
    private val filePathField = AtomicProperty((config as? FileEnvProviderConfig)?.filePath ?: "")
    private val encodingField =
        AtomicProperty((config as? FileEnvProviderConfig)?.encoding ?: "UTF-8")

    // Structured file specific fields
    private val structuredFormatField =
        AtomicProperty((config as? StructuredFileEnvProviderConfig)?.format ?: "ENV")
    private val keyField = AtomicProperty((config as? StructuredFileEnvProviderConfig)?.key ?: "")

    private val availableRunConfigurations = mapOf(
        "MavenRunConfiguration" to "Maven",
        "GradleRunConfiguration" to "Gradle"
    )

    private val providerTypes = listOf("CodeArtifact", "File", "StructuredFile")
    private lateinit var codeArtifactPanel: Panel
    private lateinit var filePanel: Panel
    private lateinit var structuredFilePanel: Panel

    init {
        title = "Environment Injector Configuration"
        init()
    }

    fun validateIfVisible(
        providerType: String,
        validation: () -> ValidationInfo?
    ): ValidationInfo? = if (providerTypeField.equals(providerType)) {
        validation()
    } else null

    override fun createCenterPanel(): JComponent = panel {
        row("Environment Variable:") {
            textField().bindText(envVarField).validation(
                DialogValidation {
                    if (isValidEnvVarName(envVarField.get())) null else ValidationInfo("Invalid environment variable name")
                }
            )
        }


        row("Provider Type:") {
            comboBox(providerTypes, SimpleListCellRenderer.create("") { it })
                .bindItem(providerTypeField)
                .onChanged {
                    val selectedItem = it.selectedItem as String
                    providerTypeField.set(selectedItem)
                    updateVisibility(selectedItem)
                }
        }


        group("Provider Configuration") {
            panel {
                codeArtifactPanel = panel {
                    row("Profile:") { textField().bindText(profileField) }
                    row("Domain:") {
                        textField().bindText(domainField).validation(
                            DialogValidation {
                                validateIfVisible("CodeArtifact") {
                                    if (domainField.get()
                                            .isNotBlank()
                                    ) null else ValidationInfo("Domain cannot be empty")
                                }
                            }
                        )
                    }
                    row("Domain Owner:") {
                        textField().bindText(domainOwnerField).validation(
                            DialogValidation {
                                validateIfVisible("CodeArtifact") {
                                    if (domainOwnerField.get()
                                            .isNotBlank()
                                    ) null else ValidationInfo("Domain Owner cannot be empty")
                                }
                            }
                        )
                    }
                    row("Region:") {
                        comboBox(AWS_REGIONS, SimpleListCellRenderer.create("") { it }).bindItem(
                            regionField
                        )
                    }
                    row { useAwsCliCheckbox }
                    row("Token Duration (seconds):") {
                        intTextField(3600..43200, 3600).bindIntText(tokenDurationField)
                    }
                }

                filePanel = panel {
                    row("File Path:") {
                        textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()).bindText(
                            filePathField
                        ).validation(
                            DialogValidation {
                                validateIfVisible("File") {
                                    if (filePathField.get()
                                            .isNotBlank()
                                    ) null else ValidationInfo("File path cannot be empty")
                                }
                            }
                        )
                    }
                    row("Encoding:") { textField().bindText(encodingField) }
                }.visible(false)

                structuredFilePanel = panel {
                    row("File Path:") {
                        textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor()).bindText(
                            filePathField
                        ).validation(
                            DialogValidation {
                                validateIfVisible("StructuredFile") {
                                    if (filePathField.get()
                                            .isNotBlank()
                                    ) null else ValidationInfo("File path cannot be empty")
                                }
                            }
                        )
                    }
                    row("Format:") {
                        comboBox(
                            listOf("ENV", "JSON", "YAML"),
                            SimpleListCellRenderer.create("") { it })
                            .bindItem(structuredFormatField)
                    }
                    row("Key:") { textField().bindText(keyField) }
                    row("Encoding:") { textField().bindText(encodingField) }
                }.visible(false)
            }
        }

        row { enabledCheckbox }
        group("Enabled Run Configurations") {
            panel {
                row {
                    availableRunConfigurations.forEach { (runConfiguration, text) ->
                        checkBox(text).onChanged {
                            if (enabledRunConfigurations.get().contains(runConfiguration)) {
                                enabledRunConfigurations.set(enabledRunConfigurations.get() - runConfiguration)
                            } else {
                                enabledRunConfigurations.set(enabledRunConfigurations.get() + runConfiguration)
                            }
                        }.selected(enabledRunConfigurations.get().contains(runConfiguration))
                    }
                }
            }
        }
    }

    private fun updateVisibility(providerType: String) {
        codeArtifactPanel.visible(providerType == "CodeArtifact")
        filePanel.visible(providerType == "File")
        structuredFilePanel.visible(providerType == "StructuredFile")
    }

    fun getUpdatedConfig(): EnvProviderConfig = when (providerTypeField.get()) {
        "CodeArtifact" -> CodeArtifactConfig(
            environmentVariable = envVarField.get(),
            profile = profileField.get(),
            domain = domainField.get(),
            domainOwner = domainOwnerField.get(),
            region = regionField.get(),
            useAwsCli = useAwsCliCheckbox.isSelected,
            tokenDuration = tokenDurationField.get(),
            enabled = enabledCheckbox.isSelected,
            enabledRunConfigurations = enabledRunConfigurations.get()
        )

        "File" -> FileEnvProviderConfig(
            environmentVariable = envVarField.get(),
            enabled = enabledCheckbox.isSelected,
            enabledRunConfigurations = enabledRunConfigurations.get(),
            filePath = filePathField.get(),
            encoding = encodingField.get()
        )

        "StructuredFile" -> StructuredFileEnvProviderConfig(
            environmentVariable = envVarField.get(),
            enabled = enabledCheckbox.isSelected,
            enabledRunConfigurations = enabledRunConfigurations.get(),
            filePath = filePathField.get(),
            format = structuredFormatField.get(),
            key = keyField.get(),
            encoding = encodingField.get()
        )

        else -> throw IllegalStateException("Unknown provider type: ${providerTypeField.get()}")
    }

    private fun isValidEnvVarName(name: String): Boolean =
        name.matches(Regex("[A-Za-z_][A-Za-z0-9_]*"))

    companion object {
        private val AWS_REGIONS = listOf(
            "us-east-1", "us-east-2", "us-west-1", "us-west-2",
            "eu-west-1", "eu-central-1"
        )
    }
}
