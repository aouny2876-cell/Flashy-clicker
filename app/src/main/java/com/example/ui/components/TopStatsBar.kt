package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ClickerViewModel
import com.example.viewmodel.GameUiState

@Composable
fun TopStatsBar(
  uiState: GameUiState,
  onToggleSound: () -> Unit,
  onToggleHaptics: () -> Unit,
  onOpenAchievements: () -> Unit,
  modifier: Modifier = Modifier
) {
  val levelMultiplier = (1.0 + (uiState.playerLevel - 1) * 0.10)
  val expProgress = (uiState.currentExp / uiState.maxExp).toFloat().coerceIn(0f, 1f)

  // Infinite pulsing animation for high combo
  val infiniteTransition = rememberInfiniteTransition(label = "combo_pulse")
  val comboGlow by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "combo_glow"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("top_stats_bar"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Row: App Title & Quick Control Toggles
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.linearGradient(listOf(NeonCyan, NeonMagenta))
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "FLASHY CLICKER",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 1.sp
          )
          if (uiState.prestigeRebirths > 0) {
            Text(
              text = "⭐ REBIRTH ${uiState.prestigeRebirths} (+${(uiState.prestigeRebirths * 50)}% DIVINE)",
              style = MaterialTheme.typography.labelSmall,
              color = NeonYellow,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Settings icons
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onToggleSound,
          modifier = Modifier
            .size(40.dp)
            .testTag("toggle_sound_button")
        ) {
          Icon(
            imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            contentDescription = "Toggle Sound",
            tint = if (uiState.soundEnabled) NeonCyan else TextMuted
          )
        }
        IconButton(
          onClick = onToggleHaptics,
          modifier = Modifier
            .size(40.dp)
            .testTag("toggle_haptics_button")
        ) {
          Icon(
            imageVector = if (uiState.hapticsEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
            contentDescription = "Toggle Haptics",
            tint = if (uiState.hapticsEnabled) NeonMagenta else TextMuted
          )
        }
        IconButton(
          onClick = onOpenAchievements,
          modifier = Modifier
            .size(40.dp)
            .testTag("open_achievements_button")
        ) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Achievements",
            tint = NeonYellow
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Main Points Display
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.5f), NeonMagenta.copy(alpha = 0.5f))), RoundedCornerShape(16.dp))
        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = NeonCyan),
      colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.88f)),
      shape = RoundedCornerShape(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = ClickerViewModel.formatNumber(uiState.points),
          style = MaterialTheme.typography.displayMedium,
          color = Color.White,
          fontWeight = FontWeight.Black,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("main_points_display")
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "PTS",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
          )
          Text(
            text = "+${ClickerViewModel.formatNumber(uiState.pointsPerSecond)}/s",
            style = MaterialTheme.typography.labelMedium,
            color = NeonGreen,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("pps_display")
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Level & EXP Progress Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = NeonMagenta.copy(alpha = 0.2f),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta)
        ) {
          Text(
            text = "LVL ${uiState.playerLevel}",
            style = MaterialTheme.typography.labelMedium,
            color = NeonMagenta,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "+${String.format(java.util.Locale.US, "%.0f", (levelMultiplier - 1.0) * 100)}% MULT",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary,
          fontWeight = FontWeight.SemiBold
        )
      }

      Text(
        text = "${ClickerViewModel.formatNumber(uiState.currentExp)} / ${ClickerViewModel.formatNumber(uiState.maxExp)} EXP",
        style = MaterialTheme.typography.labelSmall,
        color = NeonCyan,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // EXP Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(7.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(CyberSurfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(expProgress)
          .clip(RoundedCornerShape(4.dp))
          .background(
            Brush.horizontalGradient(listOf(NeonCyan, NeonMagenta))
          )
      )
    }

    // Dynamic Combo Streak Bar (shows up whenever combo >= 2)
    AnimatedVisibility(
      visible = uiState.comboStreak > 1,
      enter = fadeIn() + androidx.compose.animation.expandVertically(),
      exit = fadeOut() + androidx.compose.animation.shrinkVertically()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        val isMaxCombo = uiState.comboStreak >= uiState.maxComboMultiplier
        val comboColor = if (isMaxCombo) NeonYellow else NeonMagenta

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (isMaxCombo) "🔥 MAX COMBO" else "⚡ COMBO STREAK",
              style = MaterialTheme.typography.labelSmall,
              color = comboColor,
              fontWeight = FontWeight.Black
            )
          }

          Text(
            text = "${uiState.comboStreak}x MULTIPLIER",
            style = MaterialTheme.typography.labelMedium,
            color = comboColor,
            fontWeight = FontWeight.Black,
            modifier = Modifier.testTag("combo_multiplier_text")
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Combo Decay Bar
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(CyberSurfaceVariant)
        ) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth(uiState.comboProgress)
              .clip(RoundedCornerShape(3.dp))
              .background(
                Brush.horizontalGradient(
                  if (isMaxCombo) listOf(NeonYellow, NeonOrange)
                  else listOf(NeonCyan, NeonMagenta)
                )
              )
          )
        }
      }
    }
  }
}
