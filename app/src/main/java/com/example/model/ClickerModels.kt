package com.example.model

enum class ToolType(
  val id: String,
  val displayName: String,
  val description: String,
  val multiplier: Double,
  val critBonus: Double,
  val unlockLevel: Int,
  val unlockCost: Double,
  val primaryColorHex: Long,
  val accentColorHex: Long,
  val iconName: String
) {
  WOODEN_POINTER(
    id = "wooden_pointer",
    displayName = "Wooden Pointer",
    description = "Standard tactile cursor with spark impacts.",
    multiplier = 1.0,
    critBonus = 0.0,
    unlockLevel = 1,
    unlockCost = 0.0,
    primaryColorHex = 0xFFD4A373,
    accentColorHex = 0xFFFFFFFF,
    iconName = "touch_app"
  ),
  NEON_CYBER_DAGGER(
    id = "neon_cyber_dagger",
    displayName = "Neon Cyber-Dagger",
    description = "Laser blade that slices across target, leaving glowing trails.",
    multiplier = 2.5,
    critBonus = 0.05,
    unlockLevel = 3,
    unlockCost = 350.0,
    primaryColorHex = 0xFF00F0FF,
    accentColorHex = 0xFFFF007F,
    iconName = "colorize"
  ),
  MJOLNIR_HAMMER(
    id = "mjolnir_hammer",
    displayName = "Mjolnir’s Hammer",
    description = "Sends arcs of electric lightning across screen with thunder impact.",
    multiplier = 6.0,
    critBonus = 0.10,
    unlockLevel = 8,
    unlockCost = 2500.0,
    primaryColorHex = 0xFFFFE600,
    accentColorHex = 0xFF38BDF8,
    iconName = "bolt"
  ),
  COSMIC_SCYTHE(
    id = "cosmic_scythe",
    displayName = "Cosmic Scythe",
    description = "Tears open a mini black hole, pulling floating bonus particles.",
    multiplier = 15.0,
    critBonus = 0.15,
    unlockLevel = 16,
    unlockCost = 20000.0,
    primaryColorHex = 0xFFA855F7,
    accentColorHex = 0xFFEC4899,
    iconName = "all_inclusive"
  ),
  SOLAR_FLARE_STAFF(
    id = "solar_flare_staff",
    displayName = "Solar Flare Staff",
    description = "Solar burst explosions expanding outward, detonating bonus nodes.",
    multiplier = 40.0,
    critBonus = 0.25,
    unlockLevel = 25,
    unlockCost = 150000.0,
    primaryColorHex = 0xFFFF4D00,
    accentColorHex = 0xFFFFD600,
    iconName = "wb_sunny"
  );

  companion object {
    fun fromId(id: String): ToolType = entries.find { it.id == id } ?: WOODEN_POINTER
  }
}

enum class AutoClickerType(
  val id: String,
  val displayName: String,
  val baseCost: Double,
  val basePps: Double,
  val costMultiplier: Double,
  val iconName: String
) {
  CLICK_BOT("bot", "Click Bot", 15.0, 0.5, 1.15, "smart_toy"),
  QUANTUM_SERVER("quantum", "Quantum Server", 120.0, 4.0, 1.15, "memory"),
  CYBER_DRONE("drone", "Cyber Drone", 1100.0, 32.0, 1.15, "flight"),
  AI_OVERMIND("overmind", "AI Overmind", 12000.0, 260.0, 1.15, "psychology"),
  DYSON_REACTOR("dyson", "Dyson Reactor", 130000.0, 1800.0, 1.15, "wb_iridescent"),
  COSMIC_SINGULARITY("singularity", "Cosmic Singularity", 1500000.0, 15000.0, 1.15, "flare");

  fun getCost(currentOwned: Int): Double {
    return baseCost * Math.pow(costMultiplier, currentOwned.toDouble())
  }
}

