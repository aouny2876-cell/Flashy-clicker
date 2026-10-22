package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AutoClickerType
import com.example.model.ToolType
import com.example.model.UpgradeType

data class SavedGameState(
  val points: Double,
  val lifetimePoints: Double,
  val totalClicks: Long,
  val totalCrits: Long,
  val highestCombo: Int,
  val playerLevel: Int,
  val currentExp: Double,
  val equippedToolId: String,
  val unlockedToolIds: Set<String>,
  val autoClickerCounts: Map<String, Int>,
  val upgradeLevels: Map<String, Int>,
  val prestigeRebirths: Int,
  val cosmicShards: Int,
  val prestigeUpgradeLevels: Map<String, Int>,
  val unlockedAchievements: Set<String>,
  val soundEnabled: Boolean,
  val hapticsEnabled: Boolean
)

class ClickerRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("flashy_clicker_save", Context.MODE_PRIVATE)

  fun loadGame(): SavedGameState {
    val points = java.lang.Double.longBitsToDouble(
      prefs.getLong("points", java.lang.Double.doubleToRawLongBits(0.0))
    )
    val lifetimePoints = java.lang.Double.longBitsToDouble(
      prefs.getLong("lifetimePoints", java.lang.Double.doubleToRawLongBits(0.0))
    )
    val currentExp = java.lang.Double.longBitsToDouble(
      prefs.getLong("currentExp", java.lang.Double.doubleToRawLongBits(0.0))
    )
    val totalClicks = prefs.getLong("totalClicks", 0L)
    val totalCrits = prefs.getLong("totalCrits", 0L)
    val highestCombo = prefs.getInt("highestCombo", 0)
    val playerLevel = prefs.getInt("playerLevel", 1).coerceAtLeast(1)
    val equippedToolId = prefs.getString("equippedToolId", ToolType.WOODEN_POINTER.id) ?: ToolType.WOODEN_POINTER.id

    val defaultUnlockedTools = setOf(ToolType.WOODEN_POINTER.id)
    val unlockedTools = prefs.getStringSet("unlockedToolIds", defaultUnlockedTools) ?: defaultUnlockedTools

    val autoClickers = mutableMapOf<String, Int>()
    for (type in AutoClickerType.entries) {
      autoClickers[type.id] = prefs.getInt("auto_${type.id}", 0)
    }

    val upgrades = mutableMapOf<String, Int>()
    for (type in UpgradeType.entries) {
      upgrades[type.id] = prefs.getInt("upg_${type.id}", 0)
    }

    val prestigeUpgrades = mutableMapOf<String, Int>()
    for (key in listOf("divine_harvest", "cosmic_focus", "celestial_crit", "supernova_combo")) {
      prestigeUpgrades[key] = prefs.getInt("prestige_upg_$key", 0)
    }

    val prestigeRebirths = prefs.getInt("prestigeRebirths", 0)
    val cosmicShards = prefs.getInt("cosmicShards", 0)
    val achievements = prefs.getStringSet("unlockedAchievements", emptySet()) ?: emptySet()
    val soundEnabled = prefs.getBoolean("soundEnabled", true)
    val hapticsEnabled = prefs.getBoolean("hapticsEnabled", true)

    return SavedGameState(
      points = points,
      lifetimePoints = lifetimePoints,
      totalClicks = totalClicks,
      totalCrits = totalCrits,
      highestCombo = highestCombo,
      playerLevel = playerLevel,
      currentExp = currentExp,
      equippedToolId = equippedToolId,
      unlockedToolIds = unlockedTools,
      autoClickerCounts = autoClickers,
      upgradeLevels = upgrades,
      prestigeRebirths = prestigeRebirths,
      cosmicShards = cosmicShards,
      prestigeUpgradeLevels = prestigeUpgrades,
      unlockedAchievements = achievements,
      soundEnabled = soundEnabled,
      hapticsEnabled = hapticsEnabled
    )
  }

  fun saveGame(state: SavedGameState) {
    prefs.edit()
      .putLong("points", java.lang.Double.doubleToRawLongBits(state.points))
      .putLong("lifetimePoints", java.lang.Double.doubleToRawLongBits(state.lifetimePoints))
      .putLong("currentExp", java.lang.Double.doubleToRawLongBits(state.currentExp))
      .putLong("totalClicks", state.totalClicks)
      .putLong("totalCrits", state.totalCrits)
      .putInt("highestCombo", state.highestCombo)
      .putInt("playerLevel", state.playerLevel)
      .putString("equippedToolId", state.equippedToolId)
      .putStringSet("unlockedToolIds", state.unlockedToolIds)
      .putInt("prestigeRebirths", state.prestigeRebirths)
      .putInt("cosmicShards", state.cosmicShards)
      .putStringSet("unlockedAchievements", state.unlockedAchievements)
      .putBoolean("soundEnabled", state.soundEnabled)
      .putBoolean("hapticsEnabled", state.hapticsEnabled)
      .apply {
        state.autoClickerCounts.forEach { (id, count) ->
          putInt("auto_$id", count)
        }
        state.upgradeLevels.forEach { (id, level) ->
          putInt("upg_$id", level)
        }
        state.prestigeUpgradeLevels.forEach { (id, level) ->
          putInt("prestige_upg_$id", level)
        }
      }
      .apply()
  }

  fun resetForRebirth(shardsGained: Int): SavedGameState {
    val current = loadGame()
    val newRebirths = current.prestigeRebirths + 1
    val newShards = current.cosmicShards + shardsGained

    val defaultUnlocked = setOf(ToolType.WOODEN_POINTER.id)
    val emptyAutos = AutoClickerType.entries.associate { it.id to 0 }
    val emptyUpgs = UpgradeType.entries.associate { it.id to 0 }

    val newState = current.copy(
      points = 0.0,
      playerLevel = 1,
      currentExp = 0.0,
      equippedToolId = ToolType.WOODEN_POINTER.id,
      unlockedToolIds = defaultUnlocked,
      autoClickerCounts = emptyAutos,
      upgradeLevels = emptyUpgs,
      prestigeRebirths = newRebirths,
      cosmicShards = newShards
    )
    saveGame(newState)
    return newState
  }
}
