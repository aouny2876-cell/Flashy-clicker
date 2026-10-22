package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ToolType
import com.example.ui.theme.*

@Composable
fun LevelUpDialog(
  newLevel: Int,
  onDismiss: () -> Unit
) {
  val unlockedTool = ToolType.entries.find { it.unlockLevel == newLevel }
  val multiplierGain = "+10%"

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = CyberSurface,
      border = androidx.compose.foundation.BorderStroke(
        2.dp,
        Brush.linearGradient(listOf(NeonCyan, NeonMagenta, NeonYellow))
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("level_up_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icon
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
              Brush.linearGradient(listOf(NeonMagenta, NeonYellow))
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = CyberBackground,
            modifier = Modifier.size(38.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "LEVEL UP!",
          style = MaterialTheme.typography.displaySmall,
          color = NeonYellow,
          fontWeight = FontWeight.Black,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "REACHED LEVEL $newLevel",
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CyberSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Passive Point Multiplier increased by $multiplierGain!",
              style = MaterialTheme.typography.bodyMedium,
              color = NeonGreen,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center
            )

            if (unlockedTool != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "⚡ NEW TOOL UNLOCKED: ${unlockedTool.displayName}!",
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dismiss_level_up_button")
        ) {
          Text(
            text = "CLAIM & KEEP CLICKING",
            color = CyberBackground,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    }
  }
}
