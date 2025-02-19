package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.service.awscf.CliTokenRetriever.Companion.prepareProcessBuilder
import com.github.mpecan.runconfigenvinjector.state.CodeArtifactConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.FileEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.StructuredFileEnvProviderConfig
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.bind
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.validation.DialogValidation
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.*
import org.jetbrains.plugins.groovy.lang.resolve.log
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent

class EnvProviderConfigDialog(
    config: EnvProviderConfig,
    private val presenter: DialogPresenter = DefaultDialogPresenter()
) : DialogWrapper(true), ConfigurationDialog {
    private val availableRunConfigurations = mapOf(
        "MavenRunConfiguration" to "Maven",
        "GradleRunConfiguration" to "Gradle"
    )
    internal val envVarField = AtomicProperty(config.environmentVariable)
    internal val enabled = AtomicProperty(config.enabled)
    internal val enabledRunConfigurations =
        AtomicProperty(config.enabledRunConfigurations.ifEmpty { availableRunConfigurations.keys.toList() }
            .toSet())
    private val providerTypeField = AtomicProperty(config.type.let {
        when (it) {
            "File" -> "File"
            "StructuredFile" -> "StructuredFile"
            else -> "CodeArtifact"
        }
    })

    // CodeArtifact specific fields
    private val executablePath =
        AtomicProperty((config as? CodeArtifactConfig)?.executablePath ?: "aws")
    private val profileField = AtomicProperty((config as? CodeArtifactConfig)?.profile ?: "default")
    private val domainField = AtomicProperty((config as? CodeArtifactConfig)?.domain ?: "")
    private val domainOwnerField =
        AtomicProperty((config as? CodeArtifactConfig)?.domainOwner ?: "")
    private val regionField = AtomicProperty((config as? CodeArtifactConfig)?.region ?: "")
    private val tokenDurationField =
        AtomicProperty((config as? CodeArtifactConfig)?.tokenDuration ?: 3600)

    // File specific fields
    private val filePathField = AtomicProperty(when{
        config is FileEnvProviderConfig -> config.filePath
        config is StructuredFileEnvProviderConfig -> config.filePath
        else -> ""
    })
    private val encodingField =
        AtomicProperty((config as? FileEnvProviderConfig)?.encoding ?: "UTF-8")

    // Structured file specific fields
    private val structuredFormatField =
        AtomicProperty((config as? StructuredFileEnvProviderConfig)?.format ?: "ENV")
    private val keyField = AtomicProperty((config as? StructuredFileEnvProviderConfig)?.key ?: "")


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
    ): ValidationInfo? = if (providerTypeField.get() == providerType) {
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
                                    when {
                                        domainField.get()
                                            .contains(" ") -> ValidationInfo("Domain cannot contain spaces")

                                        domainField.get().length < 3 -> ValidationInfo("Domain must be at least 3 characters long")
                                        domainField.get().matches(Regex("[a-zA-Z0-9-]+")) -> null
                                        else -> ValidationInfo("Domain can only contain alphanumeric characters and hyphens")
                                    }
                                }
                            }
                        )
                    }
                    row("Domain Owner:") {
                        textField().bindText(domainOwnerField).validation(
                            DialogValidation {
                                validateIfVisible("CodeArtifact") {
                                    // Minimum length of 12
                                    when {
                                        domainOwnerField.get()
                                            .contains(" ") -> ValidationInfo("Domain Owner cannot contain spaces")

                                        domainOwnerField.get().length != 12 -> ValidationInfo("Domain Owner must be at exactly 12 characters long")
                                        domainOwnerField.get().matches(Regex("[0-9]+")) -> null
                                        else -> ValidationInfo("Domain Owner can only contain numbers")
                                    }
                                }
                            }
                        )
                    }
                    row("Region:") {
                        comboBox(AWS_REGIONS, SimpleListCellRenderer.create("") { it }).bindItem(
                            regionField
                        )
                    }
                    row("Token Duration (seconds):") {
                        intTextField(3600..43200, 3600).bindIntText(tokenDurationField)
                    }
                    row("Executable Path:") {
                        textField().bindText(executablePath).validation(
                            DialogValidation {
                                validateIfVisible("CodeArtifact") {
                                    when {
                                        executablePath.get()
                                            .isBlank() -> ValidationInfo("Executable path cannot be empty")

                                        executablePath.get()
                                            .contains(" ") -> ValidationInfo("Executable path cannot contain spaces")

                                        executablePath.get().let {
                                            prepareProcessBuilder(arrayOf("which", it))
                                                .start().waitFor() != 0
                                        } -> ValidationInfo("Executable path does not exist").also {
                                            log.warn("Process env: ${ProcessBuilder("which", "it")
                                                .environment()}")
                                            log.warn(System.getenv("PATH"))
                                        }

                                        else -> null
                                    }
                                }
                            }
                        )
                    }
                }

                filePanel = panel {
                    row("File Path:") {
                        cell(TextFieldWithBrowseButton().apply {
                            addBrowseFolderListener(
                                null,
                                FileChooserDescriptorFactory.createSingleFileDescriptor().apply {
                                    title = "Select File"
                                    description =
                                        "Select the file to read environment variables from"
                                }
                            )
                            bind(filePathField)
                        }).validation(
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
                        cell(TextFieldWithBrowseButton().apply {
                            addBrowseFolderListener(
                                null,
                                FileChooserDescriptorFactory.createSingleFileDescriptor().apply {
                                    title = "Select File"
                                    description =
                                        "Select the file to read environment variables from"
                                }
                            )
                            bind(filePathField)
                        }).validation(
                            DialogValidation {
                                validateIfVisible("StructuredFile") {
                                  when {
                                      filePathField.get()
                                          .isBlank() -> ValidationInfo("File path cannot be empty")

                                      else -> null
                                  }
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
                    row("Key:") { textField().bindText(keyField).validation(
                        DialogValidation {
                            validateIfVisible("StructuredFile") {
                              when{
                                keyField.get().isBlank() -> ValidationInfo("Key cannot be empty")
                                else -> null
                              }
                            }
                        }
                    ) }
                    row("Encoding:") { textField().bindText(encodingField) }
                }.visible(false)
            }

            row("Enable the configuration:") { checkBox("Enabled").bindSelected(enabled) }
        }

        group("Enabled Run Configurations") {
            panel {
                row {
                    availableRunConfigurations.forEach { (runConfiguration, text) ->
                        checkBox(text).onChanged {
                            if (it.isSelected) {
                                enabledRunConfigurations.set(enabledRunConfigurations.get() + runConfiguration)
                            } else {
                                enabledRunConfigurations.set(enabledRunConfigurations.get() - runConfiguration)
                            }
                        }.selected(enabledRunConfigurations.get().contains(runConfiguration))
                    }
                }
            }
        }
    }


    protected inner class TestConfigurationAction : DialogWrapperAction("Test Configuration") {
        override fun doAction(e: ActionEvent) {
            presenter.testConfiguration(this@EnvProviderConfigDialog)
        }
    }

    override fun doOKAction() {
        if (presenter.showDialog(this)) {
            super.doOKAction()
        }
    }

    override fun setProviderType(providerType: String) {
        providerTypeField.set(providerType)
        updateVisibility(providerType)
    }

    override fun validateDialog(): List<ValidationInfo> =
        doValidateAll()

    override fun show() {
        super.show()
    }

    override fun showAndGet(): Boolean {
        return super.showAndGet()
    }

    override fun createLeftSideActions(): Array<Action> {
        return arrayOf(TestConfigurationAction())
    }

    private fun updateVisibility(providerType: String) {
        codeArtifactPanel.visible(providerType == "CodeArtifact")
        filePanel.visible(providerType == "File")
        structuredFilePanel.visible(providerType == "StructuredFile")
    }

    override fun getUpdatedConfig(): EnvProviderConfig = when (providerTypeField.get()) {
        "CodeArtifact" -> constructCodeArtifactConfig()

        "File" -> constructFileEnvProviderConfig()

        "StructuredFile" -> constructStructuredFileEnvProviderConfig()

        else -> throw IllegalStateException("Unknown provider type: ${providerTypeField.get()}")
    }

    private fun constructStructuredFileEnvProviderConfig() = StructuredFileEnvProviderConfig(
        environmentVariable = envVarField.get(),
        enabled = enabled.get(),
        enabledRunConfigurations = enabledRunConfigurations.get(),
        filePath = filePathField.get(),
        format = structuredFormatField.get(),
        key = keyField.get(),
        encoding = encodingField.get()
    )

    private fun constructFileEnvProviderConfig() = FileEnvProviderConfig(
        environmentVariable = envVarField.get(),
        enabled = enabled.get(),
        enabledRunConfigurations = enabledRunConfigurations.get(),
        filePath = filePathField.get(),
        encoding = encodingField.get()
    )

    private fun constructCodeArtifactConfig() = CodeArtifactConfig(
        environmentVariable = envVarField.get(),
        profile = profileField.get(),
        domain = domainField.get(),
        domainOwner = domainOwnerField.get(),
        region = regionField.get(),
        tokenDuration = tokenDurationField.get(),
        enabled = enabled.get(),
        enabledRunConfigurations = enabledRunConfigurations.get(),
        executablePath = executablePath.get()
    )

    private fun isValidEnvVarName(name: String): Boolean =
        name.matches(Regex("[A-Za-z_][A-Za-z0-9_]*"))

    companion object {
        private val AWS_REGIONS = listOf(
            "us-east-1", "us-east-2", "us-west-1", "us-west-2",
            "eu-west-1", "eu-central-1"
        )
    }
}
