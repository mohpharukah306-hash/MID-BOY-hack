package com.example.parser

import com.example.model.CurrencyField
import com.example.model.FieldChange
import com.example.model.FileFormat
import com.example.model.SaveFile
import java.util.UUID

object SaveParserManager {

  fun parseFile(fileName: String, content: String, rawBytes: ByteArray? = null, sourceUriString: String? = null): SaveFile {
    val sizeBytes = rawBytes?.size?.toLong() ?: content.toByteArray().size.toLong()
    val lowerName = fileName.lowercase()

    // Check for APK
    if (lowerName.endsWith(".apk")) {
      return SaveFile(
        id = UUID.randomUUID().toString(),
        name = fileName,
        sizeBytes = sizeBytes,
        format = FileFormat.APK,
        content = "Android Package Archive ($sizeBytes bytes)",
        isSupported = false,
        unsupportedReason = "APK is an application installer containing compiled code, assets, and digital signatures. It is not a runtime save file. Use 'Inspect APK' to check offline balance data, permissions, and engine security.",
        sourceUriString = sourceUriString
      )
    }

    // Check for SQLite database magic header: "SQLite format 3"
    if (content.startsWith("SQLite format 3") || (rawBytes != null && rawBytes.take(15).toByteArray().decodeToString().startsWith("SQLite format 3"))) {
      return SaveFile(
        id = UUID.randomUUID().toString(),
        name = fileName,
        sizeBytes = sizeBytes,
        format = FileFormat.ENCRYPTED_OR_BINARY,
        content = "[Binary SQLite Database Header: SQLite format 3]",
        isSupported = false,
        unsupportedReason = "Binary SQLite Database detected. Modifying raw database tables without schema transaction integrity risks corrupting game progress. Please extract the JSON/XML export or SharedPrefs from your device.",
        sourceUriString = sourceUriString
      )
    }

    // Check for non-text binary data
    if (isBinaryData(content, rawBytes)) {
      return SaveFile(
        id = UUID.randomUUID().toString(),
        name = fileName,
        sizeBytes = sizeBytes,
        format = FileFormat.ENCRYPTED_OR_BINARY,
        content = "[Encrypted or Binary Stream - ${sizeBytes} bytes]",
        isSupported = false,
        unsupportedReason = "Encrypted or proprietary binary save format. The file does not contain plain text keys or values. For your security and to prevent save corruption, MID GAME HACKER only modifies verified open text structures (JSON, XML, INI, TXT).",
        sourceUriString = sourceUriString
      )
    }

    // Detect format
    val format = detectFormat(fileName, content)
    val parsedFields: List<CurrencyField> = when (format) {
      FileFormat.JSON -> JsonSaveParser.extractFields(content)
      FileFormat.XML -> XmlSaveParser.extractFields(content)
      FileFormat.INI -> IniSaveParser.extractFields(content)
      FileFormat.TXT -> TextSaveParser.extractFields(content)
      else -> emptyList()
    }

    return SaveFile(
      id = UUID.randomUUID().toString(),
      name = fileName,
      sizeBytes = sizeBytes,
      format = format,
      content = content,
      parsedFields = parsedFields,
      isSupported = true,
      unsupportedReason = null,
      sourceUriString = sourceUriString
    )
  }

  fun detectFormat(fileName: String, content: String): FileFormat {
    val lower = fileName.lowercase()
    val trimmed = content.trim()

    return when {
      lower.endsWith(".json") || trimmed.startsWith("{") || trimmed.startsWith("[") -> FileFormat.JSON
      lower.endsWith(".xml") || trimmed.startsWith("<?xml") || trimmed.startsWith("<map") || (trimmed.startsWith("<") && trimmed.contains(">")) -> FileFormat.XML
      lower.endsWith(".ini") || lower.endsWith(".cfg") || (trimmed.contains("[") && trimmed.contains("]") && trimmed.contains("=")) -> FileFormat.INI
      lower.endsWith(".txt") || lower.endsWith(".dat") || lower.endsWith(".sav") || lower.endsWith(".save") -> {
        // Test if it's JSON or XML first
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) FileFormat.JSON
        else if (trimmed.startsWith("<")) FileFormat.XML
        else if (trimmed.contains("[") && trimmed.contains("]")) FileFormat.INI
        else FileFormat.TXT
      }
      else -> FileFormat.TXT
    }
  }

  private fun isBinaryData(content: String, rawBytes: ByteArray?): Boolean {
    val sample = rawBytes?.take(512)?.toByteArray() ?: content.take(512).toByteArray()
    if (sample.isEmpty()) return false

    var nullBytes = 0
    var nonPrintable = 0
    for (b in sample) {
      val ub = b.toInt() and 0xFF
      if (ub == 0) nullBytes++
      if (ub < 9 || (ub in 14..31)) nonPrintable++
    }

    // If more than 10% non-printable or any null byte in text, consider binary
    return nullBytes > 0 || (nonPrintable.toDouble() / sample.size > 0.15)
  }

  fun applyModifications(file: SaveFile, modifications: Map<String, Long>): Pair<SaveFile, List<FieldChange>> {
    if (!file.isSupported || modifications.isEmpty()) {
      return Pair(file, emptyList())
    }

    val (modifiedContent, changes) = when (file.format) {
      FileFormat.JSON -> JsonSaveParser.applyModifications(file.content, modifications)
      FileFormat.XML -> XmlSaveParser.applyModifications(file.content, modifications)
      FileFormat.INI -> IniSaveParser.applyModifications(file.content, modifications)
      FileFormat.TXT -> TextSaveParser.applyModifications(file.content, modifications)
      else -> Pair(file.content, emptyList())
    }

    // Re-extract updated fields
    val updatedFields = when (file.format) {
      FileFormat.JSON -> JsonSaveParser.extractFields(modifiedContent)
      FileFormat.XML -> XmlSaveParser.extractFields(modifiedContent)
      FileFormat.INI -> IniSaveParser.extractFields(modifiedContent)
      FileFormat.TXT -> TextSaveParser.extractFields(modifiedContent)
      else -> file.parsedFields
    }

    val updatedFile = file.copy(
      content = modifiedContent,
      sizeBytes = modifiedContent.toByteArray().size.toLong(),
      parsedFields = updatedFields,
      isModified = changes.isNotEmpty()
    )

    return Pair(updatedFile, changes)
  }
}
