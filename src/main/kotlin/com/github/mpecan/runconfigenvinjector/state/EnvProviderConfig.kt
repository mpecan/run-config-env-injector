package com.github.mpecan.runconfigenvinjector.state

sealed interface EnvProviderConfig {
    val environmentVariable: String
    val enabled: Boolean
    val enabledRunConfigurations: Set<String>
    val type: String
}

data class BaseEnvProviderConfig(
    override val environmentVariable: String = "",
    override val enabled: Boolean = true,
    override val enabledRunConfigurations: Set<String> = emptySet(),
    override val type: String
) : EnvProviderConfig

data class CodeArtifactConfig(
    override val environmentVariable: String = "",
    override val enabled: Boolean = true,
    override val enabledRunConfigurations: Set<String> = emptySet(),
    val profile: String = "",
    val domain: String = "",
    val domainOwner: String = "",
    val region: String = "",
    val tokenDuration: Int = 3600
) : EnvProviderConfig {
    override val type: String = "CodeArtifact"
}

data class FileEnvProviderConfig(
    override val environmentVariable: String = "",
    override val enabled: Boolean = true,
    override val enabledRunConfigurations: Set<String> = emptySet(),
    val filePath: String = "",
    val encoding: String = "UTF-8"
) : EnvProviderConfig {
    override val type: String = "File"
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
}