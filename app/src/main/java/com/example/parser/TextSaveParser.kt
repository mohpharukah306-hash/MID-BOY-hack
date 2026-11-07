package com.example.parser

import com.example.model.CurrencyField
import com.example.model.FieldChange

object TextSaveParser {

  private val PAIR_REGEX = Regex("""^([^=:\s]+)\s*[:=]\s*(.*)$""")

  fun extractFields(content: String): List<CurrencyField> {
    val results = mutableListOf<CurrencyField>()
    content.lineSequence().forEach { line ->
      val trimmed = line.trim()
      val match = PAIR_REGEX.find(trimmed)
      if (match != null) {
        val key = match.groupValues[1].trim()
        val value = match.groupValues[2].trim()
        if (CurrencyDetector.isCandidateKey(key)) {
          val (numVal, type) = CurrencyDetector.parseNumericValue(value)
          results.add(
            CurrencyField(
              path = key,
              key = key,
              currentValue = value,
              numericValue = numVal,
              type = type,
              category = CurrencyDetector.categorizeKey(key)
            )
          )
        }
      }
    }
    return results
  }

  fun applyModifications(content: String, modifications: Map<String, Long>): Pair<String, List<FieldChange>> {
    val changes = mutableListOf<FieldChange>()
    val output = mutableListOf<String>()

    content.lineSequence().forEach { rawLine ->
      val trimmed = rawLine.trim()
      val match = PAIR_REGEX.find(trimmed)
      if (match != null) {
        val key = match.groupValues[1].trim()
        val value = match.groupValues[2].trim()
        val targetVal = modifications[key] ?: modifications[key.lowercase()]
        if (targetVal != null) {
          val sep = if (trimmed.contains("=")) "=" else ":"
          output.add("$key$sep $targetVal")
          changes.add(FieldChange(key, key, value, targetVal.toString()))
        } else {
          output.add(rawLine)
        }
      } else {
        output.add(rawLine)
      }
    }

    return Pair(output.joinToString("\n"), changes)
  }
}
