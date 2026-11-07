package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BottomInputBar(
  inputText: String,
  onInputTextChange: (String) -> Unit,
  onSendClick: () -> Unit,
  onAttachClick: () -> Unit,
  onMicClick: () -> Unit,
  onPresetsClick: () -> Unit,
  placeholder: String = "What do you want to change?",
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(32.dp))
        .background(CyberSurfaceContainer)
        .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(32.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Attachment paperclip icon
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .clickable { onAttachClick() }
          .testTag("attach_file_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.AttachFile,
          contentDescription = "Attach Save File",
          tint = TextSecondary,
          modifier = Modifier.size(19.dp)
        )
      }

      // Presets bolt icon
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .clickable { onPresetsClick() }
          .testTag("preset_hacks_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Bolt,
          contentDescription = "Quick Hacks",
          tint = NeonEmerald,
          modifier = Modifier.size(19.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Input field
      Box(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        if (inputText.isEmpty()) {
          Text(
            text = placeholder,
            color = TextSecondary.copy(alpha = 0.65f),
            fontSize = 14.sp
          )
        }
        BasicTextField(
          value = inputText,
          onValueChange = onInputTextChange,
          singleLine = true,
          textStyle = TextStyle(
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          ),
          cursorBrush = SolidColor(NeonEmerald),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(onSend = { onSendClick() }),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_input_text")
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Mic / Voice Orb icon
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .clickable { onMicClick() }
          .testTag("mic_scan_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Mic,
          contentDescription = "Voice / Orb Scanner",
          tint = TextSecondary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // Send button (circular white/neon pill with arrow)
      val canSend = inputText.isNotBlank()
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(if (canSend) NeonEmerald else Color.White)
          .clickable(enabled = canSend) { onSendClick() }
          .testTag("send_command_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send Command",
          tint = CyberBlack,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
