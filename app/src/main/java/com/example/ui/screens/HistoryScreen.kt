package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SaveOperation
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  operations: List<SaveOperation>,
  onBack: () -> Unit,
  onRestoreOperation: (SaveOperation) -> Unit,
  modifier: Modifier = Modifier
) {
  val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
      }
      Text(
        text = "MODIFICATION & BACKUP AUDIT",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = NeonEmerald
      )
    }

    if (operations.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(52.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No modifications logged yet.\nWhen you modify coins or currencies, untouched backups and changes will appear here for 1-tap restore.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
      return
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(operations, key = { it.id }) { op ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CyberSurface),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(16.dp))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = op.fileName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
              )
              Text(
                text = dateFormat.format(Date(op.timestamp)),
                color = TextSecondary,
                fontSize = 10.sp
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Command: \"${op.command}\"",
              fontSize = 12.sp,
              color = NeonMint,
              fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CyberSurfaceContainer)
                .padding(8.dp)
            ) {
              op.changes.forEach { change ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = change.fieldKey, color = TextPrimary, fontSize = 11.sp)
                  Text(
                    text = "${change.oldValue} ➜ ${change.newValue}",
                    color = NeonEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              Button(
                onClick = { onRestoreOperation(op) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("restore_op_${op.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.Restore,
                  contentDescription = null,
                  tint = CyberBlack,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restore This State", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}
