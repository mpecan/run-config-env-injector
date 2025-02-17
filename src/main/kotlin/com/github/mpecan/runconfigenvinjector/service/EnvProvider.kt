package com.github.mpecan.runconfigenvinjector.service

import com.github.mpecan.runconfigenvinjector.service.awscf.CodeArtifactProvider
import com.github.mpecan.runconfigenvinjector.service.file.FileEnvProvider
import com.github.mpecan.runconfigenvinjector.service.file.StructuredFileEnvProvider
import com.github.mpecan.runconfigenvinjector.state.CodeArtifactConfig
import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.FileEnvProviderConfig
import com.github.mpecan.runconfigenvinjector.state.StructuredFileEnvProviderConfig

interface EnvProvider<T : EnvProviderConfig> {
    fun getValue(): CacheableValue?
    fun getCacheKey(): String
}

object EnvProviderFactory {

    fun <T : EnvProviderConfig> createProvider(config: T): EnvProvider<out EnvProviderConfig> {
        return when(config) {
            is CodeArtifactConfig -> CodeArtifactProvider(config)
            is StructuredFileEnvProviderConfig -> StructuredFileEnvProvider(config)
            is FileEnvProviderConfig -> FileEnvProvider(config)
            else -> throw RuntimeException("No known implementation for $config")
        }
    }
}