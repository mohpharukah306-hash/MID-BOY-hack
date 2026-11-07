package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.FieldChange
import com.example.model.MessageRole
import com.example.model.SaveFile
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChatScreen(
  messages: List<ChatMessage>,
  currentFile: SaveFile?,
  isThinking: Boolean,
  onApplyPendingChanges: (Map<String, Long>, String) -> Unit,
  onExportClick: () -> Unit,
  onRestoreBackupClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()
  val clipboardManager = LocalClipboardManager.current

  LaunchedEffect(messages.size, isThinking) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
  ) {
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 110.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        when (msg.role) {
          MessageRole.USER -> UserMessageBubble(text = msg.text)
          MessageRole.ASSISTANT -> AssistantMessageCard(
            message = msg,
            currentFile = currentFile,
            onApply = { mods -> onApplyPendingChanges(mods, msg.text) },
            onExport = onExportClick,
            onRestore = onRestoreBackupClick,
            onCopy = { clipboardManager.setText(AnnotatedString(msg.text)) }
          )
          MessageRole.SYSTEM -> SystemMessageBanner(text = msg.text)
        }
      }

      if (isThinking) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(CyberSurfaceContainer),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(
                color = NeonEmerald,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Analyzing save structure & generating modification protocol...",
              fontSize = 12.sp,
              color = NeonMint,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }
  }
}

@Composable
fun UserMessageBubble(text: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
        .background(CyberSurfaceContainer)
        .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag("user_message_bubble")
    ) {
      Text(
        text = text,
        color = TextPrimary,
        fontSize = 14.sp,
        lineHeight = 20.sp
      )
    }
  }
}

@Composable
fun AssistantMessageCard(
  message: ChatMessage,
  currentFile: SaveFile?,
  onApply: (Map<String, Long>) -> Unit,
  onExport: () -> Unit,
  onRestore: () -> Unit,
  onCopy: () -> Unit
) {
  var isThumbedUp by remember { mutableStateOf(false) }
  var isThumbedDown by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .background(CyberSurface)
      .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(18.dp))
      .padding(16.dp)
      .testTag("assistant_message_card")
  ) {
    // Header tag
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 8.dp)
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(CyberSurfaceContainer)
          .padding(horizontal = 8.dp, vertical = 3.dp)
      ) {
        Text(
          text = if (message.isAiOnline) "🤖 GEMINI 3.8 FLASH" else "⚡ OFFLINE ENGINE",
          color = NeonEmerald,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      if (message.targetFileName != null) {
        Text(
          text = message.targetFileName,
          color = TextSecondary,
          fontSize = 10.sp
        )
      }
    }

    // Message body
    Text(
      text = message.text,
      color = TextPrimary,
      fontSize = 14.sp,
      lineHeight = 21.sp
    )

    // Applied field changes summary card (if already modified)
    if (message.fieldChanges.isNotEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(CyberSurfaceContainer)
          .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
          .padding(12.dp)
      ) {
        Text(
          text = "🛡️ Verified Field Modifications:",
          color = NeonEmerald,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        message.fieldChanges.forEach { change ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = change.fieldKey, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
              text = "${change.oldValue} ➜ ${change.newValue}",
              color = NeonMint,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(
            onClick = onExport,
            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).testTag("chat_export_button")
          ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export Save", color = CyberBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = onRestore,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).testTag("chat_restore_button")
          ) {
            Icon(Icons.Default.Restore, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Undo / Restore", fontSize = 12.sp)
          }
        }
      }
    }

    // Pending direct apply button
    if (message.canApplyDirectly && message.pendingChanges.isNotEmpty() && message.fieldChanges.isEmpty()) {
      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = { onApply(message.pendingChanges) },
        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("apply_modifications_button")
      ) {
        Text(
          text = "⚡ INJECT MODIFICATIONS TO SAVE",
          color = CyberBlack,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }
    }

    // Feedback Action Bar matching Phone 3 in screenshot
    Spacer(modifier = Modifier.height(12.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .clickable {
            isThumbedUp = !isThumbedUp
            isThumbedDown = false
          },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ThumbUp,
          contentDescription = "Thumbs Up",
          tint = if (isThumbedUp) NeonEmerald else TextSecondary,
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .clickable {
            isThumbedDown = !isThumbedDown
            isThumbedUp = false
          },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ThumbDown,
          contentDescription = "Thumbs Down",
          tint = if (isThumbedDown) TextPrimary else TextSecondary,
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .clickable { onCopy() },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ContentCopy,
          contentDescription = "Copy Text",
          tint = TextSecondary,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun SystemMessageBanner(text: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CyberSurfaceContainer.copy(alpha = 0.5f))
      .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Text(
      text = text,
      color = TextSecondary,
      fontSize = 12.sp,
      lineHeight = 18.sp
    )
  }
}
