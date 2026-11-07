package com.example.parser

import com.example.model.CurrencyField
import com.example.model.FieldChange
import org.json.JSONArray
import org.json.JSONObject

object JsonSaveParser {

  private val JSON_NUM_PAIR_REGEX = Regex("""["']([a-zA-Z0-9_\-]+)["']\s*:\s*(-?[0-9]+(?:\.[0-9]+)?)""")

  fun extractFields(content: String): List<CurrencyField> {
    val results = mutableListOf<CurrencyField>()
    val trimmed = content.trim()

    try {
      if (trimmed.startsWith("{")) {
        val root = JSONObject(trimmed)
        traverseObject("", root, results)
        if (results.isNotEmpty()) return results
      } else if (trimmed.startsWith("[")) {
        val root = JSONArray(trimmed)
        traverseArray("", root, results)
        if (results.isNotEmpty()) return results
      }
    } catch (_: Throwable) {
      // In host JVM unit tests without Robolectric shadows, org.json stubs throw.
      // Gracefully fall back to robust regex extraction.
    }

    // Fallback regex extractor (works on JVM unit test and corrupt JSON)
    val seen = mutableSetOf<String>()
    for (match in JSON_NUM_PAIR_REGEX.findAll(content)) {
      val key = match.groupValues[1]
      val value = match.groupValues[2]
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

    return results
  }

  private fun traverseObject(currentPath: String, obj: JSONObject, list: MutableList<CurrencyField>) {
    val keys = obj.keys()
    while (keys.hasNext()) {
      val key = keys.next()
      val path = if (currentPath.isEmpty()) key else "$currentPath.$key"
      val value = obj.opt(key) ?: continue

      when (value) {
        is JSONObject -> traverseObject(path, value, list)
        is JSONArray -> traverseArray(path, value, list)
        is Number -> {
          if (CurrencyDetector.isCandidateKey(key) || CurrencyDetector.isCandidateKey(path)) {
            val numVal = value.toLong()
            val cat = CurrencyDetector.categorizeKey(key)
            list.add(
              CurrencyField(
                path = path,
                key = key,
                currentValue = value.toString(),
                numericValue = numVal,
                category = cat
              )
            )
          }
        }
        is String -> {
          if (CurrencyDetector.isCandidateKey(key) || CurrencyDetector.isCandidateKey(path)) {
            val (numVal, type) = CurrencyDetector.parseNumericValue(value)
            val cat = CurrencyDetector.categorizeKey(key)
            list.add(
              CurrencyField(
                path = path,
                key = key,
                currentValue = value,
                numericValue = numVal,
                type = type,
                category = cat
              )
            )
          }
        }
      }
    }
  }

  private fun traverseArray(currentPath: String, arr: JSONArray, list: MutableList<CurrencyField>) {
    for (i in 0 until arr.length()) {
      val path = "$currentPath[$i]"
      val value = arr.opt(i) ?: continue
      when (value) {
        is JSONObject -> traverseObject(path, value, list)
        is JSONArray -> traverseArray(path, value, list)
        else -> {
          if (CurrencyDetector.isCandidateKey(currentPath)) {
            val (numVal, type) = CurrencyDetector.parseNumericValue(value.toString())
            list.add(
              CurrencyField(
                path = path,
                key = "$currentPath[$i]",
                currentValue = value.toString(),
                numericValue = numVal,
                type = type,
                category = CurrencyDetector.categorizeKey(currentPath)
              )
            )
          }
        }
      }
    }
  }

  fun applyModifications(content: String, modifications: Map<String, Long>): Pair<String, List<FieldChange>> {
    val changes = mutableListOf<FieldChange>()
    val trimmed = content.trim()

    try {
      if (trimmed.startsWith("{")) {
        val root = JSONObject(trimmed)
        applyToObject("", root, modifications, changes)
        if (changes.isNotEmpty()) {
          return Pair(root.toString(2), changes)
        }
      } else if (trimmed.startsWith("[")) {
        val root = JSONArray(trimmed)
        applyToArray("", root, modifications, changes)
        if (changes.isNotEmpty()) {
          return Pair(root.toString(2), changes)
        }
      }
    } catch (_: Throwable) {
      // Fall through to regex modification
    }

    // Robust regex fallback modifier
    var modified = content
    for ((targetKey, targetVal) in modifications) {
      val cleanKey = targetKey.substringAfterLast('.').lowercase()
      val pattern = Regex("""(["']$cleanKey["']\s*:\s*)(-?[0-9]+)""", RegexOption.IGNORE_CASE)
      if (pattern.containsMatchIn(modified)) {
        val match = pattern.find(modified)
        if (match != null) {
          val oldVal = match.groupValues[2]
          modified = pattern.replace(modified) { m ->
            "${m.groupValues[1]}$targetVal"
          }
          changes.add(FieldChange(cleanKey, targetKey, oldVal, targetVal.toString()))
        }
      }
    }

    return Pair(modified, changes)
  }

  private fun applyToObject(
    currentPath: String,
    obj: JSONObject,
    modifications: Map<String, Long>,
    changes: MutableList<FieldChange>
  ) {
    val keys = obj.keys().asSequence().toList()
    for (key in keys) {
      val path = if (currentPath.isEmpty()) key else "$currentPath.$key"
      val value = obj.opt(key) ?: continue

      val targetNewVal = modifications[path] ?: modifications[key] ?: modifications[key.lowercase()]

      if (targetNewVal != null && (value is Number || (value is String && value.toLongOrNull() != null))) {
        val oldValStr = value.toString()
        if (value is String) {
          obj.put(key, targetNewVal.toString())
        } else if (value is Double || value is Float) {
          obj.put(key, targetNewVal.toDouble())
        } else {
          obj.put(key, targetNewVal)
        }
        changes.add(
          FieldChange(
            fieldKey = key,
            path = path,
            oldValue = oldValStr,
            newValue = targetNewVal.toString()
          )
        )
      } else if (value is JSONObject) {
        applyToObject(path, value, modifications, changes)
      } else if (value is JSONArray) {
        applyToArray(path, value, modifications, changes)
      }
    }
  }

  private fun applyToArray(
    currentPath: String,
    arr: JSONArray,
    modifications: Map<String, Long>,
    changes: MutableList<FieldChange>
  ) {
    for (i in 0 until arr.length()) {
      val path = "$currentPath[$i]"
      val value = arr.opt(i) ?: continue
      if (value is JSONObject) {
        applyToObject(path, value, modifications, changes)
      } else if (value is JSONArray) {
        applyToArray(path, value, modifications, changes)
      }
    }
  }
}