enum class UpgradeType(
  val id: String,
  val displayName: String,
  val description: String,
  val baseCost: Double,
  val costMultiplier: Double,
  val maxLevel: Int,
  val isCosmetic: Boolean = false
) {
  BASE_CLICK_POWER("click_power", "Overclock Click", "+2 Base points per click rank", 25.0, 1.35, 50),
  CRIT_CHANCE("crit_chance", "Precision Tuning", "+1% Critical Hit chance", 75.0, 1.45, 25),
  CRIT_DAMAGE("crit_damage", "Lethal Voltage", "+0.5x Critical Hit multiplier", 150.0, 1.5, 30),
  COMBO_EXTENDER("combo_window", "Flow Matrix", "+0.1s Combo window duration", 300.0, 1.6, 10),
  EXP_ACCELERATOR("exp_boost", "EXP Surge", "+20% EXP gain from clicks", 200.0, 1.4, 25),
  VFX_GOLDEN_AURA("vfx_golden", "Golden Aura", "Cosmetic: Radiates golden solar aura around target core", 5000.0, 1.0, 1, isCosmetic = true),
  VFX_RAINBOW_TRAILS("vfx_rainbow", "Rainbow Particles", "Cosmetic: Prismatic rainbow particle spectrum", 15000.0, 1.0, 1, isCosmetic = true),
  VFX_HYPER_GLOW("vfx_hyper", "Hyper Neon Bloom", "Cosmetic: Intense synthwave glow & reactive floor grid", 40000.0, 1.0, 1, isCosmetic = true);

  fun getCost(currentLevel: Int): Double {
    if (isCosmetic) return baseCost
    return baseCost * Math.pow(costMultiplier, currentLevel.toDouble())
  }
}

enum class PrestigeUpgradeType(
  val id: String,
  val displayName: String,
  val description: String,
  val costInShards: Int,
  val maxLevel: Int
) {
  DIVINE_HARVEST("divine_harvest", "Divine Harvest", "+50% PPS generation", 1, 10),
  COSMIC_FOCUS("cosmic_focus", "Cosmic Focus", "+100% Base click power", 2, 10),
  CELESTIAL_CRIT("celestial_crit", "Celestial Crit", "+5% Permanent crit chance", 3, 5),
  SUPERNOVA_COMBO("supernova_combo", "Supernova Combo", "+1x Max combo multiplier (up to 15x)", 5, 5);

  fun getCost(level: Int): Int = costInShards * (level + 1)
}

enum class NodeKind(val colorHex: Long, val label: String) {
  GOLDEN_ORB(0xFFFFD600, "ORB"),
  LUCKY_STAR(0xFF00F0FF, "STAR"),
  COMET_CORE(0xFFFF007F, "COMET")
}

data class BonusNode(
  val id: Long,
  val x: Float,
  val y: Float,
  val vx: Float,
  val vy: Float,
  val radius: Float = 28f,
  val kind: NodeKind,
  val pointsReward: Double,
  val expReward: Double,
  var lifeTime: Float = 10f
)

data class FloatingTextItem(
  val id: Long,
  val text: String,
  val x: Float,
  val y: Float,
  val colorHex: Long,
  val scale: Float = 1.0f,
  val alpha: Float = 1.0f,
  val isCrit: Boolean = false,
  val isLevelUp: Boolean = false
)

enum class ParticleKind {
  SPARK,
  TRAIL,
  LIGHTNING,
  VORTEX,
  SOLAR,
  CONFETTI
}

data class ParticleItem(
  val id: Long,
  val x: Float,
  val y: Float,
  val vx: Float,
  val vy: Float,
  val colorHex: Long,
  val size: Float,
  val alpha: Float,
  val life: Float,
  val maxLife: Float,
  val kind: ParticleKind
)

data class LightningArc(
  val points: List<Pair<Float, Float>>,
  val alpha: Float,
  val colorHex: Long,
  val width: Float
)

data class SliceLine(
  val startX: Float,
  val startY: Float,
  val endX: Float,
  val endY: Float,
  val alpha: Float,
  val colorHex: Long
)

data class BlackHoleVortex(
  val x: Float,
  val y: Float,
  val radius: Float,
  val rotation: Float,
  val alpha: Float
)

data class SolarShockwave(
  val x: Float,
  val y: Float,
  val currentRadius: Float,
  val maxRadius: Float,
  val alpha: Float
)

data class Achievement(
  val id: String,
  val title: String,
  val description: String,
  val isUnlocked: Boolean,
  val rewardShards: Int,
  val progress: String = ""
)
