package com.github.mpecan.runconfigenvinjector.service.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.github.mpecan.runconfigenvinjector.service.CacheableValue
import com.github.mpecan.runconfigenvinjector.service.EnvProvider
import com.github.mpecan.runconfigenvinjector.service.SingleUseValue
import com.github.mpecan.runconfigenvinjector.state.StructuredFileEnvProviderConfig
import java.io.File
import java.nio.charset.Charset

class StructuredFileEnvProvider(private val config: StructuredFileEnvProviderConfig) :
    EnvProvider<StructuredFileEnvProviderConfig> {
    override fun getValue(): CacheableValue? {
        return try {
            val content = File(config.filePath).readText(Charset.forName(config.encoding))
            when (config.format.uppercase()) {
                "ENV" -> parseEnvFile(content, config.key)
                "JSON" -> parseJsonFile(content, config.key)
                "YAML" -> parseYamlFile(content, config.key)
                else -> null
            }?.let { SingleUseValue(it) }
        } catch (e: Exception) {
            null
        }
    }

    override fun getCacheKey(): String {
        return config.filePath + config.key
    }

    private fun parseEnvFile(content: String, key: String): String? {
        return content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .map { it.split("=", limit = 2) }
            .firstOrNull { it.size == 2 && it[0].trim() == key }
            ?.get(1)?.trim()
            ?.removeSurrounding("\"")
            ?.removeSurrounding("'")
    }

    private fun parseJsonFile(content: String, key: String): String? {
        return try {
            ObjectMapper().readTree(content).at("/${key.replace(".", "/")}").asText(null)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseYamlFile(content: String, key: String): String? {
        return try {
            ObjectMapper(YAMLFactory()).readTree(content).at("/${key.replace(".", "/")}").asText(null)
        } catch (e: Exception) {
            null
        }
    }
}