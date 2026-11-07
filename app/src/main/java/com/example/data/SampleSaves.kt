package com.example.data

import com.example.model.FileFormat
import com.example.model.SaveFile
import com.example.parser.SaveParserManager
import java.util.UUID

object SampleSaves {

  val RPG_JSON = """
  {
    "gameTitle": "Shadow Realm RPG",
    "version": "1.4.2",
    "player": {
      "name": "ShadowBlade",
      "level": 14,
      "experience": 45200,
      "energy": 65,
      "maxEnergy": 100,
      "stats": {
        "health": 1200,
        "mana": 450,
        "attack": 320,
        "defense": 180
      }
    },
    "wallet": {
      "gold": 2450,
      "coins": 18200,
      "diamonds": 45,
      "ruby_gems": 8
    },
    "inventory": [
      {"id": "sword_iron", "count": 1},
      {"id": "health_potion", "count": 12},
      {"id": "respawn_token", "count": 3}
    ],
    "settings": {
      "soundVolume": 80,
      "musicVolume": 60,
      "haptics": true
    }
  }
  """.trimIndent()

  val SPEED_DRIFT_XML = """
  <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
  <map>
      <string name="driver_alias">SpeedDemon</string>
      <int name="player_level" value="9" />
      <int name="cash" value="12500" />
      <int name="gold_bars" value="18" />
      <int name="nitro_boost" value="5" />
      <int name="drift_tokens" value="340" />
      <long name="high_score" value="1295000" />
      <boolean name="tilt_controls" value="true" />
      <string name="current_car">cyber_gtr_r35</string>
  </map>
  """.trimIndent()

  val CYBER_MECHA_INI = """
  ; Cyber Mecha 2088 Save State
  [Pilot]
  callsign = Vanguard
  rank = 7
  pilot_xp = 18900

  [Resources]
  credits = 3200
  plasma_cores = 14
  titanium_ore = 850
  repair_nanites = 25
  energy_cells = 48

  [MechaStats]
  armor = 2400
  shield = 1800
  weapon_damage = 450
  """.trimIndent()

  val RETRO_DUNGEON_TXT = """
  PLAYER_NAME: KnightArtorias
  COINS: 850
  GEMS: 12
  SOULS: 14200
  DUNGEON_FLOOR: 8
  MAX_HP: 350
  LIVES: 3
  """.trimIndent()

  fun getSamples(): List<SaveFile> {
    val sample1 = SaveParserManager.parseFile("shadow_realm_save.json", RPG_JSON).copy(isSample = true)
    val sample2 = SaveParserManager.parseFile("speed_drift_prefs.xml", SPEED_DRIFT_XML).copy(isSample = true)
    val sample3 = SaveParserManager.parseFile("cyber_mecha_state.ini", CYBER_MECHA_INI).copy(isSample = true)
    val sample4 = SaveParserManager.parseFile("retro_dungeon_save.txt", RETRO_DUNGEON_TXT).copy(isSample = true)
    return listOf(sample1, sample2, sample3, sample4)
  }
}
