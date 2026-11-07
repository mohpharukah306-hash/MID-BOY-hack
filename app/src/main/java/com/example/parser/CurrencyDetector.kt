package com.example.parser

import com.example.model.FieldCategory
import com.example.model.FieldType
import java.util.Locale

object CurrencyDetector {
  private val COIN_KEYWORDS = listOf("coin", "coins", "silver", "penny", "credits")
  private val GOLD_KEYWORDS = listOf("gold", "gold_bars", "goldbars", "bullion", "ore")
  private val DIAMOND_KEYWORDS = listOf("diamond", "diamonds", "gem", "gems", "ruby", "rubies", "crystal", "crystals", "emerald")
  private val CASH_KEYWORDS = listOf("cash", "money", "bucks", "dollar", "funds", "wallet", "balance")
  private val ENERGY_KEYWORDS = listOf("energy", "stamina", "mana", "fuel", "health", "hp", "lives", "heart", "hearts")
  private val LEVEL_KEYWORDS = listOf("level", "lvl", "xp", "exp", "experience", "rank", "tier", "skill_points")
  private val SCORE_KEYWORDS = listOf("score", "highscore", "high_score", "points", "stars")

  fun isCandidateKey(key: String): Boolean {
    val lower = key.lowercase(Locale.ROOT)
    return COIN_KEYWORDS.any { lower.contains(it) } ||
      GOLD_KEYWORDS.any { lower.contains(it) } ||
      DIAMOND_KEYWORDS.any { lower.contains(it) } ||
      CASH_KEYWORDS.any { lower.contains(it) } ||
      ENERGY_KEYWORDS.any { lower.contains(it) } ||
      LEVEL_KEYWORDS.any { lower.contains(it) } ||
      SCORE_KEYWORDS.any { lower.contains(it) } ||
      lower.contains("token") || lower.contains("ticket") || lower.contains("res")
  }

  fun categorizeKey(key: String): FieldCategory {
    val lower = key.lowercase(Locale.ROOT)
    return when {
      GOLD_KEYWORDS.any { lower.contains(it) } -> FieldCategory.GOLD
      DIAMOND_KEYWORDS.any { lower.contains(it) } -> FieldCategory.DIAMONDS
      COIN_KEYWORDS.any { lower.contains(it) } -> FieldCategory.COINS
      CASH_KEYWORDS.any { lower.contains(it) } -> FieldCategory.CASH
      ENERGY_KEYWORDS.any { lower.contains(it) } -> FieldCategory.ENERGY
      LEVEL_KEYWORDS.any { lower.contains(it) } -> FieldCategory.LEVEL_XP
      SCORE_KEYWORDS.any { lower.contains(it) } -> FieldCategory.SCORE
      else -> FieldCategory.OTHER
    }
  }

  fun parseNumericValue(valueStr: String): Pair<Long?, FieldType> {
    val clean = valueStr.trim().removeSurrounding("\"", "\"").removeSurrounding("'", "'")
    return try {
      val longVal = clean.toLong()
      Pair(longVal, FieldType.INTEGER)
    } catch (_: Exception) {
      try {
        val doubleVal = clean.toDouble()
        Pair(doubleVal.toLong(), FieldType.FLOAT)
      } catch (_: Exception) {
        Pair(null, FieldType.STRING_NUMBER)
      }
    }
  }
}
