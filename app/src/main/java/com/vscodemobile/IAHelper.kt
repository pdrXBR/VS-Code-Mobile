package com.vscodemobile

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*

class IAHelper {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())

    fun getSuggestion(codeSnippet: String, callback: (String) -> Unit) {
        scope.launch {
            delay(1000) // Simula processamento
            val suggestion = generateSuggestion(codeSnippet)
            mainHandler.post { callback(suggestion) }
        }
    }

    private fun generateSuggestion(codeSnippet: String): String {
        return when {
            codeSnippet.contains("fun ") || codeSnippet.contains("def ") ->
                "Considere adicionar uma docstring explicando o propósito da função."
            codeSnippet.contains("for ") ->
                "Talvez você possa usar compreensão de lista para tornar o código mais conciso."
            codeSnippet.contains("if ") ->
                "Verifique se todos os casos estão cobertos."
            else -> "Código parece ok. Continue assim!"
        }
    }
}