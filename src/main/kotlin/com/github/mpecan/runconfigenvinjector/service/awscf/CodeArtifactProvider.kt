package com.github.mpecan.runconfigenvinjector.service.awscf

import com.github.mpecan.runconfigenvinjector.service.CacheableValue
import com.github.mpecan.runconfigenvinjector.service.EnvProvider
import com.github.mpecan.runconfigenvinjector.service.TimeBasedValue
import com.github.mpecan.runconfigenvinjector.state.CodeArtifactConfig
import com.intellij.openapi.diagnostic.Logger
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.CodeartifactException
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest

class CodeArtifactProvider(private val config: CodeArtifactConfig) :
    EnvProvider<CodeArtifactConfig> {
    private val log = Logger.getInstance(CodeArtifactProvider::class.java)
    override fun getCacheKey(): String {
        return "codeartifact-${config.domain}-${config.domainOwner}-${config.region}-${config.profile}"
    }

    override fun getValue(): CacheableValue? {
        return try {
            if (config.useAwsCli) {
                getTokenFromAwsCli(config)
            } else {
                getTokenFromSdk(config)
            }
        } catch (e: Exception) {
            log.warn("Failed to get CodeArtifact token", e)
            null
        }
    }

    private fun getTokenFromSdk(config: CodeArtifactConfig): CacheableValue? {
        val credentialsProvider = if (config.profile.isNotBlank() && config.profile != "default") {
            ProfileCredentialsProvider.create(config.profile)
        } else {
            ProfileCredentialsProvider.create()
        }

        val client = CodeartifactClient.builder()
            .region(Region.of(config.region))
            .credentialsProvider(credentialsProvider)
            .build()

        return try {
            val request = GetAuthorizationTokenRequest.builder()
                .domain(config.domain)
                .domainOwner(config.domainOwner)
                .durationSeconds(config.tokenDuration.toLong())
                .build()

            val authorizationToken = client.getAuthorizationToken(request)
            TimeBasedValue(
                authorizationToken.expiration().epochSecond,
                authorizationToken.authorizationToken()
            )
        } catch (e: CodeartifactException) {
            log.warn("Failed to get authorization token from AWS SDK", e)
            null
        }
    }

    private fun getTokenFromAwsCli(config: CodeArtifactConfig): CacheableValue? {
        val retriever = CliTokenRetriever()
        return retriever.getAuthToken(config)
    }
}