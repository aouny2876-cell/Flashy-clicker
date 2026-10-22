package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.ClickerViewModel
import com.example.viewmodel.GameUiState

enum class StoreTab(val title: String) {
  TOOLS("TOOLS"),
  GENERATORS("AUTOS"),
  UPGRADES("UPGRADES"),
  PRESTIGE("REBIRTH")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSheet(
  uiState: GameUiState,
  initialTab: StoreTab = StoreTab.TOOLS,
  onDismiss: () -> Unit,
  onEquipTool: (ToolType) -> Unit,
  onUnlockTool: (ToolType) -> Unit,
  onBuyAutoClicker: (AutoClickerType) -> Unit,
  onBuyUpgrade: (UpgradeType) -> Unit,
  onPerformRebirth: () -> Unit,
  onBuyPrestigeUpgrade: (PrestigeUpgradeType) -> Unit
) {
  var selectedTab by remember { mutableStateOf(initialTab) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = CyberBackgroundSecondary,
    contentColor = TextPrimary,
    dragHandle = {
      BottomSheetDefaults.DragHandle(color = CyberSurfaceHigh)
    },
    modifier = Modifier.testTag("store_modal_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .padding(horizontal = 16.dp)
        .navigationBarsPadding()
    ) {
      // Store Header: Title and Points Balance
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "CYBER STORE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
          Text(
            text = "Equip tools & amplify your power",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = CyberSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = ClickerViewModel.formatNumber(uiState.points),
              style = MaterialTheme.typography.labelLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Tab Row
      TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = CyberSurface,
        contentColor = NeonCyan,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
            color = NeonCyan
          )
        },
        divider = {}
      ) {
        StoreTab.entries.forEach { tab ->
          Tab(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            text = {
              Text(
                text = tab.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedTab == tab) NeonCyan else TextMuted
              )
            },
            modifier = Modifier.testTag("store_tab_${tab.name.lowercase()}")
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Tab Content
      when (selectedTab) {
        StoreTab.TOOLS -> {
          ToolsTabContent(
            uiState = uiState,
            onEquip = onEquipTool,
            onUnlock = onUnlockTool
          )
        }
        StoreTab.GENERATORS -> {
          GeneratorsTabContent(
            uiState = uiState,
            onBuy = onBuyAutoClicker
          )
        }
        StoreTab.UPGRADES -> {
          UpgradesTabContent(
            uiState = uiState,
            onBuy = onBuyUpgrade
          )
        }
        StoreTab.PRESTIGE -> {
          PrestigeTabContent(
            uiState = uiState,
            onRebirth = onPerformRebirth,
            onBuyPrestige = onBuyPrestigeUpgrade
          )
        }
      }
    }
  }
}

// --- Tools Tab ---
@Composable
private fun ToolsTabContent(
  uiState: GameUiState,
  onEquip: (ToolType) -> Unit,
  onUnlock: (ToolType) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    items(ToolType.entries) { tool ->
      val isUnlocked = uiState.unlockedTools.contains(tool.id)
      val isEquipped = uiState.equippedTool == tool
      val canAfford = uiState.points >= tool.unlockCost
      val levelSatisfied = uiState.playerLevel >= tool.unlockLevel
      val toolColor = Color(tool.primaryColorHex)

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(
            width = if (isEquipped) 2.dp else 1.dp,
            color = if (isEquipped) toolColor else CyberSurfaceVariant,
            shape = RoundedCornerShape(16.dp)
          )
          .testTag("tool_card_${tool.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Tool Icon
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(toolColor.copy(alpha = 0.2f))
              .border(1.dp, toolColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = getToolVector(tool),
              contentDescription = tool.displayName,
              tint = toolColor,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          // Tool Info
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = tool.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              if (isEquipped) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = toolColor.copy(alpha = 0.25f)
                ) {
                  Text(
                    text = "ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = toolColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Text(
              text = tool.description,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Multiplier & Crit bonus tags
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "${tool.multiplier}x CLICK",
                style = MaterialTheme.typography.labelSmall,
                color = toolColor,
                fontWeight = FontWeight.Bold
              )
              if (tool.critBonus > 0) {
                Text(
                  text = "+${(tool.critBonus * 100).toInt()}% CRIT",
                  style = MaterialTheme.typography.labelSmall,
                  color = NeonYellow,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Action Button
          when {
            isEquipped -> {
              OutlinedButton(
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, toolColor.copy(alpha = 0.4f))
              ) {
                Text("EQUIPPED", color = toolColor, style = MaterialTheme.typography.labelSmall)
              }
            }
            isUnlocked -> {
              Button(
                onClick = { onEquip(tool) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("equip_button_${tool.id}")
              ) {
                Text("EQUIP", color = Color.White, style = MaterialTheme.typography.labelSmall)
              }
            }
            else -> {
              Button(
                onClick = { onUnlock(tool) },
                enabled = canAfford && levelSatisfied,
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (canAfford && levelSatisfied) NeonCyan else CyberSurfaceVariant,
                  disabledContainerColor = CyberSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("unlock_button_${tool.id}")
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = ClickerViewModel.formatNumber(tool.unlockCost),
                    color = if (canAfford && levelSatisfied) CyberBackground else TextMuted,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                  )
                  if (!levelSatisfied) {
                    Text(
                      text = "LVL ${tool.unlockLevel}",
                      color = NeonMagenta,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
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
}

// --- Generators Tab (Auto-Clickers) ---
@Composable
private fun GeneratorsTabContent(
  uiState: GameUiState,
  onBuy: (AutoClickerType) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    items(AutoClickerType.entries) { auto ->
      val owned = uiState.autoClickers[auto.id] ?: 0
      val cost = auto.getCost(owned)
      val canAfford = uiState.points >= cost
      val totalPps = auto.basePps * owned

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CyberSurfaceVariant, RoundedCornerShape(16.dp))
          .testTag("generator_card_${auto.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Icon
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(NeonCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when (auto) {
                AutoClickerType.CLICK_BOT -> Icons.Default.SmartToy
                AutoClickerType.QUANTUM_SERVER -> Icons.Default.Memory
                AutoClickerType.CYBER_DRONE -> Icons.Default.Flight
                AutoClickerType.AI_OVERMIND -> Icons.Default.Psychology
                AutoClickerType.DYSON_REACTOR -> Icons.Default.WbIridescent
                AutoClickerType.COSMIC_SINGULARITY -> Icons.Default.Flare
              },
              contentDescription = auto.displayName,
              tint = NeonCyan,
              modifier = Modifier.size(26.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          // Details
          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = auto.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = CyberSurfaceVariant
              ) {
                Text(
                  text = "x$owned",
                  style = MaterialTheme.typography.labelSmall,
                  color = NeonCyan,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = "+${auto.basePps}/s each (Total +${ClickerViewModel.formatNumber(totalPps)}/s)",
              style = MaterialTheme.typography.bodySmall,
              color = NeonGreen,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Buy Button
          Button(
            onClick = { onBuy(auto) },
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(
              containerColor = NeonCyan,
              disabledContainerColor = CyberSurfaceVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("buy_auto_${auto.id}")
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "BUY",
                color = if (canAfford) CyberBackground else TextMuted,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
              )
              Text(
                text = ClickerViewModel.formatNumber(cost),
                color = if (canAfford) CyberBackground else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }
  }
}

// --- Upgrades & VFX Tab ---
@Composable
private fun UpgradesTabContent(
  uiState: GameUiState,
  onBuy: (UpgradeType) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    items(UpgradeType.entries) { upg ->
      val currentLevel = uiState.upgrades[upg.id] ?: 0
      val isMax = currentLevel >= upg.maxLevel
      val cost = upg.getCost(currentLevel)
      val canAfford = uiState.points >= cost && !isMax

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(
            1.dp,
            if (upg.isCosmetic) NeonMagenta.copy(alpha = 0.5f) else CyberSurfaceVariant,
            RoundedCornerShape(16.dp)
          )
          .testTag("upgrade_card_${upg.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (upg.isCosmetic) NeonMagenta.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.15f)
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (upg.isCosmetic) Icons.Default.Palette else Icons.Default.TrendingUp,
              contentDescription = null,
              tint = if (upg.isCosmetic) NeonMagenta else NeonCyan,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = upg.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              if (upg.isCosmetic) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = NeonMagenta.copy(alpha = 0.25f)
                ) {
                  Text(
                    text = "VFX",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonMagenta,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Text(
              text = upg.description,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              fontSize = 11.sp
            )

            if (!upg.isCosmetic) {
              Text(
                text = "Rank: $currentLevel / ${upg.maxLevel}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontSize = 10.sp
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          if (isMax) {
            OutlinedButton(
              onClick = {},
              enabled = false,
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("MAX", color = NeonGreen, style = MaterialTheme.typography.labelSmall)
            }
          } else {
            Button(
              onClick = { onBuy(upg) },
              enabled = canAfford,
              colors = ButtonDefaults.buttonColors(
                containerColor = if (upg.isCosmetic) NeonMagenta else NeonCyan,
                disabledContainerColor = CyberSurfaceVariant
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("buy_upgrade_${upg.id}")
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "UPGRADE",
                  color = if (canAfford) CyberBackground else TextMuted,
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.labelSmall
                )
                Text(
                  text = ClickerViewModel.formatNumber(cost),
                  color = if (canAfford) CyberBackground else TextMuted,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }
  }
}

// --- Prestige (Rebirth) Tab ---
@Composable
private fun PrestigeTabContent(
  uiState: GameUiState,
  onRebirth: () -> Unit,
  onBuyPrestige: (PrestigeUpgradeType) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(bottom = 24.dp)
  ) {
    // Prestige Overview Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, NeonYellow.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(16.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "COSMIC REBIRTH",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NeonYellow
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Reset your level, tools & points to ascend to divinity. Earn permanent Divine Multipliers and Cosmic Shards to unlock godlike celestial powers!",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("REBIRTHS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                "${uiState.prestigeRebirths}",
                style = MaterialTheme.typography.titleLarge,
                color = NeonYellow,
                fontWeight = FontWeight.Bold
              )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("DIVINE BOOST", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                "+${uiState.prestigeRebirths * 50}%",
                style = MaterialTheme.typography.titleLarge,
                color = NeonGreen,
                fontWeight = FontWeight.Bold
              )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("COSMIC SHARDS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                "${uiState.cosmicShards}",
                style = MaterialTheme.typography.titleLarge,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onRebirth,
            enabled = uiState.isRebirthAvailable,
            colors = ButtonDefaults.buttonColors(
              containerColor = NeonYellow,
              disabledContainerColor = CyberSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("perform_rebirth_button")
          ) {
            Text(
              text = if (uiState.isRebirthAvailable)
                "REBIRTH NOW (+${uiState.shardsOnRebirth} SHARDS & +50% DIVINE)"
              else "REACH LEVEL 20 TO REBIRTH (Current: LVL ${uiState.playerLevel})",
              color = if (uiState.isRebirthAvailable) CyberBackground else TextMuted,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelMedium
            )
          }
        }
      }
    }

    item {
      Text(
        text = "COSMIC SHARD UPGRADES",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = NeonCyan
      )
    }

    // Shard Upgrade items
    items(PrestigeUpgradeType.entries) { upg ->
      val lvl = uiState.prestigeUpgrades[upg.id] ?: 0
      val cost = upg.getCost(lvl)
      val isMax = lvl >= upg.maxLevel
      val canAfford = uiState.cosmicShards >= cost && !isMax

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, CyberSurfaceVariant, RoundedCornerShape(14.dp))
          .testTag("prestige_upg_${upg.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(14.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = upg.displayName,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = upg.description,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              fontSize = 11.sp
            )
            Text(
              text = "Rank: $lvl / ${upg.maxLevel}",
              style = MaterialTheme.typography.labelSmall,
              color = NeonYellow,
              fontSize = 10.sp
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          if (isMax) {
            OutlinedButton(onClick = {}, enabled = false) {
              Text("MAX", color = NeonGreen)
            }
          } else {
            Button(
              onClick = { onBuyPrestige(upg) },
              enabled = canAfford,
              colors = ButtonDefaults.buttonColors(
                containerColor = NeonYellow,
                disabledContainerColor = CyberSurfaceVariant
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("buy_prestige_upg_${upg.id}")
            ) {
              Text(
                text = "$cost SHARDS",
                color = if (canAfford) CyberBackground else TextMuted,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
              )
            }
          }
        }
      }
    }
  }
}
