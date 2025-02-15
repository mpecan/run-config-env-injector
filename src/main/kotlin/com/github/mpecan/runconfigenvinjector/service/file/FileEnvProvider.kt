package com.github.mpecan.runconfigenvinjector.service.file

import com.github.mpecan.runconfigenvinjector.service.CacheableValue
import com.github.mpecan.runconfigenvinjector.service.EnvProvider
import com.github.mpecan.runconfigenvinjector.service.SingleUseValue
import com.github.mpecan.runconfigenvinjector.state.FileEnvProviderConfig
import java.io.File
import java.nio.charset.Charset

class FileEnvProvider(private val config: FileEnvProviderConfig) :
    EnvProvider<FileEnvProviderConfig> {

    override fun getValue(): CacheableValue? {
        return try {
            SingleUseValue(File(config.filePath).readText(Charset.forName(config.encoding)).trim())
        } catch (e: Exception) {
            null
        }
    }

    override fun getCacheKey(): String {
        return config.filePath
    }
}