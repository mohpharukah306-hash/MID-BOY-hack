package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
  initialProxyUrl: String,
  initialApiKey: String,
  initialModel: String,
  onDismiss: () -> Unit,
  onSave: (proxyUrl: String, apiKey: String, model: String) -> Unit
) {
  var proxyUrl by remember { mutableStateOf(initialProxyUrl) }
  var apiKey by remember { mutableStateOf(initialApiKey) }
  var selectedModel by remember { mutableStateOf(initialModel) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = CyberSurface,
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(20.dp))
        .padding(4.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "⚙️ AI & PROXY SETTINGS",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = NeonEmerald
        )

        Text(
          text = "MID GAME HACKER uses local native parsing for offline files, and connects to a secure Gemini backend proxy for natural language reasoning.",
          fontSize = 12.sp,
          color = TextSecondary
        )

        // Proxy URL
        OutlinedTextField(
          value = proxyUrl,
          onValueChange = { proxyUrl = it },
          label = { Text("Secure Backend Proxy URL", color = TextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonEmerald,
            unfocusedBorderColor = DeepEmeraldBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("proxy_url_input")
        )

        // Optional API key override
        OutlinedTextField(
          value = apiKey,
          onValueChange = { apiKey = it },
          label = { Text("Optional Gemini API Key (Direct)", color = TextSecondary) },
          placeholder = { Text("Leave blank to use Backend Proxy", color = TextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonEmerald,
            unfocusedBorderColor = DeepEmeraldBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("api_key_input")
        )

        Text(
          text = "Select Model Tier:",
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = TextPrimary
        )

        listOf(
          "gemini-3.8-flash" to "General Balance Analysis (Recommended)",
          "gemini-3.1-flash-lite" to "Ultra-Fast Intent Parsing",
          "gemini-4-argon" to "Deep Complex Save Decompilation"
        ).forEach { (model, desc) ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            RadioButton(
              selected = selectedModel == model,
              onClick = { selectedModel = model },
              colors = RadioButtonDefaults.colors(
                selectedColor = NeonEmerald,
                unselectedColor = TextSecondary
              )
            )
            Column(modifier = Modifier.padding(start = 4.dp)) {
              Text(text = model, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
              Text(text = desc, color = TextSecondary, fontSize = 10.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(
            onClick = onDismiss,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.padding(horizontal = 4.dp))
          Button(
            onClick = { onSave(proxyUrl, apiKey, selectedModel) },
            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("save_settings_button")
          ) {
            Text("Save", color = CyberBlack, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
