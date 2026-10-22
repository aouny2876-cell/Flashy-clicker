package com.example.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ToolType
import com.example.ui.theme.*
import com.example.viewmodel.GameUiState

@Composable
fun ToolDock(
  uiState: GameUiState,
  onToolSelected: (ToolType) -> Unit,
  onOpenStore: () -> Unit,
  onOpenRebirth: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("tool_dock"),
    shape = RoundedCornerShape(20.dp),
    color = CyberSurface.copy(alpha = 0.95f),
    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceVariant),
    tonalElevation = 8.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Unlocked Tools Quick Switcher
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (tool in ToolType.entries) {
          val isUnlocked = uiState.unlockedTools.contains(tool.id)
          val isEquipped = uiState.equippedTool == tool
          val icon = getToolVector(tool)
          val toolColor = Color(tool.primaryColorHex)

          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isEquipped) toolColor.copy(alpha = 0.25f)
                else if (isUnlocked) CyberSurfaceVariant
                else Color(0xFF0F101A)
              )
              .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) toolColor else if (isUnlocked) toolColor.copy(alpha = 0.4f) else Color(0xFF222438),
                shape = RoundedCornerShape(12.dp)
              )
              .clickable(enabled = isUnlocked) { onToolSelected(tool) }
              .testTag("tool_switch_${tool.id}"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isUnlocked) icon else Icons.Default.Lock,
              contentDescription = tool.displayName,
              tint = if (isEquipped) toolColor else if (isUnlocked) TextSecondary else TextMuted,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }

      // Rebirth Button (if level >= 20) or Store Button
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (uiState.isRebirthAvailable) {
          Button(
            onClick = onOpenRebirth,
            colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("rebirth_dock_button")
          ) {
            Icon(
              imageVector = Icons.Default.AutoMode,
              contentDescription = null,
              tint = CyberBackground,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "REBIRTH",
              color = CyberBackground,
              fontWeight = FontWeight.Black,
              style = MaterialTheme.typography.labelMedium
            )
          }
        }

        Button(
          onClick = onOpenStore,
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonCyan
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier.testTag("open_store_button")
        ) {
          Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = CyberBackground,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "STORE",
            color = CyberBackground,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    }
  }
}

fun getToolVector(tool: ToolType): ImageVector {
  return when (tool) {
    ToolType.WOODEN_POINTER -> Icons.Default.TouchApp
    ToolType.NEON_CYBER_DAGGER -> Icons.Default.Colorize
    ToolType.MJOLNIR_HAMMER -> Icons.Default.Bolt
    ToolType.COSMIC_SCYTHE -> Icons.Default.AllInclusive
    ToolType.SOLAR_FLARE_STAFF -> Icons.Default.WbSunny
  }
}
