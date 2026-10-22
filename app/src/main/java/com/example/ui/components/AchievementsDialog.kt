package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Achievement
import com.example.ui.theme.*
import com.example.viewmodel.ClickerViewModel
import com.example.viewmodel.GameUiState

@Composable
fun AchievementsDialog(
  uiState: GameUiState,
  achievements: List<Achievement>,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = CyberBackgroundSecondary,
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonYellow.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .testTag("achievements_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = NeonYellow,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ACHIEVEMENTS",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = NeonYellow
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_achievements_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Deep Player Stats Summary Card
        Card(
          colors = CardDefaults.cardColors(containerColor = CyberSurface),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            Text(
              text = "GAMEPLAY STATS",
              style = MaterialTheme.typography.labelSmall,
              color = NeonCyan,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              StatColumn("TOTAL CLICKS", "${uiState.totalClicks}")
              StatColumn("CRITICAL HITS", "${uiState.totalCrits}")
              StatColumn("MAX COMBO", "${uiState.highestCombo}x")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              StatColumn("LIFETIME PTS", ClickerViewModel.formatNumber(uiState.lifetimePoints))
              StatColumn("REBIRTHS", "${uiState.prestigeRebirths}")
              StatColumn("COSMIC SHARDS", "${uiState.cosmicShards}")
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Achievements List
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 12.dp)
        ) {
          items(achievements) { ach ->
            val unlocked = ach.isUnlocked
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .border(
                  1.dp,
                  if (unlocked) NeonYellow.copy(alpha = 0.5f) else CyberSurfaceVariant,
                  RoundedCornerShape(12.dp)
                ),
              colors = CardDefaults.cardColors(
                containerColor = if (unlocked) CyberSurface else CyberSurfaceVariant.copy(alpha = 0.4f)
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (unlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                  contentDescription = null,
                  tint = if (unlocked) NeonYellow else TextMuted,
                  modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = ach.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) Color.White else TextMuted
                  )
                  Text(
                    text = ach.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (unlocked) TextSecondary else TextMuted,
                    fontSize = 11.sp
                  )
                  if (ach.progress.isNotEmpty() && !unlocked) {
                    Text(
                      text = "Progress: ${ach.progress}",
                      style = MaterialTheme.typography.labelSmall,
                      color = NeonCyan,
                      fontSize = 10.sp
                    )
                  }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (unlocked) NeonYellow.copy(alpha = 0.2f) else CyberSurfaceVariant
                ) {
                  Text(
                    text = "+${ach.rewardShards} ⭐",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unlocked) NeonYellow else TextMuted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StatColumn(title: String, value: String) {
  Column {
    Text(title, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
    Text(value, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
  }
}
