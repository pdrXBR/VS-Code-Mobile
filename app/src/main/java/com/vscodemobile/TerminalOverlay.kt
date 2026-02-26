package com.vscodemobile

import android.content.Context
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class TerminalOverlay(private val context: Context) {

    private lateinit var terminalText: TextView
    private lateinit var scrollView: ScrollView
    private var job: Job? = null

    fun attachToContainer(container: FrameLayout) {
        // Inflar ou criar a view do terminal
        scrollView = ScrollView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        terminalText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
            setTextColor(ContextCompat.getColor(context, R.color.terminalText))
            setBackgroundColor(ContextCompat.getColor(context, R.color.terminalBackground))
        }
        scrollView.addView(terminalText)
        container.removeAllViews()
        container.addView(scrollView)
    }

    fun appendOutput(text: String) {
        terminalText.append(text)
        // Scroll para o final
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    fun clear() {
        terminalText.text = ""
    }

    fun simulateCommand(command: String) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            // Simula execução de comando
            appendOutput("$ $command\n")
            delay(500)
            appendOutput("Resultado simulado do comando: $command\n")
            appendOutput("> ")
        }
    }

    fun stop() {
        job?.cancel()
    }
}