package com.example.model

enum class FileFormat(val label: String, val extension: String) {
  JSON("JSON Save", ".json"),
  XML("XML SharedPrefs", ".xml"),
  INI("INI Config", ".ini"),
  TXT("Plain Text Save", ".txt"),
  APK("Android Package", ".apk"),
  ENCRYPTED_OR_BINARY("Encrypted / Binary", ".dat"),
  UNKNOWN("Unknown Format", "")
}

enum class FieldType {
  INTEGER,
  FLOAT,
  STRING_NUMBER
}

enum class FieldCategory(val displayName: String, val icon: String) {
  COINS("Coins", "🪙"),
  GOLD("Gold", "👑"),
  DIAMONDS("Diamonds / Gems", "💎"),
  CASH("Cash / Money", "💵"),
  ENERGY("Energy / Stamina", "⚡"),
  LEVEL_XP("Level / XP", "⭐"),
  SCORE("Score", "🎯"),
  OTHER("Attribute", "⚙️")
}

data class CurrencyField(
  val path: String,
  val key: String,
  val currentValue: String,
  val numericValue: Long?,
  val type: FieldType = FieldType.INTEGER,
  val category: FieldCategory = FieldCategory.OTHER
)

data class FieldChange(
  val fieldKey: String,
  val path: String,
  val oldValue: String,
  val newValue: String
)

data class SaveFile(
  val id: String,
  val name: String,
  val sizeBytes: Long,
  val format: FileFormat,
  val content: String,
  val parsedFields: List<CurrencyField> = emptyList(),
  val isSupported: Boolean = true,
  val unsupportedReason: String? = null,
  val isModified: Boolean = false,
  val isSample: Boolean = false,
  val sourceUriString: String? = null
)

data class SaveOperation(
  val id: String,
  val fileName: String,
  val timestamp: Long,
  val command: String,
  val changes: List<FieldChange>,
  val backupContent: String
)

enum class MessageRole {
  USER,
  ASSISTANT,
  SYSTEM
}

data class ChatMessage(
  val id: String,
  val role: MessageRole,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val fieldChanges: List<FieldChange> = emptyList(),
  val isAiOnline: Boolean = false,
  val targetFileName: String? = null,
  val canApplyDirectly: Boolean = false,
  val pendingChanges: Map<String, Long> = emptyMap()
)

data class ApkMetadata(
  val packageName: String,
  val appName: String,
  val versionName: String,
  val versionCode: Long,
  val permissions: List<String>,
  val isOfflineGame: Boolean,
  val offlineConfidenceReason: String,
  val detectedConfigAssets: List<String>,
  val detectedGameEngines: List<String>,
  val warnings: List<String>,
  val fileSize: Long
)
