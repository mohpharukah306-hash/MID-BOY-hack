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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApkMetadata
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.CyberYellow
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ApkInspectorScreen(
  metadata: ApkMetadata?,
  onBack: () -> Unit,
  onImportOtherFile: () -> Unit,
  modifier: Modifier = Modifier
) {
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
        text = "APK SECURITY & ARCHIVE AUDIT",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = NeonEmerald
      )
    }

    if (metadata == null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = NeonEmerald,
            modifier = Modifier.size(56.dp)
          )
          Text(
            text = "No APK currently inspected.\nImport an APK file to audit offline storage feasibility, game engines, and anti-cheat protections.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Button(
            onClick = onImportOtherFile,
            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = CyberBlack)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Import APK File", color = CyberBlack, fontWeight = FontWeight.Bold)
          }
        }
      }
      return
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Offline Status Banner
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CyberSurface),
          modifier = Modifier
            .fillMaxWidth()
            .border(
              1.dp,
              if (metadata.isOfflineGame) NeonEmerald else CyberYellow,
              RoundedCornerShape(16.dp)
            )
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = if (metadata.isOfflineGame) Icons.Default.CheckCircle else Icons.Default.Warning,
              contentDescription = null,
              tint = if (metadata.isOfflineGame) NeonEmerald else CyberYellow,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (metadata.isOfflineGame) "Standalone Offline Game Architecture" else "Online / Hybrid Services Detected",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = metadata.offlineConfidenceReason,
                fontSize = 12.sp,
                color = TextSecondary
              )
            }
          }
        }
      }

      // App Metadata Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CyberSurface),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(16.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "📦 Package Identification", fontWeight = FontWeight.Bold, color = NeonEmerald, fontSize = 13.sp)
            Text(text = "App Name: ${metadata.appName}", color = TextPrimary, fontSize = 13.sp)
            Text(text = "Target Package: ${metadata.packageName}", color = TextPrimary, fontSize = 13.sp)
            Text(text = "Archive Entries: ${metadata.versionCode} files (${metadata.fileSize} bytes)", color = TextSecondary, fontSize = 12.sp)
            Text(text = "Detected Engine: ${metadata.detectedGameEngines.joinToString()}", color = NeonMint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      // Embedded Balance Assets inside APK
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CyberSurface),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(16.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "🎮 Detected Asset Configs (${metadata.detectedConfigAssets.size})", fontWeight = FontWeight.Bold, color = NeonEmerald, fontSize = 13.sp)
            if (metadata.detectedConfigAssets.isEmpty()) {
              Text(text = "No plain text JSON/XML balancing tables discovered in root assets folder.", color = TextSecondary, fontSize = 12.sp)
            } else {
              metadata.detectedConfigAssets.forEach { asset ->
                Text(text = " • $asset", color = TextPrimary, fontSize = 12.sp)
              }
            }
          }
        }
      }

      // Legal & Technical Architecture Notice
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = CyberSurfaceContainer),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(16.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "🛡️ Safe Save Protocol Notice", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
            metadata.warnings.forEach { warning ->
              Text(text = "• $warning", color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
            }
          }
        }
      }
    }
  }
}
