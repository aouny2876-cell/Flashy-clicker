package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundAndHapticManager
import com.example.data.ClickerRepository
import com.example.data.SavedGameState
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

data class GameUiState(
  val points: Double = 0.0,
  val lifetimePoints: Double = 0.0,
  val totalClicks: Long = 0L,
  val totalCrits: Long = 0L,
  val highestCombo: Int = 0,
  val playerLevel: Int = 1,
  val currentExp: Double = 0.0,
  val maxExp: Double = 100.0,
  val pointsPerSecond: Double = 0.0,
  val comboStreak: Int = 1,
  val comboProgress: Float = 0f, // 1.0 down to 0.0
  val maxComboMultiplier: Int = 10,
  val equippedTool: ToolType = ToolType.WOODEN_POINTER,
  val unlockedTools: Set<String> = setOf(ToolType.WOODEN_POINTER.id),
  val autoClickers: Map<String, Int> = emptyMap(),
  val upgrades: Map<String, Int> = emptyMap(),
  val prestigeRebirths: Int = 0,
  val cosmicShards: Int = 0,
  val prestigeUpgrades: Map<String, Int> = emptyMap(),
  val unlockedAchievements: Set<String> = emptySet(),
  val soundEnabled: Boolean = true,
  val hapticsEnabled: Boolean = true,
  val screenShakeOffset: Pair<Float, Float> = Pair(0f, 0f),
  val targetScaleX: Float = 1.0f,
  val targetScaleY: Float = 1.0f,
  val particles: List<ParticleItem> = emptyList(),
  val floatingTexts: List<FloatingTextItem> = emptyList(),
  val bonusNodes: List<BonusNode> = emptyList(),
  val lightningArcs: List<LightningArc> = emptyList(),
  val sliceLines: List<SliceLine> = emptyList(),
  val blackHoles: List<BlackHoleVortex> = emptyList(),
  val solarShockwaves: List<SolarShockwave> = emptyList(),
  val levelUpEvent: Int? = null,
  val isRebirthAvailable: Boolean = false,
  val shardsOnRebirth: Int = 0
)

class ClickerViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ClickerRepository(application)
  val soundManager = SoundAndHapticManager(application)

  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  private var comboTimerJob: Job? = null
  private var lastClickTime: Long = 0L
  private var nextId: Long = 1L

  init {
    loadSavedState()
    startGameLoops()
  }

  private fun loadSavedState() {
    val saved = repository.loadGame()
    soundManager.soundEnabled = saved.soundEnabled
    soundManager.hapticsEnabled = saved.hapticsEnabled

    val equipped = ToolType.fromId(saved.equippedToolId)
    val maxExp = calculateMaxExpForLevel(saved.playerLevel)
    val pps = calculatePps(saved.autoClickerCounts, saved.prestigeRebirths, saved.prestigeUpgradeLevels)
    val maxCombo = 10 + (saved.prestigeUpgradeLevels["supernova_combo"] ?: 0)

    val shardsCanGet = calculateRebirthShards(saved.playerLevel, saved.lifetimePoints)

    _uiState.update {
      it.copy(
        points = saved.points,
        lifetimePoints = saved.lifetimePoints,
        totalClicks = saved.totalClicks,
        totalCrits = saved.totalCrits,
        highestCombo = saved.highestCombo,
        playerLevel = saved.playerLevel,
        currentExp = saved.currentExp,
        maxExp = maxExp,
        pointsPerSecond = pps,
        equippedTool = equipped,
        unlockedTools = saved.unlockedToolIds,
        autoClickers = saved.autoClickerCounts,
        upgrades = saved.upgradeLevels,
        prestigeRebirths = saved.prestigeRebirths,
        cosmicShards = saved.cosmicShards,
        prestigeUpgrades = saved.prestigeUpgradeLevels,
        unlockedAchievements = saved.unlockedAchievements,
        soundEnabled = saved.soundEnabled,
        hapticsEnabled = saved.hapticsEnabled,
        maxComboMultiplier = maxCombo,
        isRebirthAvailable = saved.playerLevel >= 20,
        shardsOnRebirth = shardsCanGet
      )
    }
  }

  private fun calculateMaxExpForLevel(level: Int): Double {
    return 100.0 * (1.18).pow((level - 1).toDouble())
  }

  private fun calculatePps(
    autoClickers: Map<String, Int>,
    rebirths: Int,
    prestigeUpgrades: Map<String, Int>
  ): Double {
    var rawPps = 0.0
    for (type in AutoClickerType.entries) {
      val count = autoClickers[type.id] ?: 0
      rawPps += type.basePps * count
    }
    val divineMult = 1.0 + (rebirths * 0.5)
    val harvestBoost = 1.0 + ((prestigeUpgrades["divine_harvest"] ?: 0) * 0.5)
    return rawPps * divineMult * harvestBoost
  }

  private fun calculateRebirthShards(level: Int, lifetime: Double): Int {
    if (level < 20) return 0
    val levelShards = (level - 19)
    val wealthShards = (log10(max(1.0, lifetime)) - 4).toInt().coerceAtLeast(0)
    return (levelShards + wealthShards).coerceAtLeast(1)
  }

  private fun startGameLoops() {
    // Auto-clicker ticker (runs 10 times per second for fluid progress)
    viewModelScope.launch {
      while (true) {
        delay(100)
        val current = _uiState.value
        if (current.pointsPerSecond > 0) {
          val addedPoints = current.pointsPerSecond / 10.0
          val newPoints = current.points + addedPoints
          val newLifetime = current.lifetimePoints + addedPoints
          _uiState.update {
            it.copy(
              points = newPoints,
              lifetimePoints = newLifetime,
              isRebirthAvailable = it.playerLevel >= 20,
              shardsOnRebirth = calculateRebirthShards(it.playerLevel, newLifetime)
            )
          }
        }
      }
    }

    // Animation & physics step ticker (runs every ~33ms, ~30fps for smooth canvas particles)
    viewModelScope.launch {
      while (true) {
        delay(33)
        updateVfxFrame()
      }
    }

    // Bonus node spawner loop (every 9 to 16 seconds)
    viewModelScope.launch {
      while (true) {
        delay(Random.nextLong(9000, 16000))
        spawnBonusNode()
      }
    }

    // Auto-save loop (every 5 seconds)
    viewModelScope.launch {
      while (true) {
        delay(5000)
        saveCurrentState()
      }
    }
  }

  // --- Click Logic ---

  fun clickTarget(clickX: Float, clickY: Float) {
    val state = _uiState.value
    val now = System.currentTimeMillis()

    // Combo window calculation
    val comboWindowBonus = (state.upgrades[UpgradeType.COMBO_EXTENDER.id] ?: 0) * 0.1
    val comboWindowMs = ((0.5 + comboWindowBonus) * 1000).toLong()

    val newComboStreak = if (now - lastClickTime <= comboWindowMs) {
      (state.comboStreak + 1).coerceAtMost(state.maxComboMultiplier)
    } else {
      1
    }
    lastClickTime = now
    startComboCountdown(comboWindowMs)

    if (newComboStreak > 1) {
      soundManager.playComboSound(newComboStreak)
    } else {
      soundManager.playClickSound()
    }
    soundManager.vibrateClick()

    // Critical Hit calculation
    val baseCrit = 0.05
    val toolCrit = state.equippedTool.critBonus
    val upgCrit = (state.upgrades[UpgradeType.CRIT_CHANCE.id] ?: 0) * 0.01
    val prestCrit = (state.prestigeUpgrades["celestial_crit"] ?: 0) * 0.05
    val totalCritChance = (baseCrit + toolCrit + upgCrit + prestCrit).coerceAtMost(0.90)

    val isCrit = Random.nextDouble() < totalCritChance

    // Multipliers
    val basePower = 1.0 + (state.upgrades[UpgradeType.BASE_CLICK_POWER.id] ?: 0) * 2.0
    val cosmicFocusMult = 1.0 + (state.prestigeUpgrades["cosmic_focus"] ?: 0) * 1.0
    val toolMult = state.equippedTool.multiplier
    val comboMult = newComboStreak.toDouble()
    val levelMult = 1.0 + (state.playerLevel - 1) * 0.10
    val divineMult = 1.0 + state.prestigeRebirths * 0.50

    val baseCritMultiplier = 5.0 + (state.upgrades[UpgradeType.CRIT_DAMAGE.id] ?: 0) * 0.5
    val critMult = if (isCrit) baseCritMultiplier else 1.0

    val earnedPoints = basePower * cosmicFocusMult * toolMult * comboMult * levelMult * divineMult * critMult

    // EXP Calculation
    val expBoost = 1.0 + (state.upgrades[UpgradeType.EXP_ACCELERATOR.id] ?: 0) * 0.20
    val earnedExp = (10.0 * expBoost * (1.0 + (newComboStreak - 1) * 0.15))

    val newTotalClicks = state.totalClicks + 1
    val newTotalCrits = state.totalCrits + (if (isCrit) 1 else 0)
    val newHighestCombo = max(state.highestCombo, newComboStreak)
    val newPoints = state.points + earnedPoints
    val newLifetime = state.lifetimePoints + earnedPoints

    // Screen Shake & Target Elasticity
    var shakeX = 0f
    var shakeY = 0f
    if (isCrit || state.equippedTool == ToolType.MJOLNIR_HAMMER || newComboStreak >= 8) {
      shakeX = (Random.nextFloat() - 0.5f) * (if (isCrit) 24f else 12f)
      shakeY = (Random.nextFloat() - 0.5f) * (if (isCrit) 24f else 12f)
      if (isCrit) {
        soundManager.playCritSound()
        soundManager.vibrateCrit()
      }
    }

    // Squash & Stretch target elasticity
    val stretchX = 1.15f
    val stretchY = 0.88f

    // Trigger Tool Special Active Effects
    triggerToolEffects(state.equippedTool, clickX, clickY, isCrit)

    // Spawn Particles & Floating Text
    spawnClickParticles(clickX, clickY, state.equippedTool, isCrit, newComboStreak)
    spawnFloatingText(clickX, clickY, earnedPoints, earnedExp, isCrit, newComboStreak)

    // EXP and Level Up check
    var currentExp = state.currentExp + earnedExp
    var currentLevel = state.playerLevel
    var currentMaxExp = state.maxExp
    var didLevelUp = false

    while (currentExp >= currentMaxExp) {
      currentExp -= currentMaxExp
      currentLevel++
      currentMaxExp = calculateMaxExpForLevel(currentLevel)
      didLevelUp = true
    }

    if (didLevelUp) {
      triggerLevelUp(currentLevel)
    }

    // Check achievements
    checkAchievements(newTotalClicks, newTotalCrits, newHighestCombo, currentLevel, newLifetime)

    _uiState.update {
      it.copy(
        points = newPoints,
        lifetimePoints = newLifetime,
        totalClicks = newTotalClicks,
        totalCrits = newTotalCrits,
        highestCombo = newHighestCombo,
        comboStreak = newComboStreak,
        comboProgress = 1.0f,
        playerLevel = currentLevel,
        currentExp = currentExp,
        maxExp = currentMaxExp,
        targetScaleX = stretchX,
        targetScaleY = stretchY,
        screenShakeOffset = Pair(shakeX, shakeY),
        isRebirthAvailable = currentLevel >= 20,
        shardsOnRebirth = calculateRebirthShards(currentLevel, newLifetime)
      )
    }
  }

  private fun startComboCountdown(durationMs: Long) {
    comboTimerJob?.cancel()
    comboTimerJob = viewModelScope.launch {
      val startTime = System.currentTimeMillis()
      while (true) {
        val elapsed = System.currentTimeMillis() - startTime
        val remaining = (durationMs - elapsed).coerceAtLeast(0)
        val progress = remaining.toFloat() / durationMs.toFloat()
        _uiState.update { it.copy(comboProgress = progress) }
        if (remaining <= 0) {
          _uiState.update { it.copy(comboStreak = 1, comboProgress = 0f) }
          break
        }
        delay(30)
      }
    }
  }

  // --- Tool Active Abilities ---

  private fun triggerToolEffects(tool: ToolType, x: Float, y: Float, isCrit: Boolean) {
    when (tool) {
      ToolType.WOODEN_POINTER -> {
        // Standard sparks
      }
      ToolType.NEON_CYBER_DAGGER -> {
        // Laser slice across the core
        val angle = Random.nextFloat() * Math.PI.toFloat()
        val length = 320f
        val dx = cos(angle) * length
        val dy = sin(angle) * length
        val line = SliceLine(
          startX = x - dx,
          startY = y - dy,
          endX = x + dx,
          endY = y + dy,
          alpha = 1.0f,
          colorHex = if (Random.nextBoolean()) 0xFF00F0FF else 0xFFFF007F
        )
        _uiState.update { it.copy(sliceLines = it.sliceLines + line) }
      }
      ToolType.MJOLNIR_HAMMER -> {
        // Branching electric lightning arcs
        soundManager.playMjolnirSound()
        soundManager.vibrateMjolnir()
        val arcs = mutableListOf<LightningArc>()
        repeat(Random.nextInt(3, 6)) {
          arcs.add(generateLightningArc(x, y))
        }
        _uiState.update { it.copy(lightningArcs = it.lightningArcs + arcs) }
      }
      ToolType.COSMIC_SCYTHE -> {
        // Mini black hole singularity pulling bonus particles & nodes
        soundManager.playCosmicSound()
        val vortex = BlackHoleVortex(
          x = x,
          y = y,
          radius = 160f,
          rotation = 0f,
          alpha = 1.0f
        )
        _uiState.update { current ->
          // Attract nearby bonus nodes directly into click!
          val attractedNodes = current.bonusNodes.map { node ->
            val dist = hypot(node.x - x, node.y - y)
            if (dist < 400f) {
              node.copy(
                vx = (x - node.x) * 0.3f,
                vy = (y - node.y) * 0.3f
              )
            } else node
          }
          current.copy(
            blackHoles = current.blackHoles + vortex,
            bonusNodes = attractedNodes
          )
        }
      }
      ToolType.SOLAR_FLARE_STAFF -> {
        // Expanding solar burst explosion auto-detonating bonus nodes
        soundManager.playSolarSound()
        val wave = SolarShockwave(
          x = x,
          y = y,
          currentRadius = 30f,
          maxRadius = 380f,
          alpha = 1.0f
        )
        _uiState.update { it.copy(solarShockwaves = it.solarShockwaves + wave) }
        detonateNearbyBonusNodes(x, y, 380f)
      }
    }
  }

  private fun generateLightningArc(startX: Float, startY: Float): LightningArc {
    val points = mutableListOf<Pair<Float, Float>>()
    points.add(Pair(startX, startY))
    var curX = startX
    var curY = startY
    val targetAngle = Random.nextFloat() * 2f * Math.PI.toFloat()
    val steps = Random.nextInt(4, 8)
    val stepLen = 50f

    for (i in 0 until steps) {
      val angleOffset = (Random.nextFloat() - 0.5f) * 1.2f
      val segAngle = targetAngle + angleOffset
      curX += cos(segAngle) * stepLen
      curY += sin(segAngle) * stepLen
      points.add(Pair(curX, curY))
    }

    return LightningArc(
      points = points,
      alpha = 1.0f,
      colorHex = if (Random.nextBoolean()) 0xFFFFE600 else 0xFF38BDF8,
      width = Random.nextFloat() * 3f + 3f
    )
  }

  private fun detonateNearbyBonusNodes(centerX: Float, centerY: Float, radius: Float) {
    val state = _uiState.value
    val nodesToHit = state.bonusNodes.filter {
      hypot(it.x - centerX, it.y - centerY) <= radius
    }
    for (node in nodesToHit) {
      collectBonusNode(node, autoDetonated = true)
    }
  }

  // --- Particles & VFX Spawn ---

  private fun spawnClickParticles(
    x: Float,
    y: Float,
    tool: ToolType,
    isCrit: Boolean,
    combo: Int
  ) {
    val count = if (isCrit) 35 else (14 + combo * 2).coerceAtMost(32)
    val newParticles = mutableListOf<ParticleItem>()
    val hasRainbow = (_uiState.value.upgrades[UpgradeType.VFX_RAINBOW_TRAILS.id] ?: 0) > 0

    val rainbowPalette = listOf(
      0xFFFF007F, 0xFFFF6600, 0xFFFFD600, 0xFF00FF66, 0xFF00F0FF, 0xFFA855F7
    )

    for (i in 0 until count) {
      val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
      val speed = Random.nextFloat() * (if (isCrit) 18f else 11f) + 3f
      val vx = cos(angle) * speed
      val vy = sin(angle) * speed

      val color = if (hasRainbow) {
        rainbowPalette[Random.nextInt(rainbowPalette.size)]
      } else if (isCrit) {
        0xFFFFD600
      } else if (Random.nextBoolean()) {
        tool.primaryColorHex
      } else {
        tool.accentColorHex
      }

      val kind = when (tool) {
        ToolType.WOODEN_POINTER -> ParticleKind.SPARK
        ToolType.NEON_CYBER_DAGGER -> ParticleKind.TRAIL
        ToolType.MJOLNIR_HAMMER -> ParticleKind.LIGHTNING
        ToolType.COSMIC_SCYTHE -> ParticleKind.VORTEX
        ToolType.SOLAR_FLARE_STAFF -> ParticleKind.SOLAR
      }

      val life = Random.nextFloat() * 0.4f + 0.35f
      newParticles.add(
        ParticleItem(
          id = nextId++,
          x = x,
          y = y,
          vx = vx,
          vy = vy,
          colorHex = color,
          size = Random.nextFloat() * (if (isCrit) 9f else 6f) + 4f,
          alpha = 1.0f,
          life = life,
          maxLife = life,
          kind = kind
        )
      )
    }

    _uiState.update { it.copy(particles = (it.particles + newParticles).takeLast(160)) }
  }

  private fun spawnFloatingText(
    x: Float,
    y: Float,
    points: Double,
    exp: Double,
    isCrit: Boolean,
    combo: Int
  ) {
    val items = mutableListOf<FloatingTextItem>()
    val formattedPoints = formatNumber(points)

    if (isCrit) {
      items.add(
        FloatingTextItem(
          id = nextId++,
          text = "CRITICAL! +$formattedPoints",
          x = x + (Random.nextFloat() - 0.5f) * 30f,
          y = y - 40f,
          colorHex = 0xFFFFD600,
          scale = 1.4f,
          isCrit = true
        )
      )
    } else {
      items.add(
        FloatingTextItem(
          id = nextId++,
          text = "+$formattedPoints",
          x = x + (Random.nextFloat() - 0.5f) * 40f,
          y = y - 30f,
          colorHex = 0xFF00F0FF,
          scale = 1.0f
        )
      )
    }

    if (combo >= 4 && combo % 2 == 0) {
      items.add(
        FloatingTextItem(
          id = nextId++,
          text = "COMBO x$combo!",
          x = x,
          y = y - 75f,
          colorHex = 0xFFFF007F,
          scale = 1.15f
        )
      )
    }

    _uiState.update { it.copy(floatingTexts = (it.floatingTexts + items).takeLast(25)) }
  }

  // --- Frame Updates for Canvas Physics ---

  private fun updateVfxFrame() {
    _uiState.update { state ->
      // Ease squash & stretch back to 1.0
      val newScaleX = state.targetScaleX + (1.0f - state.targetScaleX) * 0.2f
      val newScaleY = state.targetScaleY + (1.0f - state.targetScaleY) * 0.2f

      // Dampen screen shake
      val (sx, sy) = state.screenShakeOffset
      val newSx = sx * 0.75f
      val newSy = sy * 0.75f
      val finalShake = if (abs(newSx) < 0.2f && abs(newSy) < 0.2f) Pair(0f, 0f) else Pair(newSx, newSy)

      // Update particles
      val updatedParticles = state.particles.mapNotNull { p ->
        val newLife = p.life - 0.033f
        if (newLife <= 0f) null
        else {
          val gravity = if (p.kind == ParticleKind.CONFETTI) 0.6f else 0.15f
          p.copy(
            x = p.x + p.vx,
            y = p.y + p.vy + gravity,
            vx = p.vx * 0.94f,
            vy = p.vy * 0.94f,
            alpha = (newLife / p.maxLife).coerceIn(0f, 1f),
            life = newLife
          )
        }
      }

      // Update floating texts
      val updatedTexts = state.floatingTexts.mapNotNull { t ->
        val newAlpha = t.alpha - 0.035f
        if (newAlpha <= 0f) null
        else {
          t.copy(
            y = t.y - 3.2f,
            alpha = newAlpha
          )
        }
      }

      // Update slice lines
      val updatedSlices = state.sliceLines.mapNotNull { s ->
        val newAlpha = s.alpha - 0.08f
        if (newAlpha <= 0f) null else s.copy(alpha = newAlpha)
      }

      // Update lightning arcs
      val updatedArcs = state.lightningArcs.mapNotNull { a ->
        val newAlpha = a.alpha - 0.12f
        if (newAlpha <= 0f) null else a.copy(alpha = newAlpha)
      }

      // Update black holes
      val updatedVortices = state.blackHoles.mapNotNull { v ->
        val newAlpha = v.alpha - 0.04f
        if (newAlpha <= 0f) null else v.copy(alpha = newAlpha, rotation = v.rotation + 18f)
      }

      // Update solar shockwaves
      val updatedWaves = state.solarShockwaves.mapNotNull { w ->
        val newR = w.currentRadius + 18f
        val newA = w.alpha - 0.05f
        if (newA <= 0f || newR >= w.maxRadius) null
        else w.copy(currentRadius = newR, alpha = newA)
      }

      // Update bonus nodes movement
      val updatedNodes = state.bonusNodes.mapNotNull { n ->
        val newLife = n.lifeTime - 0.033f
        if (newLife <= 0f) null
        else {
          n.copy(
            x = n.x + n.vx,
            y = n.y + n.vy,
            lifeTime = newLife
          )
        }
      }

      state.copy(
        targetScaleX = newScaleX,
        targetScaleY = newScaleY,
        screenShakeOffset = finalShake,
        particles = updatedParticles,
        floatingTexts = updatedTexts,
        sliceLines = updatedSlices,
        lightningArcs = updatedArcs,
        blackHoles = updatedVortices,
        solarShockwaves = updatedWaves,
        bonusNodes = updatedNodes
      )
    }
  }

  // --- Bonus Nodes ---

  private fun spawnBonusNode() {
    val state = _uiState.value
    if (state.bonusNodes.size >= 4) return

    val kind = when (Random.nextInt(3)) {
      0 -> NodeKind.GOLDEN_ORB
      1 -> NodeKind.LUCKY_STAR
      else -> NodeKind.COMET_CORE
    }

    // Spawn from random screen edges
    val startX = Random.nextFloat() * 600f + 100f
    val startY = Random.nextFloat() * 300f + 250f
    val vx = (Random.nextFloat() - 0.5f) * 2.5f
    val vy = (Random.nextFloat() - 0.5f) * 2.5f

    val baseReward = max(50.0, state.pointsPerSecond * 15.0)
    val expReward = max(50.0, state.playerLevel * 25.0)

    val node = BonusNode(
      id = nextId++,
      x = startX,
      y = startY,
      vx = vx,
      vy = vy,
      kind = kind,
      pointsReward = baseReward * (if (kind == NodeKind.COMET_CORE) 3.0 else 1.5),
      expReward = expReward,
      lifeTime = 12f
    )

    _uiState.update { it.copy(bonusNodes = it.bonusNodes + node) }
  }

  fun collectBonusNode(node: BonusNode, autoDetonated: Boolean = false) {
    val state = _uiState.value
    if (!state.bonusNodes.contains(node)) return

    soundManager.playBonusNodeSound()
    soundManager.vibrateCrit()

    val earned = node.pointsReward * (if (autoDetonated) 1.5 else 1.0)
    val earnedExp = node.expReward

    val newPoints = state.points + earned
    val newLifetime = state.lifetimePoints + earned

    // Burst text
    val label = if (autoDetonated) "SOLAR BLAST! +${formatNumber(earned)}" else "${node.kind.label} HIT! +${formatNumber(earned)}"
    val floatText = FloatingTextItem(
      id = nextId++,
      text = label,
      x = node.x,
      y = node.y,
      colorHex = node.kind.colorHex,
      scale = 1.35f,
      isCrit = true
    )

    // Spark particles
    val burstParticles = (0..20).map {
      val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
      val speed = Random.nextFloat() * 12f + 4f
      ParticleItem(
        id = nextId++,
        x = node.x,
        y = node.y,
        vx = cos(angle) * speed,
        vy = sin(angle) * speed,
        colorHex = node.kind.colorHex,
        size = Random.nextFloat() * 8f + 4f,
        alpha = 1f,
        life = 0.6f,
        maxLife = 0.6f,
        kind = ParticleKind.SPARK
      )
    }

    _uiState.update {
      it.copy(
        points = newPoints,
        lifetimePoints = newLifetime,
        currentExp = it.currentExp + earnedExp,
        bonusNodes = it.bonusNodes - node,
        floatingTexts = it.floatingTexts + floatText,
        particles = it.particles + burstParticles
      )
    }
  }

  // --- Level Up Event ---

  private fun triggerLevelUp(newLevel: Int) {
    soundManager.playLevelUpSound()
    soundManager.vibrateLevelUp()

    // Confetti & Fireworks screen burst!
    val confettiList = (0..60).map {
      val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
      val speed = Random.nextFloat() * 22f + 6f
      val colors = listOf(0xFF00F0FF, 0xFFFF007F, 0xFFFFD600, 0xFF00FF66, 0xFFA855F7, 0xFFFFFFFF)
      ParticleItem(
        id = nextId++,
        x = 540f,
        y = 960f,
        vx = cos(angle) * speed,
        vy = sin(angle) * speed - 8f,
        colorHex = colors[Random.nextInt(colors.size)],
        size = Random.nextFloat() * 10f + 6f,
        alpha = 1.0f,
        life = 1.4f,
        maxLife = 1.4f,
        kind = ParticleKind.CONFETTI
      )
    }

    _uiState.update {
      it.copy(
        levelUpEvent = newLevel,
        particles = it.particles + confettiList
      )
    }
  }

  fun dismissLevelUpDialog() {
    _uiState.update { it.copy(levelUpEvent = null) }
  }

  // --- Store & Tool Actions ---

  fun equipTool(tool: ToolType) {
    val state = _uiState.value
    if (state.unlockedTools.contains(tool.id)) {
      _uiState.update { it.copy(equippedTool = tool) }
      soundManager.playClickSound()
      saveCurrentState()
    }
  }

  fun unlockTool(tool: ToolType) {
    val state = _uiState.value
    if (state.points >= tool.unlockCost && state.playerLevel >= tool.unlockLevel) {
      val newPoints = state.points - tool.unlockCost
      val newUnlocked = state.unlockedTools + tool.id
      _uiState.update {
        it.copy(
          points = newPoints,
          unlockedTools = newUnlocked,
          equippedTool = tool
        )
      }
      soundManager.playCritSound()
      soundManager.vibrateCrit()
      saveCurrentState()
    }
  }

  fun buyAutoClicker(type: AutoClickerType) {
    val state = _uiState.value
    val count = state.autoClickers[type.id] ?: 0
    val cost = type.getCost(count)

    if (state.points >= cost) {
      val newCount = count + 1
      val newAutoMap = state.autoClickers.toMutableMap().apply { put(type.id, newCount) }
      val newPoints = state.points - cost
      val newPps = calculatePps(newAutoMap, state.prestigeRebirths, state.prestigeUpgrades)

      _uiState.update {
        it.copy(
          points = newPoints,
          autoClickers = newAutoMap,
          pointsPerSecond = newPps
        )
      }
      soundManager.playClickSound()
      soundManager.vibrateClick()
      saveCurrentState()
    }
  }

  fun buyUpgrade(type: UpgradeType) {
    val state = _uiState.value
    val currentLevel = state.upgrades[type.id] ?: 0
    if (currentLevel >= type.maxLevel) return

    val cost = type.getCost(currentLevel)
    if (state.points >= cost) {
      val newLevel = currentLevel + 1
      val newUpgrades = state.upgrades.toMutableMap().apply { put(type.id, newLevel) }
      val newPoints = state.points - cost

      _uiState.update {
        it.copy(
          points = newPoints,
          upgrades = newUpgrades
        )
      }
      soundManager.playClickSound()
      soundManager.vibrateClick()
      saveCurrentState()
    }
  }

  // --- Prestige (Rebirth) ---

  fun performRebirth() {
    val state = _uiState.value
    if (state.playerLevel < 20) return

    val shardsEarned = calculateRebirthShards(state.playerLevel, state.lifetimePoints)
    soundManager.vibrateRebirth()
    soundManager.playLevelUpSound()

    val newState = repository.resetForRebirth(shardsEarned)
    val maxExp = calculateMaxExpForLevel(newState.playerLevel)
    val pps = calculatePps(newState.autoClickerCounts, newState.prestigeRebirths, newState.prestigeUpgradeLevels)

    _uiState.update {
      it.copy(
        points = newState.points,
        playerLevel = newState.playerLevel,
        currentExp = newState.currentExp,
        maxExp = maxExp,
        pointsPerSecond = pps,
        equippedTool = ToolType.WOODEN_POINTER,
        unlockedTools = newState.unlockedToolIds,
        autoClickers = newState.autoClickerCounts,
        upgrades = newState.upgradeLevels,
        prestigeRebirths = newState.prestigeRebirths,
        cosmicShards = newState.cosmicShards,
        prestigeUpgrades = newState.prestigeUpgradeLevels,
        comboStreak = 1,
        comboProgress = 0f,
        isRebirthAvailable = false,
        shardsOnRebirth = 0
      )
    }
  }

  fun buyPrestigeUpgrade(type: PrestigeUpgradeType) {
    val state = _uiState.value
    val currentLvl = state.prestigeUpgrades[type.id] ?: 0
    if (currentLvl >= type.maxLevel) return

    val cost = type.getCost(currentLvl)
    if (state.cosmicShards >= cost) {
      val newLvl = currentLvl + 1
      val newPrestigeMap = state.prestigeUpgrades.toMutableMap().apply { put(type.id, newLvl) }
      val newShards = state.cosmicShards - cost
      val newPps = calculatePps(state.autoClickers, state.prestigeRebirths, newPrestigeMap)
      val newMaxCombo = 10 + (newPrestigeMap["supernova_combo"] ?: 0)

      _uiState.update {
        it.copy(
          cosmicShards = newShards,
          prestigeUpgrades = newPrestigeMap,
          pointsPerSecond = newPps,
          maxComboMultiplier = newMaxCombo
        )
      }
      soundManager.playCritSound()
      saveCurrentState()
    }
  }

  // --- Settings ---

  fun toggleSound() {
    val toggled = !_uiState.value.soundEnabled
    soundManager.soundEnabled = toggled
    _uiState.update { it.copy(soundEnabled = toggled) }
    saveCurrentState()
  }

  fun toggleHaptics() {
    val toggled = !_uiState.value.hapticsEnabled
    soundManager.hapticsEnabled = toggled
    _uiState.update { it.copy(hapticsEnabled = toggled) }
    saveCurrentState()
  }

  // --- Achievements ---

  private fun checkAchievements(
    totalClicks: Long,
    totalCrits: Long,
    highestCombo: Int,
    level: Int,
    lifetime: Double
  ) {
    val unlocked = _uiState.value.unlockedAchievements.toMutableSet()
    var awardedShards = 0

    if (!unlocked.contains("first_click") && totalClicks >= 1) {
      unlocked.add("first_click")
      awardedShards += 1
    }
    if (!unlocked.contains("combo_5") && highestCombo >= 5) {
      unlocked.add("combo_5")
      awardedShards += 1
    }
    if (!unlocked.contains("combo_10") && highestCombo >= 10) {
      unlocked.add("combo_10")
      awardedShards += 2
    }
    if (!unlocked.contains("clicks_100") && totalClicks >= 100) {
      unlocked.add("clicks_100")
      awardedShards += 1
    }
    if (!unlocked.contains("clicks_1000") && totalClicks >= 1000) {
      unlocked.add("clicks_1000")
      awardedShards += 3
    }
    if (!unlocked.contains("crit_master") && totalCrits >= 50) {
      unlocked.add("crit_master")
      awardedShards += 2
    }
    if (!unlocked.contains("level_10") && level >= 10) {
      unlocked.add("level_10")
      awardedShards += 2
    }
    if (!unlocked.contains("level_25") && level >= 25) {
      unlocked.add("level_25")
      awardedShards += 5
    }
    if (!unlocked.contains("millionaire") && lifetime >= 1000000.0) {
      unlocked.add("millionaire")
      awardedShards += 5
    }

    if (awardedShards > 0) {
      _uiState.update {
        it.copy(
          unlockedAchievements = unlocked,
          cosmicShards = it.cosmicShards + awardedShards
        )
      }
      soundManager.playCritSound()
    }
  }

  fun getAchievementsList(): List<Achievement> {
    val state = _uiState.value
    val un = state.unlockedAchievements
    return listOf(
      Achievement("first_click", "First Sparks", "Click the reactor for the first time", un.contains("first_click"), 1, "${state.totalClicks}/1"),
      Achievement("combo_5", "Rhythm Tapper", "Reach a 5x Combo streak", un.contains("combo_5"), 1, "${min(state.highestCombo, 5)}/5"),
      Achievement("combo_10", "Hyper Flow God", "Max out a 10x Combo streak", un.contains("combo_10"), 2, "${min(state.highestCombo, 10)}/10"),
      Achievement("clicks_100", "Century Clicker", "Perform 100 total clicks", un.contains("clicks_100"), 1, "${min(state.totalClicks, 100)}/100"),
      Achievement("clicks_1000", "Clicking Overdrive", "Perform 1,000 total clicks", un.contains("clicks_1000"), 3, "${min(state.totalClicks, 1000)}/1000"),
      Achievement("crit_master", "Lethal Precision", "Land 50 Critical Hits", un.contains("crit_master"), 2, "${min(state.totalCrits, 50)}/50"),
      Achievement("level_10", "Energy Surge", "Reach Player Level 10", un.contains("level_10"), 2, "${min(state.playerLevel, 10)}/10"),
      Achievement("level_25", "Solar Ascendant", "Reach Player Level 25", un.contains("level_25"), 5, "${min(state.playerLevel, 25)}/25"),
      Achievement("millionaire", "Cosmic Wealth", "Earn 1,000,000 lifetime points", un.contains("millionaire"), 5, "${formatNumber(state.lifetimePoints)}/1M")
    )
  }

  private fun saveCurrentState() {
    val s = _uiState.value
    repository.saveGame(
      SavedGameState(
        points = s.points,
        lifetimePoints = s.lifetimePoints,
        totalClicks = s.totalClicks,
        totalCrits = s.totalCrits,
        highestCombo = s.highestCombo,
        playerLevel = s.playerLevel,
        currentExp = s.currentExp,
        equippedToolId = s.equippedTool.id,
        unlockedToolIds = s.unlockedTools,
        autoClickerCounts = s.autoClickers,
        upgradeLevels = s.upgrades,
        prestigeRebirths = s.prestigeRebirths,
        cosmicShards = s.cosmicShards,
        prestigeUpgradeLevels = s.prestigeUpgrades,
        unlockedAchievements = s.unlockedAchievements,
        soundEnabled = s.soundEnabled,
        hapticsEnabled = s.hapticsEnabled
      )
    )
  }

  override fun onCleared() {
    super.onCleared()
    saveCurrentState()
    soundManager.release()
  }

  companion object {
    fun formatNumber(value: Double): String {
      return when {
        value >= 1_000_000_000_000.0 -> String.format(java.util.Locale.US, "%.2fT", value / 1_000_000_000_000.0)
        value >= 1_000_000_000.0 -> String.format(java.util.Locale.US, "%.2fB", value / 1_000_000_000.0)
        value >= 1_000_000.0 -> String.format(java.util.Locale.US, "%.2fM", value / 1_000_000.0)
        value >= 1_000.0 -> String.format(java.util.Locale.US, "%.1fK", value / 1_000.0)
        else -> String.format(java.util.Locale.US, "%.0f", value)
      }
    }
  }
}
