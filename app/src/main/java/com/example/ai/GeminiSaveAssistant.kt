package com.example.ai

import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.model.SaveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class AiResponse(
  val message: ChatMessage,
  val fieldModifications: Map<String, Long>
)

class GeminiSaveAssistant(
  var proxyUrl: String = "https://ais-dev-5rs5ir5ngb2elkxrmooc6k-555320434917.asia-southeast1.run.app",
  var apiKey: String = "",
  var modelName: String = "gemini-3.8-flash"
) {

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  private val conversationHistory = mutableListOf<ChatMessage>()

  fun getHistory(): List<ChatMessage> = conversationHistory.toList()

  fun addMessage(message: ChatMessage) {
    conversationHistory.add(message)
  }

  fun clearHistory() {
    conversationHistory.clear()
  }

  suspend fun processUserCommand(
    command: String,
    currentFile: SaveFile?
  ): AiResponse = withContext(Dispatchers.IO) {
    val userMsg = ChatMessage(
      id = UUID.randomUUID().toString(),
      role = MessageRole.USER,
      text = command,
      timestamp = System.currentTimeMillis()
    )
    conversationHistory.add(userMsg)

    // Try online Gemini API first if configured or fallback to local
    var onlineResult: AiResponse? = null
    try {
      onlineResult = callGeminiApi(command, currentFile)
    } catch (_: Exception) {
      // Fallback seamlessly to offline engine
      onlineResult = null
    }

    if (onlineResult != null) {
      conversationHistory.add(onlineResult.message)
      return@withContext onlineResult
    }

    // Local offline intent engine execution
    val localResult = LocalIntentEngine.interpretCommand(command, currentFile)
    val assistantMsg = ChatMessage(
      id = UUID.randomUUID().toString(),
      role = MessageRole.ASSISTANT,
      text = localResult.explanation + "\n\n🟢 *[Local Offline Engine • Zero Latency]*",
      timestamp = System.currentTimeMillis(),
      isAiOnline = false,
      targetFileName = currentFile?.name,
      canApplyDirectly = localResult.fieldModifications.isNotEmpty(),
      pendingChanges = localResult.fieldModifications
    )
    conversationHistory.add(assistantMsg)

    AiResponse(
      message = assistantMsg,
      fieldModifications = localResult.fieldModifications
    )
  }

  private fun callGeminiApi(command: String, currentFile: SaveFile?): AiResponse? {
    val url = if (proxyUrl.endsWith("/")) proxyUrl.dropLast(1) else proxyUrl
    val targetEndpoint = "$url/v1beta/models/$modelName:generateContent"

    val fileSummary = if (currentFile != null) {
      """
      Current Active File: "${currentFile.name}"
      Format: ${currentFile.format.label}
      Size: ${currentFile.sizeBytes} bytes
      Parsed Candidate Fields:
      ${currentFile.parsedFields.joinToString("\n") { " - ${it.path} (key=${it.key}, current=${it.currentValue}, category=${it.category.displayName})" }}
      """.trimIndent()
    } else {
      "No file is currently loaded in the editor."
    }

    val systemInstructionText = """
      You are MID GAME HACKER AI, an elite cybernetic offline game save analyst and modification advisor.
      The user communicates naturally with commands like 'Give me unlimited coins', 'Set gold to 999999999', 'Analyze my save'.
      
      $fileSummary
      
      GUIDELINES:
      1. Explain your analysis with futuristic cyber hacker style (neon, glitch, precision).
      2. Identify candidate currency fields accurately from the provided file structure.
      3. If the user wants to change values, output an exact JSON code block at the end of your response with the format:
      ```json
      {
        "modifications": {
          "field_path_or_key": target_number
        }
      }
      ```
      4. Always warn if a file is an APK or online multiplayer game, explaining that online currencies are server-authoritative.
    """.trimIndent()

    val contentsArray = JSONArray()
    // Add past 4 messages for multi-turn context
    val recent = conversationHistory.takeLast(5)
    for (msg in recent) {
      val roleStr = if (msg.role == MessageRole.USER) "user" else "model"
      val partObj = JSONObject().put("text", msg.text)
      val contentObj = JSONObject()
        .put("role", roleStr)
        .put("parts", JSONArray().put(partObj))
      contentsArray.put(contentObj)
    }

    val requestBodyJson = JSONObject()
      .put("contents", contentsArray)
      .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText))))

    val endpointsToTry = mutableListOf<String>()
    if (apiKey.isNotBlank()) {
      endpointsToTry.add("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
    }
    val cleanProxy = if (proxyUrl.endsWith("/")) proxyUrl.dropLast(1) else proxyUrl
    if (cleanProxy.isNotBlank()) {
      val proxyEndpoint = if (apiKey.isNotBlank()) "$cleanProxy/v1beta/models/$modelName:generateContent?key=$apiKey" else "$cleanProxy/v1beta/models/$modelName:generateContent"
      endpointsToTry.add(proxyEndpoint)
    }

    var successfulBody: String? = null
    for (endpoint in endpointsToTry) {
      try {
        val requestBuilder = Request.Builder()
          .url(endpoint)
          .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))

        if (apiKey.isNotBlank()) {
          requestBuilder.addHeader("x-goog-api-key", apiKey)
        }

        val response = client.newCall(requestBuilder.build()).execute()
        if (response.isSuccessful) {
          successfulBody = response.body?.string()
          if (!successfulBody.isNullOrBlank()) {
            break
          }
        }
      } catch (_: Exception) {
        // Try next endpoint
      }
    }

    val responseBody = successfulBody ?: return null
    val jsonResp = JSONObject(responseBody)
    val candidates = jsonResp.optJSONArray("candidates") ?: return null
    if (candidates.length() == 0) return null

    val firstCandidate = candidates.getJSONObject(0)
    val content = firstCandidate.optJSONObject("content") ?: return null
    val parts = content.optJSONArray("parts") ?: return null
    if (parts.length() == 0) return null

    val rawText = parts.getJSONObject(0).optString("text", "")
    if (rawText.isBlank()) return null

    // Extract JSON modifications block if present
    val extractedMods = extractModificationsFromJsonBlock(rawText)

    val cleanText = rawText.replace(Regex("""```json\s*\{\s*"modifications"[\s\S]*?\}\s*```"""), "").trim()

    val assistantMsg = ChatMessage(
      id = UUID.randomUUID().toString(),
      role = MessageRole.ASSISTANT,
      text = if (cleanText.isNotBlank()) cleanText else rawText,
      timestamp = System.currentTimeMillis(),
      isAiOnline = true,
      targetFileName = currentFile?.name,
      canApplyDirectly = extractedMods.isNotEmpty(),
      pendingChanges = extractedMods
    )

    return AiResponse(
      message = assistantMsg,
      fieldModifications = extractedMods
    )
  }

  private fun extractModificationsFromJsonBlock(text: String): Map<String, Long> {
    val results = mutableMapOf<String, Long>()
    val jsonBlockRegex = Regex("""```json\s*(\{[\s\S]*?\})\s*```""")
    val match = jsonBlockRegex.find(text)
    if (match != null) {
      try {
        val jsonStr = match.groupValues[1]
        val root = JSONObject(jsonStr)
        val mods = root.optJSONObject("modifications")
        if (mods != null) {
          val keys = mods.keys()
          while (keys.hasNext()) {
            val key = keys.next()
            val valNum = mods.optLong(key, -1L)
            if (valNum >= 0) {
              results[key] = valNum
            }
          }
        }
      } catch (_: Exception) {
      }
    }
    return results
  }
}
