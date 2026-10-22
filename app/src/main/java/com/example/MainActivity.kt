package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BonusNode
import com.example.model.ToolType
import com.example.ui.components.*
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ClickerViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: ClickerViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        var isStoreOpen by remember { mutableStateOf(false) }
        var initialStoreTab by remember { mutableStateOf(StoreTab.TOOLS) }
        var isAchievementsOpen by remember { mutableStateOf(false) }
        var showRebirthConfirmDialog by remember { mutableStateOf(false) }

        // BackHandler
        BackHandler(enabled = isStoreOpen || isAchievementsOpen || showRebirthConfirmDialog) {
          if (showRebirthConfirmDialog) showRebirthConfirmDialog = false
          else if (isAchievementsOpen) isAchievementsOpen = false
          else if (isStoreOpen) isStoreOpen = false
        }

        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("main_scaffold"),
          containerColor = CyberBackground,
          contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
              .testTag("root_game_container")
          ) {
            // 1. Central Game Canvas (Interactive Arena, Core, Particles, Background)
            GameCanvas(
              uiState = uiState,
              onTargetClick = { x, y -> viewModel.clickTarget(x, y) },
              onBonusNodeClick = { node -> viewModel.collectBonusNode(node) },
              modifier = Modifier.fillMaxSize()
            )

            // 2. Top Stats Bar (Points, PPS, Level/EXP progress, Combo streak)
            TopStatsBar(
              uiState = uiState,
              onToggleSound = { viewModel.toggleSound() },
              onToggleHaptics = { viewModel.toggleHaptics() },
              onOpenAchievements = { isAchievementsOpen = true },
              modifier = Modifier.align(Alignment.TopCenter)
            )

            // 3. Bottom Tool Dock & Quick Store button
            ToolDock(
              uiState = uiState,
              onToolSelected = { tool -> viewModel.equipTool(tool) },
              onOpenStore = {
                initialStoreTab = StoreTab.TOOLS
                isStoreOpen = true
              },
              onOpenRebirth = {
                initialStoreTab = StoreTab.PRESTIGE
                isStoreOpen = true
              },
              modifier = Modifier.align(Alignment.BottomCenter)
            )

            // 4. Store Modal Sheet
            if (isStoreOpen) {
              StoreSheet(
                uiState = uiState,
                initialTab = initialStoreTab,
                onDismiss = { isStoreOpen = false },
                onEquipTool = { tool -> viewModel.equipTool(tool) },
                onUnlockTool = { tool -> viewModel.unlockTool(tool) },
                onBuyAutoClicker = { auto -> viewModel.buyAutoClicker(auto) },
                onBuyUpgrade = { upg -> viewModel.buyUpgrade(upg) },
                onPerformRebirth = {
                  isStoreOpen = false
                  showRebirthConfirmDialog = true
                },
                onBuyPrestigeUpgrade = { prestigeUpg -> viewModel.buyPrestigeUpgrade(prestigeUpg) }
              )
            }

            // 5. Level Up Celebration Dialog
            if (uiState.levelUpEvent != null) {
              LevelUpDialog(
                newLevel = uiState.levelUpEvent!!,
                onDismiss = { viewModel.dismissLevelUpDialog() }
              )
            }

            // 6. Achievements & Stats Dialog
            if (isAchievementsOpen) {
              AchievementsDialog(
                uiState = uiState,
                achievements = viewModel.getAchievementsList(),
                onDismiss = { isAchievementsOpen = false }
              )
            }

            // 7. Rebirth Confirmation Dialog
            if (showRebirthConfirmDialog) {
              AlertDialog(
                onDismissRequest = { showRebirthConfirmDialog = false },
                title = {
                  Text(
                    text = "CONFIRM COSMIC REBIRTH",
                    style = MaterialTheme.typography.titleMedium,
                    color = com.example.ui.theme.NeonYellow
                  )
                },
                text = {
                  Text(
                    text = "Are you ready to ascend? Your current points, levels, and auto-clickers will reset in exchange for:\n\n" +
                        "• +${uiState.shardsOnRebirth} Cosmic Shards\n" +
                        "• +50% Permanent Divine Multiplier\n" +
                        "• Total Divine Boost: +${(uiState.prestigeRebirths + 1) * 50}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = com.example.ui.theme.TextPrimary
                  )
                },
                confirmButton = {
                  Button(
                    onClick = {
                      showRebirthConfirmDialog = false
                      viewModel.performRebirth()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.NeonYellow),
                    modifier = Modifier.testTag("confirm_rebirth_action")
                  ) {
                    Text(
                      text = "ASCEND NOW",
                      color = CyberBackground,
                      style = MaterialTheme.typography.labelMedium
                    )
                  }
                },
                dismissButton = {
                  TextButton(
                    onClick = { showRebirthConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_rebirth_action")
                  ) {
                    Text("NOT YET", color = com.example.ui.theme.TextMuted)
                  }
                },
                containerColor = com.example.ui.theme.CyberSurface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
              )
            }
          }
        }
      }
    }
  }
}
