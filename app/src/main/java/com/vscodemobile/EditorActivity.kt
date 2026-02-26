package com.vscodemobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.vscodemobile.databinding.ActivityEditorBinding
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.widget.CodeEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditorBinding
    private lateinit var editor: CodeEditor
    private lateinit var fileExplorer: FileExplorer
    private lateinit var fileExplorerAdapter: FileExplorer.FileAdapter
    private lateinit var terminalOverlay: TerminalOverlay
    private val runManager = RunManager(this)
    private val iaHelper = IAHelper()
    private var currentFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        setupEditor()
        setupMenu()
        setupFileExplorer()
        setupTerminal()

        checkPermissions()
        loadLastProject()
    }

    private fun setupEditor() {
        editor = binding.editor
        lifecycleScope.launch {
            try {
                val scheme = TextMateColorScheme.create(assets.open("textmate/quietlight.json"))
                editor.colorScheme = scheme
            } catch (e: Exception) {
                // Fallback para tema padrão
            }
        }
        editor.setTabWidth(4)
        editor.typefaceText = resources.getFont(R.font.jetbrains_mono)
    }

    private fun setupMenu() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_run -> {
                    runCode()
                    true
                }
                R.id.action_ai -> {
                    showAISuggestion()
                    true
                }
                R.id.action_explorer -> {
                    toggleFileExplorer()
                    true
                }
                R.id.action_terminal -> {
                    toggleTerminal()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFileExplorer() {
        fileExplorer = FileExplorer(this) { file ->
            if (file.isDirectory) {
                loadDirectory(file)
            } else {
                loadFile(file)
                toggleFileExplorer() // opcional: fechar após abrir arquivo
            }
        }
        binding.fileRecyclerView.layoutManager = LinearLayoutManager(this)
        loadDirectory(File("/storage/emulated/0"))
    }

    private fun loadDirectory(directory: File) {
        val files = fileExplorer.loadFiles(directory.absolutePath)
        fileExplorerAdapter = fileExplorer.FileAdapter(files)
        binding.fileRecyclerView.adapter = fileExplorerAdapter
        binding.currentPath.text = directory.absolutePath
    }

    private fun setupTerminal() {
        terminalOverlay = TerminalOverlay(this)
    }

    private fun toggleFileExplorer() {
        if (binding.fileExplorerContainer.visibility == android.view.View.VISIBLE) {
            binding.fileExplorerContainer.visibility = android.view.View.GONE
        } else {
            binding.fileExplorerContainer.visibility = android.view.View.VISIBLE
        }
    }

    private fun toggleTerminal() {
        if (binding.terminalContainer.visibility == android.view.View.VISIBLE) {
            binding.terminalContainer.visibility = android.view.View.GONE
        } else {
            binding.terminalContainer.visibility = android.view.View.VISIBLE
            terminalOverlay.attachToContainer(binding.terminalContainer)
        }
    }

    private fun loadFile(file: File) {
        currentFile = file
        lifecycleScope.launch(Dispatchers.IO) {
            val content = file.readText()
            withContext(Dispatchers.Main) {
                editor.setText(content, null)
                // Salvar último arquivo
                getSharedPreferences("prefs", MODE_PRIVATE).edit()
                    .putString("last_file", file.absolutePath).apply()
            }
        }
    }

    private fun loadLastProject() {
        val lastFile = getSharedPreferences("prefs", MODE_PRIVATE).getString("last_file", null)
        if (lastFile != null) {
            val file = File(lastFile)
            if (file.exists()) loadFile(file)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }

    private fun runCode() {
        val code = editor.text.toString()
        val language = detectLanguage()
        terminalOverlay.simulateCommand("run $language code") // apenas simulação
        runManager.executeCode(language, code) { output ->
            runOnUiThread {
                terminalOverlay.appendOutput(output)
            }
        }
    }

    private fun showAISuggestion() {
        val cursor = editor.cursor
        val start = cursor.left() ?: return
        val end = cursor.right() ?: return
        val selectedText = editor.text.subSequence(start, end - start).toString()
        if (selectedText.isBlank()) {
            Toast.makeText(this, "Selecione um trecho de código", Toast.LENGTH_SHORT).show()
            return
        }
        iaHelper.getSuggestion(selectedText) { suggestion ->
            runOnUiThread {
                Toast.makeText(this, suggestion, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun detectLanguage(): String {
        return currentFile?.extension?.let {
            when (it) {
                "py" -> "python"
                "js" -> "javascript"
                "java" -> "java"
                "c" -> "c"
                "cpp" -> "cpp"
                "cs" -> "csharp"
                else -> "text"
            }
        } ?: "text"
    }
}