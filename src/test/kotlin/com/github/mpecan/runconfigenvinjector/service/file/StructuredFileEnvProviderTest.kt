package com.github.mpecan.runconfigenvinjector.service.file

import com.github.mpecan.runconfigenvinjector.state.StructuredFileEnvProviderConfig
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StructuredFileEnvProviderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun getTempDir(): Path {
        return tempFolder.root.toPath()
    }

    @Test
    fun shouldParseEnvFileCorrectly() {
        val testFile = getTempDir().resolve("test.env")
        testFile.writeText(
            """
            # Comment line
            IGNORED_KEY=value1
            TEST_KEY=test value
            QUOTED_KEY="quoted value"
            SINGLE_QUOTED='single quoted'
            EMPTY_LINE=

            AFTER_EMPTY=value
        """.trimIndent()
        )

        val config = StructuredFileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "UTF-8",
            format = "ENV",
            key = "TEST_KEY"
        )

        val provider = StructuredFileEnvProvider(config)
        assertEquals("test value", provider.getValue()?.value)

        // Test quoted values
        assertEquals(
            "quoted value",
            StructuredFileEnvProvider(config.copy(key = "QUOTED_KEY")).getValue()?.value
        )
        assertEquals(
            "single quoted",
            StructuredFileEnvProvider(config.copy(key = "SINGLE_QUOTED")).getValue()?.value
        )
    }

    @Test
    fun shouldParseJsonFileCorrectly() {
        val testFile = getTempDir().resolve("test.json")
        testFile.writeText(
            """
            {
                "simple": "value",
                "nested": {
                    "key": "nested value"
                },
                "array": ["not", "supported"],
                "deep": {
                    "nested": {
                        "key": "deep nested value"
                    }
                }
            }
        """.trimIndent()
        )

        val config = StructuredFileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "UTF-8",
            format = "JSON",
            key = "simple"
        )

        val provider = StructuredFileEnvProvider(config)
        assertEquals("value", provider.getValue()?.value)

        // Test nested values
        assertEquals(
            "nested value",
            StructuredFileEnvProvider(config.copy(key = "nested.key")).getValue()?.value
        )
        assertEquals(
            "deep nested value",
            StructuredFileEnvProvider(config.copy(key = "deep.nested.key")).getValue()?.value
        )
    }

    @Test
    fun shouldParseYamlFileCorrectly() {
        val testFile = getTempDir().resolve("test.yaml")
        testFile.writeText(
            """
            simple: value
            nested:
              key: nested value
            deep:
              nested:
                key: deep nested value
            list:
              - item1
              - item2
        """.trimIndent()
        )

        val config = StructuredFileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "UTF-8",
            format = "YAML",
            key = "simple"
        )

        val provider = StructuredFileEnvProvider(config)
        assertEquals("value", provider.getValue()?.value)

        // Test nested values
        assertEquals(
            "nested value",
            StructuredFileEnvProvider(config.copy(key = "nested.key")).getValue()?.value
        )
        assertEquals(
            "deep nested value",
            StructuredFileEnvProvider(config.copy(key = "deep.nested.key")).getValue()?.value
        )
    }

    @Test
    fun shouldHandleInvalidFormat() {
        val testFile = getTempDir().resolve("test.txt")
        testFile.writeText("content")

        val config = StructuredFileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "UTF-8",
            format = "INVALID",
            key = "key"
        )

        val provider = StructuredFileEnvProvider(config)
        assertNull(provider.getValue())
    }

    @Test
    fun shouldHandleInvalidFileContent() {
        val testFile = getTempDir().resolve("test.json")
        testFile.writeText("invalid json content")

        val config = StructuredFileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "UTF-8",
            format = "JSON",
            key = "key"
        )

        val provider = StructuredFileEnvProvider(config)
        assertNull(provider.getValue())
    }

    @Test
    fun shouldHandleNonExistentKeys() {
        val testFile = getTempDir().resolve("test.json")
        testFile.writeText("""{"existing": "value"}""")

        val config = StructuredFileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "UTF-8",
            format = "JSON",
            key = "non.existent.key"
        )

        val provider = StructuredFileEnvProvider(config)
        assertNull(provider.getValue())
    }
}