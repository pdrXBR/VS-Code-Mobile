package com.vscodemobile

import android.content.Context
import kotlinx.coroutines.*
import java.io.*

class RunManager(private val context: Context) {

    private var process: Process? = null
    private var job: Job? = null

    fun executeCode(language: String, code: String, onOutput: (String) -> Unit) {
        stopExecution() // cancela anterior se houver

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Cria arquivo temporário com o código
                val tempFile = createTempFile(code, language)
                // Comando para executar (aqui usamos um script simulador)
                val command = buildCommand(language, tempFile.absolutePath)
                val processBuilder = ProcessBuilder(*command)
                processBuilder.redirectErrorStream(true)
                process = processBuilder.start()

                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    onOutput(line + "\n")
                }
                val exitCode = process!!.waitFor()
                onOutput("\nProcesso encerrado com código $exitCode\n")
            } catch (e: Exception) {
                onOutput("Erro: ${e.message}\n")
            }
        }
    }

    fun stopExecution() {
        job?.cancel()
        process?.destroy()
        process = null
    }

    private fun createTempFile(code: String, language: String): File {
        val ext = when (language) {
            "python" -> "py"
            "javascript" -> "js"
            "java" -> "java"
            "c" -> "c"
            "cpp" -> "cpp"
            "csharp" -> "cs"
            else -> "txt"
        }
        val tempFile = File(context.cacheDir, "temp_code_${System.currentTimeMillis()}.$ext")
        tempFile.writeText(code)
        return tempFile
    }

    private fun buildCommand(language: String, filePath: String): Array<String> {
        // Simulação: em vez de executar o interpretador real, apenas exibe o código
        return when (language) {
            "python" -> arrayOf("/system/bin/sh", "-c", "echo 'Simulando Python'; cat $filePath")
            "javascript" -> arrayOf("/system/bin/sh", "-c", "echo 'Simulando Node.js'; cat $filePath")
            "java" -> arrayOf("/system/bin/sh", "-c", "echo 'Simulando Java'; cat $filePath")
            else -> arrayOf("/system/bin/sh", "-c", "echo 'Linguagem não suportada'; cat $filePath")
        }
    }
}