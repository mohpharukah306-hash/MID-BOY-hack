package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.FieldChange
import com.example.model.SaveFile
import com.example.model.SaveOperation
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class BackupManager(private val context: Context) {

  private val backupDir: File by lazy {
    File(context.filesDir, "game_backups").apply {
      if (!exists()) mkdirs()
    }
  }

  private val historyList = mutableListOf<SaveOperation>()

  fun getHistory(): List<SaveOperation> = historyList.toList()

  fun createBackup(saveFile: SaveFile): File {
    val time = System.currentTimeMillis()
    val cleanName = saveFile.name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    val backupFile = File(backupDir, "backup_${time}_$cleanName")
    backupFile.writeText(saveFile.content)
    return backupFile
  }

  fun recordOperation(
    saveFile: SaveFile,
    command: String,
    changes: List<FieldChange>,
    originalContent: String
  ): SaveOperation {
    val op = SaveOperation(
      id = UUID.randomUUID().toString(),
      fileName = saveFile.name,
      timestamp = System.currentTimeMillis(),
      command = command,
      changes = changes,
      backupContent = originalContent
    )
    historyList.add(0, op)
    return op
  }

  fun exportToFile(uri: Uri, content: String): Boolean {
    return try {
      context.contentResolver.openOutputStream(uri)?.use { os ->
        os.write(content.toByteArray(Charsets.UTF_8))
        os.flush()
      }
      true
    } catch (_: Exception) {
      false
    }
  }
}
