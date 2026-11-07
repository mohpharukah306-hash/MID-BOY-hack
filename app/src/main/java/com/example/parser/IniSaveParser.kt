package com.example.parser

import com.example.model.CurrencyField
import com.example.model.FieldChange

object IniSaveParser {

  private val KEY_VALUE_REGEX = Regex("""^([^=:#;]+)[=:](.*)$""")

  fun extractFields(content: String): List<CurrencyField> {
    val results = mutableListOf<CurrencyField>()
    var currentSection = ""

    content.lineSequence().forEach { rawLine ->
      val line = rawLine.trim()
      if (line.startsWith("[") && line.endsWith("]")) {
        currentSection = line.substring(1, line.length - 1).trim()
      } else if (!line.startsWith(";") && !line.startsWith("#")) {
        val match = KEY_VALUE_REGEX.find(line)
        if (match != null) {
          val key = match.groupValues[1].trim()
          val value = match.groupValues[2].trim()
          val fullPath = if (currentSection.isNotEmpty()) "$currentSection.$key" else key

          if (CurrencyDetector.isCandidateKey(key)) {
            val (numVal, type) = CurrencyDetector.parseNumericValue(value)
            results.add(
              CurrencyField(
                path = fullPath,
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
    }
    return results
  }

  fun applyModifications(content: String, modifications: Map<String, Long>): Pair<String, List<FieldChange>> {
    val changes = mutableListOf<FieldChange>()
    var currentSection = ""
    val outputLines = mutableListOf<String>()

    content.lineSequence().forEach { rawLine ->
      val trimmed = rawLine.trim()
      if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
        currentSection = trimmed.substring(1, trimmed.length - 1).trim()
        outputLines.add(rawLine)
      } else if (!trimmed.startsWith(";") && !trimmed.startsWith("#")) {
        val match = KEY_VALUE_REGEX.find(trimmed)
        if (match != null) {
          val key = match.groupValues[1].trim()
          val value = match.groupValues[2].trim()
          val fullPath = if (currentSection.isNotEmpty()) "$currentSection.$key" else key

          val targetVal = modifications[fullPath] ?: modifications[key] ?: modifications[key.lowercase()]
          if (targetVal != null) {
            val separator = if (trimmed.contains("=")) "=" else ":"
            outputLines.add("$key $separator $targetVal")
            changes.add(FieldChange(key, fullPath, value, targetVal.toString()))
          } else {
            outputLines.add(rawLine)
          }
        } else {
          outputLines.add(rawLine)
        }
      } else {
        outputLines.add(rawLine)
      }
    }

    return Pair(outputLines.joinToString("\n"), changes)
  }
}
