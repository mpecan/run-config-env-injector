package com.github.mpecan.runconfigenvinjector.state

sealed interface EnvProviderConfig {
    val environmentVariable: String
    val enabled: Boolean
    val enabledRunConfigurations: Set<String>
    val type: String
    fun copy(
        environmentVariable: String = this.environmentVariable,
        enabled: Boolean = this.enabled,
        enabledRunConfigurations: Set<String> = this.enabledRunConfigurations
    ): EnvProviderConfig

    fun toStoredConfiguration(): StoredConfiguration
}

data class BaseEnvProviderConfig(
    override val environmentVariable: String = "",
    override val enabled: Boolean = true,
    override val enabledRunConfigurations: Set<String> = emptySet(),
    override val type: String
) : EnvProviderConfig {
    override fun copy(
        environmentVariable: String,
        enabled: Boolean,
        enabledRunConfigurations: Set<String>
    ): EnvProviderConfig {
        return BaseEnvProviderConfig(environmentVariable, enabled, enabledRunConfigurations, type)
    }

    override fun toStoredConfiguration(): StoredConfiguration {
        throw RuntimeException("Not implemented")
    }
}

data class CodeArtifactConfig(
    override val environmentVariable: String = "",
    override val enabled: Boolean = true,
    override val enabledRunConfigurations: Set<String> = emptySet(),
    val profile: String = "",
    val domain: String = "",
    val domainOwner: String = "",
    val region: String = "",
    val tokenDuration: Int = 3600,
    val executablePath: String = "aws"
) : EnvProviderConfig {

    override val type: String = "CodeArtifact"
    override fun copy(
        environmentVariable: String,
        enabled: Boolean,
        enabledRunConfigurations: Set<String>
    ): EnvProviderConfig {
        return CodeArtifactConfig(
            environmentVariable,
            enabled,
            enabledRunConfigurations,
            profile,
            domain,
            domainOwner,
            region,
            tokenDuration,
            executablePath
        )
    }

    companion object {
        fun fromStoredConfiguration(storedConfiguration: StoredConfiguration) =
            CodeArtifactConfig(
                storedConfiguration.environmentVariable ?: "",
                storedConfiguration.enabled,
                storedConfiguration.enabledRunConfigurations.toSet(),
                storedConfiguration.additionalSettings["profile"] ?: "",
                storedConfiguration.additionalSettings["domain"] ?: "",
                storedConfiguration.additionalSettings["domainOwner"] ?: "",
                storedConfiguration.additionalSettings["region"] ?: "",
                storedConfiguration.additionalSettings["tokenDuration"]?.toIntOrNull() ?: 3600,
                storedConfiguration.additionalSettings["executablePath"] ?: "aws"
            )
    }

    override fun toStoredConfiguration(): StoredConfiguration {
        return StoredConfiguration().apply {
            this.environmentVariable = this@CodeArtifactConfig.environmentVariable
            this.enabled = this@CodeArtifactConfig.enabled
            this.enabledRunConfigurations = this@CodeArtifactConfig.enabledRunConfigurations.toMutableList()
            this.type = this@CodeArtifactConfig.type
            this.additionalSettings = mutableMapOf(
                "profile" to this@CodeArtifactConfig.profile,
                "domain" to this@CodeArtifactConfig.domain,
                "domainOwner" to this@CodeArtifactConfig.domainOwner,
                "region" to this@CodeArtifactConfig.region,
                "tokenDuration" to this@CodeArtifactConfig.tokenDuration.toString(),
                "executablePath" to this@CodeArtifactConfig.executablePath
            )
        }
    }
}

data class FileEnvProviderConfig(
    override val environmentVariable: String = "",
    override val enabled: Boolean = true,
    override val enabledRunConfigurations: Set<String> = emptySet(),
    val filePath: String = "",
    val encoding: String = "UTF-8"
) : EnvProviderConfig {
    override val type: String = "File"
    override fun copy(
        environmentVariable: String,
        enabled: Boolean,
        enabledRunConfigurations: Set<String>
    ): EnvProviderConfig {
        return FileEnvProviderConfig(
            environmentVariable,
            enabled,
            enabledRunConfigurations,
            filePath,
            encoding
        )
    }

    companion object {
        fun fromStoredConfiguration(storedConfiguration: StoredConfiguration) =
            FileEnvProviderConfig(
                storedConfiguration.environmentVariable ?: "",
                storedConfiguration.enabled,
                storedConfiguration.enabledRunConfigurations.toSet(),
                storedConfiguration.additionalSettings["filePath"] ?: "",
                storedConfiguration.additionalSettings["encoding"] ?: "UTF-8"
            )
    }

    override fun toStoredConfiguration(): StoredConfiguration {
        return StoredConfiguration().apply {
            this.environmentVariable = this@FileEnvProviderConfig.environmentVariable
            this.enabled = this@FileEnvProviderConfig.enabled
            this.enabledRunConfigurations = this@FileEnvProviderConfig.enabledRunConfigurations.toMutableList()
            this.type = this@FileEnvProviderConfig.type
            this.additionalSettings = mutableMapOf(
                "filePath" to this@FileEnvProviderConfig.filePath,
                "encoding" to this@FileEnvProviderConfig.encoding
            )
        }
    }
}

data class StructuredFileEnvProviderConfig(
    override val environmentVariable: String = "",
    override val enabled: Boolean = true,
    override val enabledRunConfigurations: Set<String> = emptySet(),
    val filePath: String = "",
    val format: String = "ENV",
    val key: String = "",
    val encoding: String = "UTF-8"
) : EnvProviderConfig {
    override val type: String = "StructuredFile"
    override fun copy(
        environmentVariable: String,
        enabled: Boolean,
        enabledRunConfigurations: Set<String>
    ): EnvProviderConfig {
        return StructuredFileEnvProviderConfig(
            environmentVariable,
            enabled,
            enabledRunConfigurations,
            filePath,
            format,
            key,
            encoding
        )
    }

    companion object {
        fun fromStoredConfiguration(storedConfiguration: StoredConfiguration) =
            StructuredFileEnvProviderConfig(
                storedConfiguration.environmentVariable ?: "",
                storedConfiguration.enabled,
                storedConfiguration.enabledRunConfigurations.toSet(),
                storedConfiguration.additionalSettings["filePath"] ?: "",
                storedConfiguration.additionalSettings["format"] ?: "ENV",
                storedConfiguration.additionalSettings["key"] ?: "",
                storedConfiguration.additionalSettings["encoding"] ?: "UTF-8"
            )
    }

    override fun toStoredConfiguration(): StoredConfiguration {
        return StoredConfiguration().apply {
            this.environmentVariable = this@StructuredFileEnvProviderConfig.environmentVariable
            this.enabled = this@StructuredFileEnvProviderConfig.enabled
            this.enabledRunConfigurations = this@StructuredFileEnvProviderConfig.enabledRunConfigurations.toMutableList()
            this.type = this@StructuredFileEnvProviderConfig.type
            this.additionalSettings = mutableMapOf(
                "filePath" to this@StructuredFileEnvProviderConfig.filePath,
                "format" to this@StructuredFileEnvProviderConfig.format,
                "key" to this@StructuredFileEnvProviderConfig.key,
                "encoding" to this@StructuredFileEnvProviderConfig.encoding
            )
        }
    }
}