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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleSaves
import com.example.model.SaveFile
import com.example.ui.Screen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class QuickActionItem(
  val title: String,
  val icon: ImageVector,
  val command: String,
  val targetScreen: Screen? = null
)

data class ToolItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val iconBg: Color = CyberSurfaceContainer,
  val action: () -> Unit
)

@Composable
fun HomeScreen(
  currentFile: SaveFile?,
  onImportClick: () -> Unit,
  onInspectApkClick: () -> Unit,
  onQuickCommand: (String) -> Unit,
  onLoadSample: (SaveFile) -> Unit,
  onNavigateToScreen: (Screen) -> Unit,
  onRestoreBackupClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val quickActions = listOf(
    QuickActionItem("Give unlimited coins", Icons.Default.Bolt, "Give me unlimited coins"),
    QuickActionItem("Max 500k diamonds", Icons.Default.Diamond, "Give me 500000 diamonds"),
    QuickActionItem("Analyze save data", Icons.Default.Psychology, "Analyze my game save", Screen.CHAT),
    QuickActionItem("Level 99 & XP", Icons.Default.Stars, "Set my level to 99 and max xp"),
    QuickActionItem("Max all currencies", Icons.Default.AutoFixHigh, "Max all currencies to 999999999"),
    QuickActionItem("Scan save orb", Icons.Default.Tune, "", Screen.SCAN_ORB),
    QuickActionItem("Inspect APK", Icons.Default.Security, "", Screen.APK_INSPECTOR)
  )

  val samples = SampleSaves.getSamples()

  val toolItems = listOf(
    ToolItem(
      id = "tool_1",
      title = "RPG Heroes Save (.json)",
      subtitle = "4 Currencies Found • 18,200 Coins • Backup Active",
      icon = Icons.Default.Description,
      action = { onLoadSample(samples[0]) }
    ),
    ToolItem(
      id = "tool_2",
      title = "Speed Drift SharedPrefs (.xml)",
      subtitle = "Android XML • Cash, Gold Bars & Nitro Boost",
      icon = Icons.Default.Tune,
      action = { onLoadSample(samples[1]) }
    ),
    ToolItem(
      id = "tool_3",
      title = "Cyber Mecha State (.ini)",
      subtitle = "INI Config • Credits, Plasma Cores, Nanites",
      icon = Icons.Default.Bolt,
      action = { onLoadSample(samples[2]) }
    ),
    ToolItem(
      id = "tool_4",
      title = "APK Security & Engine Inspector",
      subtitle = "Audit permissions, game engine & anti-cheat",
      icon = Icons.Default.Security,
      action = { onNavigateToScreen(Screen.APK_INSPECTOR) }
    ),
    ToolItem(
      id = "tool_5",
      title = "Operation History & Backups",
      subtitle = "Audit past modifications & restore previous state",
      icon = Icons.Default.History,
      action = { onNavigateToScreen(Screen.HISTORY) }
    )
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. User greeting matching phone 1 in screenshot
    item {
      Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Hey, Hacker 👾",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Ready to Hack Save Data Today?",
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }
    }

    // 2. Active File Status Card (if loaded)
    if (currentFile != null) {
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = CyberSurface),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(18.dp))
            .clickable { onNavigateToScreen(Screen.FILE_VIEWER) }
            .testTag("active_file_banner_card")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(CyberSurfaceContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = currentFile.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
              )
              Text(
                text = "${currentFile.format.label} • ${currentFile.parsedFields.size} currencies detected",
                fontSize = 12.sp,
                color = NeonMint
              )
            }
            Icon(
              imageVector = Icons.Default.NorthEast,
              contentDescription = "View File",
              tint = TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // 3. Quick Action Grid (Pills with icons as shown in screenshot)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row 1
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          QuickPillButton(
            title = "Unlimited coins",
            icon = Icons.Default.Bolt,
            onClick = { onQuickCommand("Give me unlimited coins") },
            modifier = Modifier.weight(1f)
          )
          QuickPillButton(
            title = "Max diamonds",
            icon = Icons.Default.Diamond,
            onClick = { onQuickCommand("Give me 500000 diamonds") },
            modifier = Modifier.weight(1f)
          )
        }

        // Row 2
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          QuickPillButton(
            title = "Analyze structure",
            icon = Icons.Default.Psychology,
            onClick = { onQuickCommand("Analyze my game save") },
            modifier = Modifier.weight(1f)
          )
          QuickPillButton(
            title = "Max all currencies",
            icon = Icons.Default.AutoFixHigh,
            onClick = { onQuickCommand("Max all currencies") },
            modifier = Modifier.weight(1f)
          )
        }

        // Row 3
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          QuickPillButton(
            title = "AI Scanner Orb",
            icon = Icons.Default.Tune,
            onClick = { onNavigateToScreen(Screen.SCAN_ORB) },
            modifier = Modifier.weight(1f)
          )
          QuickPillButton(
            title = "Import File (SAF)",
            icon = Icons.Default.FolderOpen,
            onClick = { onImportClick() },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 4. "Recently Used Tools" section matching screenshot
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Recently Used Tools",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "See All",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = TextSecondary,
          modifier = Modifier.clickable { onNavigateToScreen(Screen.HISTORY) }
        )
      }
    }

    // 5. Stacked Tool Cards matching screenshot (Card with icon, title, diagonal arrow)
    items(toolItems) { tool ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(16.dp))
          .clickable { tool.action() }
          .testTag(tool.id)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(CyberSurfaceContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = tool.icon,
              contentDescription = null,
              tint = NeonEmerald,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = tool.title,
              color = TextPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = tool.subtitle,
              color = TextSecondary,
              fontSize = 11.sp,
              maxLines = 1
            )
          }

          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(CyberSurfaceContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.NorthEast,
              contentDescription = "Open",
              tint = NeonEmerald,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun QuickPillButton(
  title: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .background(CyberSurface)
      .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(18.dp))
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 12.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(CyberSurfaceContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = NeonEmerald,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = title,
        color = TextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1
      )
    }
  }
}
