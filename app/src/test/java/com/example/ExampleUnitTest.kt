package com.example

import com.example.ai.LocalIntentEngine
import com.example.data.SampleSaves
import com.example.model.FileFormat
import com.example.parser.IniSaveParser
import com.example.parser.JsonSaveParser
import com.example.parser.SaveParserManager
import com.example.parser.TextSaveParser
import com.example.parser.XmlSaveParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testJsonSaveParsingAndModification() {
    val sample = SampleSaves.RPG_JSON
    val fields = JsonSaveParser.extractFields(sample)
    assertTrue("Should find candidate currency fields", fields.isNotEmpty())

    val coinField = fields.find { it.key == "coins" }
    assertNotNull("Should find 'coins' field", coinField)
    assertEquals(18200L, coinField?.numericValue)

    // Apply modification
    val (modifiedJson, changes) = JsonSaveParser.applyModifications(
      sample,
      mapOf("coins" to 999999999L, "diamonds" to 500000L)
    )

    assertEquals(2, changes.size)
    assertTrue("Modified JSON should contain target coins", modifiedJson.contains("999999999"))
    assertTrue("Modified JSON should contain target diamonds", modifiedJson.contains("500000"))

    // Re-parse modified JSON
    val updatedFields = JsonSaveParser.extractFields(modifiedJson)
    val updatedCoin = updatedFields.find { it.key == "coins" }
    assertEquals(999999999L, updatedCoin?.numericValue)
  }

  @Test
  fun testXmlSaveParsingAndModification() {
    val sampleXml = SampleSaves.SPEED_DRIFT_XML
    val fields = XmlSaveParser.extractFields(sampleXml)
    assertTrue("Should extract XML currency fields", fields.isNotEmpty())

    val cashField = fields.find { it.key == "cash" }
    assertNotNull("Should find cash field", cashField)
    assertEquals(12500L, cashField?.numericValue)

    // Apply modification
    val (modifiedXml, changes) = XmlSaveParser.applyModifications(
      sampleXml,
      mapOf("cash" to 999999999L)
    )

    assertEquals(1, changes.size)
    assertTrue("Modified XML should have new value", modifiedXml.contains("value=\"999999999\""))
    assertTrue("XML tags should remain intact", modifiedXml.contains("<map>"))
  }

  @Test
  fun testIniSaveParsingAndModification() {
    val sampleIni = SampleSaves.CYBER_MECHA_INI
    val fields = IniSaveParser.extractFields(sampleIni)
    assertTrue("Should extract INI fields", fields.isNotEmpty())

    val creditsField = fields.find { it.key == "credits" }
    assertNotNull("Should find credits", creditsField)
    assertEquals(3200L, creditsField?.numericValue)

    val (modifiedIni, changes) = IniSaveParser.applyModifications(
      sampleIni,
      mapOf("credits" to 999999999L)
    )
    assertTrue("Changes recorded", changes.isNotEmpty())
    assertTrue("Modified INI contains 999999999", modifiedIni.contains("credits = 999999999"))
  }

  @Test
  fun testTextSaveParsing() {
    val sampleTxt = SampleSaves.RETRO_DUNGEON_TXT
    val fields = TextSaveParser.extractFields(sampleTxt)
    assertTrue(fields.any { it.key.equals("COINS", ignoreCase = true) })

    val (modified, changes) = TextSaveParser.applyModifications(sampleTxt, mapOf("COINS" to 999999999L))
    assertEquals(1, changes.size)
    assertTrue(modified.contains("COINS: 999999999"))
  }

  @Test
  fun testLocalIntentEngine() {
    val sampleFile = SaveParserManager.parseFile("test.json", SampleSaves.RPG_JSON)

    // Unlimited coins
    val resultCoins = LocalIntentEngine.interpretCommand("Give me unlimited coins", sampleFile)
    assertFalse(resultCoins.isAnalysisOnly)
    assertTrue(resultCoins.fieldModifications.values.contains(999999999L))

    // Set specific amount
    val resultSpecific = LocalIntentEngine.interpretCommand("Set my diamonds to 500000", sampleFile)
    assertFalse(resultSpecific.isAnalysisOnly)
    assertTrue(resultSpecific.fieldModifications.values.contains(500000L))

    // Analyze command
    val resultAnalyze = LocalIntentEngine.interpretCommand("Analyze my game save", sampleFile)
    assertTrue(resultAnalyze.isAnalysisOnly)
    assertTrue(resultAnalyze.explanation.contains("Analysis"))
  }

  @Test
  fun testUnsupportedBinaryDetection() {
    val fakeBinary = "SQLite format 3\u0000\u0001\u0002\u0003database_table_blob"
    val file = SaveParserManager.parseFile("game.db", fakeBinary)
    assertFalse("SQLite should be flagged as unsupported", file.isSupported)
    assertEquals(FileFormat.ENCRYPTED_OR_BINARY, file.format)
  }
}
