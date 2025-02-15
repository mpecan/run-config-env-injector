package com.github.mpecan.runconfigenvinjector.service

import com.intellij.openapi.diagnostic.Logger

class ProcessExecutor {
    private val logger = Logger.getInstance(ProcessExecutor::class.java)

    data class ProcessResult(
        val output: String,
        val error: String
    )

    fun execute(
        command: Array<String>,
        errorHandler: (Process, String) -> Unit = { _, _ -> }
    ): String {
        val process = Runtime.getRuntime().exec(command)
        val result = collectProcessOutput(process)
        
        if (result.error.isNotEmpty()) {
            errorHandler(process, result.error)
        }

        val output = result.output.trim()
        if (output.isEmpty()) {
            throw RuntimeException("No output received from command")
        }

        return output
    }

    private fun collectProcessOutput(process: Process): ProcessResult {
        val outputLines = StringBuilder()
        val errorLines = StringBuilder()

        process.inputStream.bufferedReader().use { input ->
            process.errorStream.bufferedReader().use { error ->
                val inputThread = Thread {
                    input.forEachLine {
                        outputLines.append(it).append("\n")
                        logger.debug("Command output: $it")
                    }
                }
                val errorThread = Thread {
                    error.forEachLine {
                        errorLines.append(it).append("\n")
                        logger.debug("Command error: $it")
                    }
                }

                inputThread.start()
                errorThread.start()
                inputThread.join()
                errorThread.join()
            }
        }

        return ProcessResult(
            output = outputLines.toString().trim(),
            error = errorLines.toString().trim()
        )
    }
}