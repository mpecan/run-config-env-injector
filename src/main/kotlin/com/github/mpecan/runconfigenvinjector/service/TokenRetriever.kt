package com.github.mpecan.runconfigenvinjector.service

import com.github.mpecan.runconfigenvinjector.state.CodeArtifactConfig

interface TokenRetriever {
    fun getAuthToken(config: CodeArtifactConfig): CacheableValue?
}