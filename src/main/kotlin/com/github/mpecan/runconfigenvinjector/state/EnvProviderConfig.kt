package com.github.mpecan.runconfigenvinjector.state

sealed interface EnvProviderConfig  {
    val environmentVariable: String
    val enabled: Boolean
    val enabledRunConfigurations: Set<String>
    val type: String
    fun copy(
        environmentVariable: String = this.environmentVariable,
        enabled: Boolean = this.enabled,
        enabledRunConfigurations: Set<String> = this.enabledRunConfigurations
    ): EnvProviderConfig
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
        return CodeArtifactConfig(environmentVariable, enabled, enabledRunConfigurations, profile, domain, domainOwner, region, tokenDuration, executablePath)
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
        return FileEnvProviderConfig(environmentVariable, enabled, enabledRunConfigurations, filePath, encoding)
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
        return StructuredFileEnvProviderConfig(environmentVariable, enabled, enabledRunConfigurations, filePath, format, key, encoding)
    }
}