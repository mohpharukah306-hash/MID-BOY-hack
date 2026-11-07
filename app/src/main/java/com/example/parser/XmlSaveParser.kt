package com.example.parser

import com.example.model.CurrencyField
import com.example.model.FieldChange

object XmlSaveParser {

  // Pattern 1: <int name="coins" value="1250" /> or <long name="gold" value="5000"/>
  private val SHARED_PREFS_ATTR_REGEX = Regex("""<(int|long|float|string)\s+name=["']([^"']+)["']\s+value=["']([^"']*)["']\s*/>""")
  // Pattern 2: <string name="diamonds">15</string>
  private val SHARED_PREFS_TEXT_REGEX = Regex("""<(int|long|float|string)\s+name=["']([^"']+)["']>([^<]*)<\/(?:int|long|float|string)>""")
  // Pattern 3: <coins>1250</coins> or <gold>500</gold>
  private val TAG_TEXT_REGEX = Regex("""<([a-zA-Z0-9_\-\.]+)>([^<]+)<\/\1>""")
  // Pattern 4: <player coins="100" gold="50" ... />
  private val INLINE_ATTR_REGEX = Regex("""([a-zA-Z0-9_\-]+)=["']([0-9\.]+)["']""")

  fun extractFields(content: String): List<CurrencyField> {
    val results = mutableListOf<CurrencyField>()
    val seen = mutableSetOf<String>()

    // Check shared_prefs attribute pattern
    for (match in SHARED_PREFS_ATTR_REGEX.findAll(content)) {
      val key = match.groupValues[2]
      val value = match.groupValues[3]
      if (CurrencyDetector.isCandidateKey(key) && seen.add(key)) {
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

    // Check shared_prefs text pattern
    for (match in SHARED_PREFS_TEXT_REGEX.findAll(content)) {
      val key = match.groupValues[2]
      val value = match.groupValues[3]
      if (CurrencyDetector.isCandidateKey(key) && seen.add(key)) {
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

    // Check tag text pattern
    for (match in TAG_TEXT_REGEX.findAll(content)) {
      val key = match.groupValues[1]
      val value = match.groupValues[2].trim()
      if (CurrencyDetector.isCandidateKey(key) && seen.add(key)) {
        val (numVal, type) = CurrencyDetector.parseNumericValue(value)
        if (numVal != null) {
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

    // Check inline attributes (e.g. coins="1000")
    for (match in INLINE_ATTR_REGEX.findAll(content)) {
      val key = match.groupValues[1]
      val value = match.groupValues[2]
      if (CurrencyDetector.isCandidateKey(key) && seen.add(key)) {
        val (numVal, type) = CurrencyDetector.parseNumericValue(value)
        if (numVal != null) {
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
    var modified = content
    val changes = mutableListOf<FieldChange>()

    for ((targetKey, newVal) in modifications) {
      val cleanKey = targetKey.substringAfterLast('.').lowercase()

      // 1. Try SharedPrefs attribute pattern: <int name="coins" value="1250" />
      val attrPattern = Regex("""(<(?:int|long|float|string)\s+name=["']$cleanKey["']\s+value=["'])([^"']*)(["']\s*\/?>)""", RegexOption.IGNORE_CASE)
      if (attrPattern.containsMatchIn(modified)) {
        val match = attrPattern.find(modified)
        if (match != null) {
          val oldVal = match.groupValues[2]
          modified = attrPattern.replace(modified) { m ->
            "${m.groupValues[1]}$newVal${m.groupValues[3]}"
          }
          changes.add(FieldChange(targetKey, targetKey, oldVal, newVal.toString()))
          continue
        }
      }

      // 2. Try SharedPrefs text pattern: <string name="coins">1250</string>
      val textPattern = Regex("""(<(?:int|long|float|string)\s+name=["']$cleanKey["']>)([^<]*)(<\/(?:int|long|float|string)>)""", RegexOption.IGNORE_CASE)
      if (textPattern.containsMatchIn(modified)) {
        val match = textPattern.find(modified)
        if (match != null) {
          val oldVal = match.groupValues[2]
          modified = textPattern.replace(modified) { m ->
            "${m.groupValues[1]}$newVal${m.groupValues[3]}"
          }
          changes.add(FieldChange(targetKey, targetKey, oldVal, newVal.toString()))
          continue
        }
      }

      // 3. Try tag text pattern: <coins>1250</coins>
      val tagPattern = Regex("""(<$cleanKey>)([^<]*)(<\/$cleanKey>)""", RegexOption.IGNORE_CASE)
      if (tagPattern.containsMatchIn(modified)) {
        val match = tagPattern.find(modified)
        if (match != null) {
          val oldVal = match.groupValues[2]
          modified = tagPattern.replace(modified) { m ->
            "${m.groupValues[1]}$newVal${m.groupValues[3]}"
          }
          changes.add(FieldChange(targetKey, targetKey, oldVal, newVal.toString()))
          continue
        }
      }

      // 4. Try inline attribute: coins="1250"
      val inlinePattern = Regex("""(\b$cleanKey=["'])([^"']*)(["'])""", RegexOption.IGNORE_CASE)
      if (inlinePattern.containsMatchIn(modified)) {
        val match = inlinePattern.find(modified)
        if (match != null) {
          val oldVal = match.groupValues[2]
          modified = inlinePattern.replace(modified) { m ->
            "${m.groupValues[1]}$newVal${m.groupValues[3]}"
          }
          changes.add(FieldChange(targetKey, targetKey, oldVal, newVal.toString()))
          continue
        }
      }
    }

    return Pair(modified, changes)
  }
}
