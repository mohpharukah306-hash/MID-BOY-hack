package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiSaveAssistant
import com.example.data.BackupManager
import com.example.data.SampleSaves
import com.example.model.ApkMetadata
import com.example.model.ChatMessage
import com.example.model.FieldChange
import com.example.model.FileFormat
import com.example.model.MessageRole
import com.example.model.SaveFile
import com.example.model.SaveOperation
import com.example.parser.ApkInspector
import com.example.parser.SaveParserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen {
  HOME,
  SCAN_ORB,
  CHAT,
  FILE_VIEWER,
  APK_INSPECTOR,
  HISTORY,
  SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val backupManager = BackupManager(application)
  private val geminiAssistant = GeminiSaveAssistant(
    apiKey = try {
      com.example.BuildConfig.GEMINI_API_KEY
    } catch (_: Throwable) {
      ""
    }
  )

  fun getApiKey(): String = geminiAssistant.apiKey
  fun getProxyUrl(): String = geminiAssistant.proxyUrl
  fun getModelName(): String = geminiAssistant.modelName

  private val _currentScreen = MutableStateFlow(Screen.HOME)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  private val _currentFile = MutableStateFlow<SaveFile?>(null)
  val currentFile: StateFlow<SaveFile?> = _currentFile.asStateFlow()

  private val _untouchedBackup = MutableStateFlow<SaveFile?>(null)
  val untouchedBackup: StateFlow<SaveFile?> = _untouchedBackup.asStateFlow()

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  private val _operationsHistory = MutableStateFlow<List<SaveOperation>>(emptyList())
  val operationsHistory: StateFlow<List<SaveOperation>> = _operationsHistory.asStateFlow()

  private val _apkMetadata = MutableStateFlow<ApkMetadata?>(null)
  val apkMetadata: StateFlow<ApkMetadata?> = _apkMetadata.asStateFlow()

  private val _isAnalyzing = MutableStateFlow(false)
  val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

  private val _isAiThinking = MutableStateFlow(false)
  val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

  private val _statusNotification = MutableStateFlow<String?>(null)
  val statusNotification: StateFlow<String?> = _statusNotification.asStateFlow()

  init {
    // Initial welcome message from MID GAME HACKER AI
    val welcome = ChatMessage(
      id = UUID.randomUUID().toString(),
      role = MessageRole.ASSISTANT,
      text = "👾 **MID GAME HACKER AI SYSTEM ONLINE**\n\n" +
        "Import an offline game save (JSON, XML, INI, TXT) or APK to begin.\n" +
        "You can type natural language instructions like:\n" +
        " • *'Give me unlimited coins'*\n" +
        " • *'Set coins to 999999999'*\n" +
        " • *'Give me 500000 diamonds'*\n" +
        " • *'Analyze my game save'*\n\n" +
        "🛡️ Automatic untouched backups are created before every modification.",
      timestamp = System.currentTimeMillis()
    )
    _chatMessages.value = listOf(welcome)

    // Pre-populate with first sample for immediate testing convenience!
    loadSample(SampleSaves.getSamples().first())
  }

  fun setScreen(screen: Screen) {
    _currentScreen.value = screen
  }

  fun clearNotification() {
    _statusNotification.value = null
  }

  fun showNotification(message: String) {
    _statusNotification.value = message
  }

  fun loadSample(sample: SaveFile) {
    _currentFile.value = sample
    _untouchedBackup.value = sample
    backupManager.createBackup(sample)
    showNotification("Loaded test save: ${sample.name} (${sample.parsedFields.size} currencies)")
  }

  fun importFileFromUri(uri: Uri, fileName: String, fileSize: Long, rawBytes: ByteArray) {
    viewModelScope.launch {
      _isAnalyzing.value = true
      try {
        if (fileName.lowercase().endsWith(".apk")) {
          // Inspect APK
          val metadata = getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
            ApkInspector.inspectApk(stream, fileSize, fileName)
          }
          _apkMetadata.value = metadata
          val parsedFile = SaveParserManager.parseFile(fileName, "", rawBytes, uri.toString())
          _currentFile.value = parsedFile
          _currentScreen.value = Screen.APK_INSPECTOR
          showNotification("APK Inspected: ${metadata?.packageName ?: fileName}")
        } else {
          val content = String(rawBytes, Charsets.UTF_8)
          val parsed = SaveParserManager.parseFile(fileName, content, rawBytes, uri.toString())
          _currentFile.value = parsed
          _untouchedBackup.value = parsed
          backupManager.createBackup(parsed)

          if (parsed.isSupported) {
            showNotification("Imported ${parsed.name} • ${parsed.parsedFields.size} currency fields identified")
            // Add automated scan message to chat
            val scanMsg = ChatMessage(
              id = UUID.randomUUID().toString(),
              role = MessageRole.SYSTEM,
              text = "📂 **File Imported**: `${parsed.name}` (${parsed.format.label}, ${parsed.sizeBytes} bytes)\n" +
                "Detected candidate fields: " +
                if (parsed.parsedFields.isEmpty()) "None detected"
                else parsed.parsedFields.joinToString(", ") { "${it.category.icon} ${it.key} (${it.currentValue})" },
              targetFileName = parsed.name
            )
            _chatMessages.value = _chatMessages.value + scanMsg
          } else {
            showNotification("File imported (${parsed.format.label}): Not directly editable")
          }
        }
      } catch (e: Exception) {
        showNotification("Failed to read file: ${e.message}")
      } finally {
        _isAnalyzing.value = false
      }
    }
  }

  fun sendNaturalLanguageCommand(command: String) {
    if (command.isBlank()) return

    viewModelScope.launch {
      _isAiThinking.value = true
      try {
        val file = _currentFile.value
        val response = geminiAssistant.processUserCommand(command, file)
        _chatMessages.value = geminiAssistant.getHistory()

        if (response.fieldModifications.isNotEmpty() && file != null && file.isSupported) {
          // Automatically apply or prepare modification
          applyModifications(response.fieldModifications, command)
        }
      } catch (e: Exception) {
        showNotification("Command processing note: ${e.message}")
      } finally {
        _isAiThinking.value = false
      }
    }
  }

  fun applyModifications(modifications: Map<String, Long>, command: String) {
    val file = _currentFile.value ?: return
    if (!file.isSupported) return

    val backup = _untouchedBackup.value ?: file
    val originalContent = file.content

    val (updatedFile, changes) = SaveParserManager.applyModifications(file, modifications)
    _currentFile.value = updatedFile

    if (changes.isNotEmpty()) {
      val op = backupManager.recordOperation(updatedFile, command, changes, originalContent)
      _operationsHistory.value = backupManager.getHistory()

      val confirmationMsg = ChatMessage(
        id = UUID.randomUUID().toString(),
        role = MessageRole.ASSISTANT,
        text = "⚡ **Save Data Modified Successfully**\n" +
          changes.joinToString("\n") { " • ${it.fieldKey}: `${it.oldValue}` ➜ `${it.newValue}`" } +
          "\n\n🛡️ Untouched backup retained. You can export the modified save or restore the backup at any time.",
        fieldChanges = changes,
        targetFileName = updatedFile.name
      )
      _chatMessages.value = _chatMessages.value + confirmationMsg
      showNotification("Modified ${changes.size} fields in ${updatedFile.name}")
    }
  }

  fun restoreOriginalBackup() {
    val backup = _untouchedBackup.value ?: return
    _currentFile.value = backup
    showNotification("Restored original untouched backup for ${backup.name}")
    val restoreMsg = ChatMessage(
      id = UUID.randomUUID().toString(),
      role = MessageRole.SYSTEM,
      text = "🔄 **Backup Restored**: Reverted `${backup.name}` back to original unmodified state.",
      targetFileName = backup.name
    )
    _chatMessages.value = _chatMessages.value + restoreMsg
  }

  fun restoreFromHistory(operation: SaveOperation) {
    val restored = SaveParserManager.parseFile(operation.fileName, operation.backupContent)
    _currentFile.value = restored
    showNotification("Restored save to state before: '${operation.command}'")
  }

  fun exportModifiedFile(targetUri: Uri) {
    val file = _currentFile.value ?: return
    val success = backupManager.exportToFile(targetUri, file.content)
    if (success) {
      showNotification("Exported modified ${file.name} to storage!")
      val exportMsg = ChatMessage(
        id = UUID.randomUUID().toString(),
        role = MessageRole.SYSTEM,
        text = "💾 **File Export Complete**: Saved verified modified save file `${file.name}` via Storage Access Framework.",
        targetFileName = file.name
      )
      _chatMessages.value = _chatMessages.value + exportMsg
    } else {
      showNotification("Failed to export file")
    }
  }

  fun updateSettings(proxyUrl: String, apiKey: String, model: String) {
    geminiAssistant.proxyUrl = proxyUrl
    geminiAssistant.apiKey = apiKey
    geminiAssistant.modelName = model
    showNotification("API & Proxy settings updated ($model)")
  }

  fun triggerOrbScan() {
    val file = _currentFile.value
    if (file == null) {
      showNotification("Please load a file first")
      return
    }
    setScreen(Screen.SCAN_ORB)
  }
}
