package com.github.mpecan.runconfigenvinjector.service.file

import com.github.mpecan.runconfigenvinjector.state.FileEnvProviderConfig
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileEnvProviderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun getTempDir(): Path {
        return tempFolder.root.toPath()
    }

    @Test
    fun shouldReadFileContentSuccessfully() {
        val testFile = getTempDir().resolve("test.txt")
        testFile.writeText("test content")

        val config = FileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "UTF-8"
        )

        val provider = FileEnvProvider(config)
        val result = provider.getValue()

        assertEquals("test content", result?.value)
    }

    @Test
    fun shouldHandleNonExistentFile() {
        val config = FileEnvProviderConfig(
            enabled = true,
            filePath = getTempDir().resolve("nonexistent.txt").toString(),
            encoding = "UTF-8"
        )

        val provider = FileEnvProvider(config)
        val result = provider.getValue()

        assertNull(result)
    }

    @Test
    fun shouldHandleInvalidEncoding() {
        val testFile = getTempDir().resolve("test.txt")
        testFile.writeText("test content")

        val config = FileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "INVALID-ENCODING"
        )

        val provider = FileEnvProvider(config)
        val result = provider.getValue()

        assertNull(result)
    }

    @Test
    fun shouldTrimWhitespaceFromContent() {
        val testFile = getTempDir().resolve("test.txt")
        testFile.writeText("\n  test content  \n\t")

        val config = FileEnvProviderConfig(
            enabled = true,
            filePath = testFile.toString(),
            encoding = "UTF-8"
        )

        val provider = FileEnvProvider(config)
        val result = provider.getValue()

        assertEquals("test content", result?.value)
    }
}