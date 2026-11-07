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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CurrencyField
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
fun FileViewerScreen(
  saveFile: SaveFile?,
  onBack: () -> Unit,
  onApplyFieldModification: (String, Long) -> Unit,
  onExportClick: () -> Unit,
  onRestoreBackupClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Currency Fields, 1: Raw Structure
  var fieldToEdit by remember { mutableStateOf<CurrencyField?>(null) }
  var editValueText by remember { mutableStateOf("") }

  if (saveFile == null) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(CyberBackground),
      contentAlignment = Alignment.Center
    ) {
      Text("No file currently loaded", color = TextSecondary)
    }
    return
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = saveFile.name,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = TextPrimary
        )
        Text(
          text = "${saveFile.format.label} • ${saveFile.sizeBytes} bytes",
          fontSize = 11.sp,
          color = NeonMint
        )
      }

      IconButton(onClick = onExportClick) {
        Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = NeonEmerald)
      }
      IconButton(onClick = onRestoreBackupClick) {
        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = TextSecondary)
      }
    }

    // Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = CyberSurface,
      contentColor = NeonEmerald,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = NeonEmerald
        )
      }
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Text(
            text = "Currencies (${saveFile.parsedFields.size})",
            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
            color = if (selectedTab == 0) NeonEmerald else TextSecondary
          )
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Text(
            text = "Raw File Preview",
            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
            color = if (selectedTab == 1) NeonEmerald else TextSecondary
          )
        }
      )
    }

    if (selectedTab == 0) {
      // Currency fields list
      if (saveFile.parsedFields.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No candidate numeric currencies automatically identified in this file.\nCheck raw file tab or ensure save is unencrypted.",
            color = TextSecondary,
            fontSize = 13.sp
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(saveFile.parsedFields) { field ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = CyberSurface),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(14.dp))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberSurfaceContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = field.category.icon, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = field.key,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                  )
                  Text(
                    text = "Path: ${field.path} • ${field.category.displayName}",
                    fontSize = 11.sp,
                    color = TextSecondary
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "Value: ${field.currentValue}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonEmerald
                  )
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceContainer)
                    .clickable {
                      fieldToEdit = field
                      editValueText = field.numericValue?.toString() ?: field.currentValue
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("edit_field_${field.key}"),
                  contentAlignment = Alignment.Center
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit",
                      tint = NeonMint,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Set",
                      color = NeonMint,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }
        }
      }
    } else {
      // Raw file preview
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
      ) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(CyberSurfaceContainer)
              .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Text(
              text = saveFile.content.take(8000) + if (saveFile.content.length > 8000) "\n\n...[Truncated for performance]" else "",
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              lineHeight = 16.sp,
              color = TextPrimary
            )
          }
        }
      }
    }
  }

  // Edit single field dialog
  fieldToEdit?.let { targetField ->
    Dialog(onDismissRequest = { fieldToEdit = null }) {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(18.dp))
          .padding(4.dp)
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "⚡ MODIFY ${targetField.key.uppercase()}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = NeonEmerald
          )

          Text(
            text = "Current Value: ${targetField.currentValue} (${targetField.path})",
            fontSize = 12.sp,
            color = TextSecondary
          )

          OutlinedTextField(
            value = editValueText,
            onValueChange = { editValueText = it },
            label = { Text("Target Integer Amount", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonEmerald,
              unfocusedBorderColor = DeepEmeraldBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("manual_edit_input")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { editValueText = "999999999" },
              colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceContainer),
              modifier = Modifier.weight(1f)
            ) {
              Text("Max (999M)", fontSize = 11.sp, color = NeonEmerald)
            }
            Button(
              onClick = { editValueText = "500000" },
              colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceContainer),
              modifier = Modifier.weight(1f)
            ) {
              Text("500,000", fontSize = 11.sp, color = NeonEmerald)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Button(
              onClick = { fieldToEdit = null },
              colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceContainer)
            ) {
              Text("Cancel", color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                val newVal = editValueText.toLongOrNull() ?: 999999999L
                onApplyFieldModification(targetField.path, newVal)
                fieldToEdit = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
              modifier = Modifier.testTag("confirm_manual_edit_button")
            ) {
              Text("Apply", color = CyberBlack, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
