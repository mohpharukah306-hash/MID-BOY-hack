package com.example.ai

import com.example.model.CurrencyField
import com.example.model.SaveFile
import java.util.Locale

data class IntentResult(
  val explanation: String,
  val fieldModifications: Map<String, Long>,
  val isAnalysisOnly: Boolean = false,
  val confidence: Float = 1.0f
)

object LocalIntentEngine {

  fun interpretCommand(command: String, currentFile: SaveFile?): IntentResult {
    val lower = command.lowercase(Locale.ROOT).trim()

    if (currentFile == null) {
      return IntentResult(
        explanation = "⚠️ No game save file is currently loaded. Please import a file (JSON, XML, INI, TXT) or tap a Quick Action sample above to begin hacking.",
        fieldModifications = emptyMap(),
        isAnalysisOnly = true
      )
    }

    if (!currentFile.isSupported) {
      return IntentResult(
        explanation = "⚠️ This file format (${currentFile.format.label}) is not editable: ${currentFile.unsupportedReason}",
        fieldModifications = emptyMap(),
        isAnalysisOnly = true
      )
    }

    // 1. Analyze command
    if (lower.contains("analyze") || lower.contains("scan") || lower.contains("inspect") || lower.contains("what can i change") || lower.contains("show fields")) {
      val fieldsSummary = if (currentFile.parsedFields.isEmpty()) {
        "No obvious numeric currency keys found in this file. You may manually edit raw text in the File Viewer."
      } else {
        "Discovered ${currentFile.parsedFields.size} candidate fields:\n" +
          currentFile.parsedFields.joinToString("\n") { " • ${it.category.icon} ${it.key}: ${it.currentValue} (${it.category.displayName})" }
      }

      return IntentResult(
        explanation = "🔍 **Save File Analysis Completed** (${currentFile.name})\n\n" +
          "• Format: ${currentFile.format.label}\n" +
          "• File Size: ${currentFile.sizeBytes} bytes\n" +
          "• Total Currencies Found: ${currentFile.parsedFields.size}\n\n" +
          fieldsSummary + "\n\n" +
          "💡 *You can type 'Give me unlimited coins' or 'Set coins to 999999999' to modify them.*",
        fieldModifications = emptyMap(),
        isAnalysisOnly = true
      )
    }

    // 2. Max All / Unlimited Everything
    if ((lower.contains("unlimited") || lower.contains("max")) && (lower.contains("all") || lower.contains("everything") || (!lower.contains("coin") && !lower.contains("gold") && !lower.contains("diamond") && !lower.contains("gem") && !lower.contains("cash")))) {
      val mods = mutableMapOf<String, Long>()
      currentFile.parsedFields.forEach { field ->
        mods[field.path] = 999999999L
      }

      val fieldsList = currentFile.parsedFields.joinToString(", ") { "${it.category.icon} ${it.key}" }
      return IntentResult(
        explanation = "⚡ **Maximum Overflow Protocol Triggered**\n\n" +
          "Identified all ${currentFile.parsedFields.size} candidate currencies: $fieldsList.\n" +
          "Target value set to **999,999,999** for each field.",
        fieldModifications = mods
      )
    }

    // 3. Extract target amount
    val targetAmount: Long = when {
      lower.contains("unlimited") || lower.contains("infinite") || lower.contains("max") -> 999999999L
      else -> {
        val numberRegex = Regex("""\b(\d+)\b""")
        val matches = numberRegex.findAll(lower).toList()
        matches.lastOrNull()?.groupValues?.get(1)?.toLongOrNull() ?: 999999999L
      }
    }

    // 4. Match specific fields
    val matchedModifications = mutableMapOf<String, Long>()
    val matchedFieldNames = mutableListOf<String>()

    val keywords = listOf(
      "coin" to listOf("coin", "coins", "silver", "credits"),
      "gold" to listOf("gold", "gold_bars", "goldbars"),
      "diamond" to listOf("diamond", "diamonds", "gem", "gems", "ruby", "rubies", "crystal"),
      "cash" to listOf("cash", "money", "bucks", "dollar", "wallet"),
      "energy" to listOf("energy", "stamina", "mana", "fuel", "hp", "health", "lives"),
      "level" to listOf("level", "lvl", "xp", "exp", "experience", "rank"),
      "score" to listOf("score", "highscore", "points", "stars")
    )

    for ((category, words) in keywords) {
      if (words.any { lower.contains(it) }) {
        // Find fields in current file matching this category
        val matchingFields = currentFile.parsedFields.filter { field ->
          val fLower = field.key.lowercase(Locale.ROOT)
          words.any { fLower.contains(it) } || field.category.name.lowercase().contains(category)
        }

        matchingFields.forEach { field ->
          matchedModifications[field.path] = targetAmount
          matchedFieldNames.add("${field.category.icon} ${field.key} (${field.currentValue} ➜ ${formatNumber(targetAmount)})")
        }
      }
    }

    // If no keyword matched, but user typed a number and we have candidate fields
    if (matchedModifications.isEmpty() && currentFile.parsedFields.isNotEmpty()) {
      // Pick first currency or coin field
      val primaryField = currentFile.parsedFields.firstOrNull { it.category.name.contains("COIN") || it.category.name.contains("GOLD") }
        ?: currentFile.parsedFields.first()

      matchedModifications[primaryField.path] = targetAmount
      matchedFieldNames.add("${primaryField.category.icon} ${primaryField.key} (${primaryField.currentValue} ➜ ${formatNumber(targetAmount)})")
    }

    if (matchedModifications.isEmpty()) {
      return IntentResult(
        explanation = "⚠️ Could not identify specific matching currency in the prompt '$command'. Available fields in ${currentFile.name}:\n" +
          currentFile.parsedFields.joinToString(", ") { it.key },
        fieldModifications = emptyMap(),
        isAnalysisOnly = true
      )
    }

    val explanation = "👾 **Target Parameters Verified**\n\n" +
      "Identified candidate targets for modification:\n" +
      matchedFieldNames.joinToString("\n") + "\n\n" +
      "🛡️ Untouched original backup is ready. Tap below to write modifications to the save data."

    return IntentResult(
      explanation = explanation,
      fieldModifications = matchedModifications
    )
  }

  private fun formatNumber(number: Long): String {
    return String.format(Locale.US, "%,d", number)
  }
}
