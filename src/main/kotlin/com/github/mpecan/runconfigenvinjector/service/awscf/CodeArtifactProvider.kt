package com.github.mpecan.runconfigenvinjector.service.awscf

import com.github.mpecan.runconfigenvinjector.service.CacheableValue
import com.github.mpecan.runconfigenvinjector.service.EnvProvider
import com.github.mpecan.runconfigenvinjector.state.CodeArtifactConfig
import com.intellij.openapi.diagnostic.Logger

class CodeArtifactProvider(private val config: CodeArtifactConfig) :
    EnvProvider<CodeArtifactConfig> {
    private val log = Logger.getInstance(CodeArtifactProvider::class.java)
    override fun getCacheKey(): String {
        return "codeartifact-${config.domain}-${config.domainOwner}-${config.region}-${config.profile}"
    }

    override fun getValue(): CacheableValue? {
        return try {
                getTokenFromAwsCli(config)
        } catch (e: Exception) {
            log.error("Failed to get CodeArtifact token", e)
            null
        }
    }

    private fun getTokenFromAwsCli(config: CodeArtifactConfig): CacheableValue? {
        val retriever = CliTokenRetriever()
        return retriever.getAuthToken(config)
    }
}