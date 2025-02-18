package com.github.mpecan.runconfigenvinjector.config

import com.github.mpecan.runconfigenvinjector.state.EnvProviderConfig

interface MessageDisplay {
    fun showError(message: String, title: String)
    fun showInfo(message: String, title: String)
}

interface ProviderTester {
    fun testProvider(config: EnvProviderConfig): Result<Unit>
}