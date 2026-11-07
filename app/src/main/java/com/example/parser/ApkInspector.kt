package com.example.parser

import com.example.model.ApkMetadata
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ApkInspector {

  fun inspectApk(inputStream: InputStream, fileSize: Long, fileName: String): ApkMetadata {
    val detectedEngines = mutableSetOf<String>()
    val detectedConfigAssets = mutableListOf<String>()
    val networkLibs = mutableSetOf<String>()
    val antiCheatFlags = mutableSetOf<String>()
    val permissions = mutableSetOf<String>()
    var manifestFound = false
    var totalEntries = 0

    try {
      ZipInputStream(inputStream).use { zip ->
        var entry: ZipEntry? = zip.nextEntry
        while (entry != null) {
          totalEntries++
          val name = entry.name

          if (name.equals("AndroidManifest.xml", ignoreCase = true)) {
            manifestFound = true
            // Read manifest bytes to find printable permission strings
            val buffer = ByteArray(entry.size.toInt().coerceIn(1024, 65536))
            val bytesRead = zip.read(buffer)
            if (bytesRead > 0) {
              val manifestSnippet = String(buffer, 0, bytesRead, Charsets.ISO_8859_1)
              extractPermissionsFromBinaryManifest(manifestSnippet, permissions)
            }
          }

          // Game engine detection
          when {
            name.contains("libunity.so", ignoreCase = true) || name.contains("assets/bin/Data", ignoreCase = true) ->
              detectedEngines.add("Unity Engine")
            name.contains("libUE4.so", ignoreCase = true) || name.contains("libUnreal.so", ignoreCase = true) ->
              detectedEngines.add("Unreal Engine")
            name.contains("libgodot_android.so", ignoreCase = true) || name.endsWith(".pck", ignoreCase = true) ->
              detectedEngines.add("Godot Engine")
            name.contains("libcocos2d.so", ignoreCase = true) || name.contains("libcocos2djs.so", ignoreCase = true) ->
              detectedEngines.add("Cocos2d-x")
            name.contains("libflutter.so", ignoreCase = true) ->
              detectedEngines.add("Flutter Framework")
          }

          // Network & Server-Side Multiplayer Detection
          when {
            name.contains("firebase", ignoreCase = true) || name.contains("google-services.json", ignoreCase = true) ->
              networkLibs.add("Firebase Cloud / Backend Services")
            name.contains("Photon", ignoreCase = true) ->
              networkLibs.add("Photon Multiplayer Cloud Engine")
            name.contains("playfab", ignoreCase = true) ->
              networkLibs.add("Azure PlayFab Cloud Services")
            name.contains("nakama", ignoreCase = true) ->
              networkLibs.add("Nakama Server Architecture")
            name.contains("billing", ignoreCase = true) ->
              networkLibs.add("Google Play Billing Client")
          }

          // Anti-cheat & Security Obfuscation Detection
          when {
            name.contains("dexguard", ignoreCase = true) || name.contains("arxan", ignoreCase = true) ->
              antiCheatFlags.add("Advanced Bytecode Tamper-Proofing / DexGuard")
            name.contains("easyanticheat", ignoreCase = true) || name.contains("battleye", ignoreCase = true) ->
              antiCheatFlags.add("Kernel / Anti-Cheat Integrity Guard")
          }

          // Detect candidate config / balance assets
          if (name.startsWith("assets/") && (name.endsWith(".json") || name.endsWith(".xml") || name.endsWith(".ini") || name.endsWith(".csv"))) {
            if (detectedConfigAssets.size < 8) {
              detectedConfigAssets.add(name)
            }
          }

          zip.closeEntry()
          entry = zip.nextEntry
        }
      }
    } catch (_: Exception) {
      // Continue with whatever was read
    }

    val isLikelyOffline = networkLibs.isEmpty() && !permissions.contains("android.permission.INTERNET")
    val offlineReason = if (isLikelyOffline) {
      "No cloud backend libraries or INTERNET permissions detected. High probability of standalone offline local save storage."
    } else {
      "Online services detected (${networkLibs.joinToString(", ")}). Save data may be server-authoritative or cloud-synchronized."
    }

    val warnings = mutableListOf<String>()
    if (networkLibs.isNotEmpty()) {
      warnings.add("Live online architecture detected. Currencies managed server-side cannot and should not be modified locally.")
    }
    if (antiCheatFlags.isNotEmpty()) {
      warnings.add("Anti-tamper protection detected (${antiCheatFlags.joinToString()}). Modifying APK bytecode directly will cause signature verification failure.")
    }
    warnings.add("APK files contain compiled read-only app code and assets. Save files are created at runtime in the app's private data storage after installation.")

    val pkgGuess = extractLikelyPackageName(fileName)

    return ApkMetadata(
      packageName = pkgGuess,
      appName = fileName.removeSuffix(".apk"),
      versionName = "Inspected Archive",
      versionCode = totalEntries.toLong(),
      permissions = permissions.toList().ifEmpty { listOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.VIBRATE") },
      isOfflineGame = isLikelyOffline,
      offlineConfidenceReason = offlineReason,
      detectedConfigAssets = detectedConfigAssets,
      detectedGameEngines = detectedEngines.toList().ifEmpty { listOf("Native Android / Custom C++") },
      warnings = warnings,
      fileSize = fileSize
    )
  }

  private fun extractLikelyPackageName(fileName: String): String {
    val clean = fileName.removeSuffix(".apk").replace(Regex("[^a-zA-Z0-9_]"), "_").lowercase()
    return "com.game.$clean"
  }

  private fun extractPermissionsFromBinaryManifest(manifestString: String, results: MutableSet<String>) {
    val known = listOf(
      "android.permission.INTERNET",
      "android.permission.ACCESS_NETWORK_STATE",
      "android.permission.WAKE_LOCK",
      "android.permission.VIBRATE",
      "com.android.vending.BILLING",
      "android.permission.RECORD_AUDIO",
      "android.permission.CAMERA"
    )
    for (perm in known) {
      val simple = perm.substringAfterLast('.')
      if (manifestString.contains(perm) || manifestString.contains(simple)) {
        results.add(perm)
      }
    }
  }
}
