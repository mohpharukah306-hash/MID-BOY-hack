package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SaveFile
import com.example.ui.Screen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScanOrbScreen(
  currentFile: SaveFile?,
  onTriggerVoiceHack: (String) -> Unit,
  onClose: () -> Unit,
  onNavigateToFileViewer: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isListening by remember { mutableStateOf(true) }
  var commandTranscript by remember {
    mutableStateOf(
      if (currentFile != null) {
        "Target Loaded: ${currentFile.name}\nIdentified ${currentFile.parsedFields.size} currency keys (${currentFile.parsedFields.take(3).joinToString { it.key }}...)\nSay or tap 'Give me unlimited coins' to inject values."
      } else {
        "Awaiting game save file. Import a file or select a sample from Home."
      }
    )
  }

  val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  val ringScale1 by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(2500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ring1"
  )

  val ringAlpha1 by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(2500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "alpha1"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
      .padding(horizontal = 20.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Status matching screenshot ("Listening...")
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(top = 16.dp)
    ) {
      Text(
        text = if (isListening) "Listening..." else "Save Analysis Active",
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "MID AI NEURAL INTERFACE",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = NeonEmerald
      )
    }

    // Center Pulsing 3D Cyber Orb matching phone 2 in screenshot
    Box(
      modifier = Modifier
        .size(280.dp),
      contentAlignment = Alignment.Center
    ) {
      // Outer animated glowing wave rings
      Box(
        modifier = Modifier
          .size(230.dp)
          .scale(ringScale1)
          .border(2.dp, NeonEmerald.copy(alpha = ringAlpha1), CircleShape)
      )

      Box(
        modifier = Modifier
          .size(260.dp)
          .scale(ringScale1 * 0.9f)
          .border(1.dp, NeonMint.copy(alpha = ringAlpha1 * 0.7f), CircleShape)
      )

      // Center glowing orb
      Box(
        modifier = Modifier
          .size(210.dp)
          .scale(pulseScale)
          .clip(CircleShape)
          .border(3.dp, NeonEmerald, CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(NeonEmerald.copy(alpha = 0.4f), Color.Transparent)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        // High quality generated 3D Cyber Energy Orb image asset
        Image(
          painter = painterResource(id = R.drawable.cyber_energy_orb),
          contentDescription = "Cyber Energy Orb",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .testTag("cyber_energy_orb_view")
        )
      }
    }

    // Real-time instruction transcript matching phone 2 in screenshot
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(CyberSurfaceContainer.copy(alpha = 0.7f))
        .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(18.dp))
        .padding(16.dp)
    ) {
      Text(
        text = commandTranscript,
        color = TextPrimary,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Quick tap suggestion
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(NeonEmerald)
          .clickable {
            commandTranscript = "Injecting Unlimited Coins & Diamonds into ${currentFile?.name ?: "Save"}..."
            onTriggerVoiceHack("Give me unlimited coins and 500000 diamonds")
          }
          .padding(horizontal = 14.dp, vertical = 6.dp)
          .testTag("orb_auto_hack_button"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "⚡ TAP FOR INSTANT MAX COINS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = CyberBlack
        )
      }
    }

    // Bottom action row matching phone 2 in screenshot
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left icon: Document / File inspector
      Box(
        modifier = Modifier
          .size(50.dp)
          .clip(CircleShape)
          .background(CyberSurfaceContainer)
          .border(1.dp, DeepEmeraldBorder, CircleShape)
          .clickable { onNavigateToFileViewer() }
          .testTag("orb_file_viewer_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Description,
          contentDescription = "File Details",
          tint = TextPrimary,
          modifier = Modifier.size(22.dp)
        )
      }

      // Center huge glowing mic button matching phone 2
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(NeonEmerald)
          .clickable {
            isListening = !isListening
            if (isListening) {
              commandTranscript = "Listening... Speak or tap 'Give unlimited coins' to process."
            } else {
              commandTranscript = "Simulated Voice Capture: 'Set my coins to 999999999 and gold to 500000'"
              onTriggerVoiceHack("Set my coins to 999999999 and gold to 500000")
            }
          }
          .testTag("orb_main_mic_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Mic,
          contentDescription = "Mic Trigger",
          tint = CyberBlack,
          modifier = Modifier.size(32.dp)
        )
      }

      // Right close button ✕ matching phone 2
      Box(
        modifier = Modifier
          .size(50.dp)
          .clip(CircleShape)
          .background(CyberSurfaceContainer)
          .border(1.dp, DeepEmeraldBorder, CircleShape)
          .clickable { onClose() }
          .testTag("orb_close_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Close",
          tint = TextPrimary,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}
