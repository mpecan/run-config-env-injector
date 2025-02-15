package com.github.mpecan.runconfigenvinjector.service.awscf

import com.github.mpecan.runconfigenvinjector.service.CacheableValue
import com.github.mpecan.runconfigenvinjector.service.ProcessExecutor
import com.github.mpecan.runconfigenvinjector.service.TokenRetriever
import com.github.mpecan.runconfigenvinjector.state.CodeArtifactConfig

class CliTokenRetriever(
    private val processExecutor: ProcessExecutor = ProcessExecutor(),
    private val authHandler: AwsAuthenticationHandler = AwsAuthenticationHandler()
) : TokenRetriever {
    private val tokenRegexp =
        Regex("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")

    override fun getAuthToken(config: CodeArtifactConfig): CacheableValue? {
        return try {
            getTokenFromCli(config)?.let { CodeArtifactToken(it) }
        } catch (e: Exception) {
            authHandler.showError(e.message ?: "Unknown error")
            null
        }
    }

    private fun getTokenFromCli(config: CodeArtifactConfig): String? {
        val command = buildCommand(config)

        var result: String? = null
        var retryNeeded = true

        while (retryNeeded) {
            try {
                result = processExecutor.execute(command) { process, error ->
                    when {
                        authHandler.handleSsoChallenge(error)?.let { challenge ->
                            authHandler.showSsoInstructions(challenge)
                            ProcessBuilder("aws", "sso", "login").start().waitFor()
                            true
                        } == true -> throw RetryableException()

                        authHandler.handleMfaChallenge(error) -> {
                            val mfaCode = authHandler.promptForMfaCode()
                            process.outputStream.write((mfaCode + "\n").toByteArray())
                            process.outputStream.flush()
                        }

                        else -> throw RuntimeException("Error getting authorization token: $error")
                    }
                }
                retryNeeded = false
            } catch (e: RetryableException) {
                // Continue the loop to retry
            }
        }

        if (result?.let { tokenRegexp.matches(it) } == false) {
            throw RuntimeException("Invalid authorization token received: $result")
        }

        return result
    }

    private fun buildCommand(config: CodeArtifactConfig): Array<String> {
        val region =
            if (config.region.isBlank() || config.region == "default") emptyArray() else arrayOf(
                "--region",
                config.region
            )
        val profile =
            if (config.profile.isBlank() || config.profile == "default") emptyArray() else arrayOf(
                "--profile",
                config.profile
            )
        return listOfNotNull(
            "aws",
            "codeartifact",
            "get-authorization-token",
            *profile,
            *region,
            "--domain",
            config.domain,
            "--domain-owner",
            config.domainOwner,
            "--query",
            "authorizationToken",
            "--output",
            "text"
        ).toTypedArray()
    }

    private class RetryableException : Exception()
}