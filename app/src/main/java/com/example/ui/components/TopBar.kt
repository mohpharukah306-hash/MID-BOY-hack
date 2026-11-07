package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberPillGreen
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary

@Composable
fun TopBar(
  activeFileName: String?,
  onMenuClick: () -> Unit,
  onPillClick: () -> Unit,
  onActionClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left circular hamburger button matching user image
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(CyberSurfaceContainer)
        .border(1.dp, DeepEmeraldBorder, CircleShape)
        .clickable { onMenuClick() }
        .testTag("top_menu_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Menu,
        contentDescription = "Menu",
        tint = TextPrimary,
        modifier = Modifier.size(20.dp)
      )
    }

    // Center bright green pill button matching user image ("Get Plus ✦" -> "MID PRO ⚡" or file name)
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(24.dp))
        .background(CyberPillGreen)
        .clickable { onPillClick() }
        .padding(horizontal = 18.dp, vertical = 9.dp)
        .testTag("top_status_pill"),
      contentAlignment = Alignment.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = if (activeFileName != null) "HACK READY ⚡" else "MID HACKER ✦",
          color = CyberBlack,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          letterSpacing = 0.5.sp
        )
      }
    }

    // Right circular action button matching user image
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(CyberSurfaceContainer)
        .border(1.dp, DeepEmeraldBorder, CircleShape)
        .clickable { onActionClick() }
        .testTag("top_action_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "New / Sample",
        tint = TextPrimary,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}
