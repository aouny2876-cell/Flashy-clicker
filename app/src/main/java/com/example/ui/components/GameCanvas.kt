package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.viewmodel.GameUiState
import kotlin.math.*

@Composable
fun GameCanvas(
  uiState: GameUiState,
  onTargetClick: (Float, Float) -> Unit,
  onBonusNodeClick: (BonusNode) -> Unit,
  modifier: Modifier = Modifier
) {
  // Continuous rotation for reactor core rings
  val infiniteTransition = rememberInfiniteTransition(label = "core_anim")
  val ringRotationOuter by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 8000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ring_outer"
  )
  val ringRotationInner by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 5000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ring_inner"
  )
  val corePulse by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "core_pulse"
  )

  val hasGoldenAura = (uiState.upgrades[UpgradeType.VFX_GOLDEN_AURA.id] ?: 0) > 0
  val hasHyperGlow = (uiState.upgrades[UpgradeType.VFX_HYPER_GLOW.id] ?: 0) > 0

  val (shakeX, shakeY) = uiState.screenShakeOffset

  Box(
    modifier = modifier
      .fillMaxSize()
      .offset { IntOffset(shakeX.roundToInt(), shakeY.roundToInt()) }
      .testTag("game_arena_canvas_box")
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .testTag("interactive_target_canvas")
        .pointerInput(Unit) {
          detectTapGestures { offset ->
            // Check if player clicked a floating bonus node first
            val clickedNode = uiState.bonusNodes.find { node ->
              hypot(node.x - offset.x, node.y - offset.y) <= (node.radius * 2.2f)
            }
            if (clickedNode != null) {
              onBonusNodeClick(clickedNode)
            } else {
              onTargetClick(offset.x, offset.y)
            }
          }
        }
    ) {
      val canvasW = size.width
      val canvasH = size.height
      val centerX = canvasW / 2f
      val centerY = canvasH * 0.46f // slightly elevated for thumb ergonomics

      // 1. Dynamic Synthwave Background based on Combo Streak
      drawSynthwaveBackground(
        comboStreak = uiState.comboStreak,
        maxCombo = uiState.maxComboMultiplier,
        hasHyperGlow = hasHyperGlow
      )

      // 2. Solar Shockwaves
      for (wave in uiState.solarShockwaves) {
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0x00FF4D00),
              Color(0x88FF4D00).copy(alpha = wave.alpha * 0.7f),
              Color(0xFFFFD600).copy(alpha = wave.alpha)
            ),
            center = Offset(wave.x, wave.y),
            radius = wave.currentRadius
          ),
          radius = wave.currentRadius,
          center = Offset(wave.x, wave.y)
        )
        drawCircle(
          color = Color(0xFFFFD600).copy(alpha = wave.alpha),
          radius = wave.currentRadius,
          center = Offset(wave.x, wave.y),
          style = Stroke(width = 5f * wave.alpha)
        )
      }

      // 3. Black Hole Vortices (Cosmic Scythe)
      for (vortex in uiState.blackHoles) {
        drawBlackHoleVortex(vortex)
      }

      // 4. Draw Central Interactive Reactor / Target Core
      drawTargetCore(
        centerX = centerX,
        centerY = centerY,
        scaleX = uiState.targetScaleX,
        scaleY = uiState.targetScaleY,
        pulse = corePulse,
        rotOuter = ringRotationOuter,
        rotInner = ringRotationInner,
        equippedTool = uiState.equippedTool,
        comboStreak = uiState.comboStreak,
        hasGoldenAura = hasGoldenAura,
        hasHyperGlow = hasHyperGlow
      )

      // 5. Electric Lightning Arcs (Mjolnir)
      for (arc in uiState.lightningArcs) {
        if (arc.points.size > 1) {
          val path = Path().apply {
            moveTo(arc.points[0].first, arc.points[0].second)
            for (i in 1 until arc.points.size) {
              lineTo(arc.points[i].first, arc.points[i].second)
            }
          }
          // Glow pass
          drawPath(
            path = path,
            color = Color(arc.colorHex).copy(alpha = arc.alpha * 0.5f),
            style = Stroke(width = arc.width * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
          // Core bright bolt
          drawPath(
            path = path,
            color = Color.White.copy(alpha = arc.alpha),
            style = Stroke(width = arc.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
          )
        }
      }

      // 6. Laser Slice Lines (Neon Cyber-Dagger)
      for (slice in uiState.sliceLines) {
        drawLine(
          color = Color(slice.colorHex).copy(alpha = slice.alpha * 0.6f),
          start = Offset(slice.startX, slice.startY),
          end = Offset(slice.endX, slice.endY),
          strokeWidth = 14f * slice.alpha,
          cap = StrokeCap.Round
        )
        drawLine(
          color = Color.White.copy(alpha = slice.alpha),
          start = Offset(slice.startX, slice.startY),
          end = Offset(slice.endX, slice.endY),
          strokeWidth = 4f * slice.alpha,
          cap = StrokeCap.Round
        )
      }

      // 7. Floating Bonus Nodes
      for (node in uiState.bonusNodes) {
        drawBonusNode(node)
      }

      // 8. Particles
      for (p in uiState.particles) {
        val color = Color(p.colorHex).copy(alpha = p.alpha)
        when (p.kind) {
          ParticleKind.CONFETTI -> {
            drawRect(
              color = color,
              topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 2f),
              size = Size(p.size, p.size * 1.5f)
            )
          }
          ParticleKind.SPARK -> {
            drawCircle(color = color, radius = p.size, center = Offset(p.x, p.y))
          }
          ParticleKind.LIGHTNING -> {
            drawCircle(color = Color.White.copy(alpha = p.alpha), radius = p.size * 0.5f, center = Offset(p.x, p.y))
            drawCircle(color = color, radius = p.size, center = Offset(p.x, p.y))
          }
          ParticleKind.TRAIL -> {
            drawCircle(color = color, radius = p.size, center = Offset(p.x, p.y))
          }
          ParticleKind.SOLAR -> {
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(Color.White, color, Color.Transparent),
                center = Offset(p.x, p.y),
                radius = p.size * 1.5f
              ),
              radius = p.size * 1.5f,
              center = Offset(p.x, p.y)
            )
          }
          ParticleKind.VORTEX -> {
            drawCircle(color = color, radius = p.size, center = Offset(p.x, p.y))
          }
        }
      }

      // 9. Floating Burst Text
      for (textItem in uiState.floatingTexts) {
        drawFloatingText(textItem)
      }
    }
  }
}

// --- Background Renderer ---
private fun DrawScope.drawSynthwaveBackground(
  comboStreak: Int,
  maxCombo: Int,
  hasHyperGlow: Boolean
) {
  val comboRatio = ((comboStreak - 1).toFloat() / (maxCombo - 1).toFloat()).coerceIn(0f, 1f)
  val baseGlowAlpha = if (hasHyperGlow) 0.35f else 0.15f
  val currentGlowAlpha = baseGlowAlpha + (comboRatio * 0.45f)

  // Dynamic ambient background glow
  val glowColor = when {
    comboStreak >= 9 -> Color(0xFFFF007F) // Hyper Magenta
    comboStreak >= 6 -> Color(0xFFFFD600) // Electric Gold
    comboStreak >= 3 -> Color(0xFF00F0FF) // Cyber Cyan
    else -> Color(0xFF38BDF8) // Cosmic Blue
  }

  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(
        Color(0xFF07080E),
        Color(0xFF0D0E1A),
        glowColor.copy(alpha = currentGlowAlpha * 0.4f),
        Color(0xFF07080E)
      )
    )
  )

  // Perspective Synthwave Grid at bottom
  val horizonY = size.height * 0.72f
  val gridColor = glowColor.copy(alpha = currentGlowAlpha * 0.5f)

  // Horizontal grid lines
  var curY = horizonY
  var spacing = 14f
  while (curY < size.height) {
    drawLine(
      color = gridColor,
      start = Offset(0f, curY),
      end = Offset(size.width, curY),
      strokeWidth = 1.8f
    )
    spacing *= 1.35f
    curY += spacing
  }

  // Perspective vertical rays
  val vanishingX = size.width / 2f
  val lineCount = 12
  for (i in -lineCount..lineCount) {
    val bottomX = vanishingX + (i * (size.width / (lineCount * 0.9f)))
    drawLine(
      color = gridColor,
      start = Offset(vanishingX, horizonY),
      end = Offset(bottomX, size.height),
      strokeWidth = 1.5f
    )
  }
}

// --- Central Target Core ---
private fun DrawScope.drawTargetCore(
  centerX: Float,
  centerY: Float,
  scaleX: Float,
  scaleY: Float,
  pulse: Float,
  rotOuter: Float,
  rotInner: Float,
  equippedTool: ToolType,
  comboStreak: Int,
  hasGoldenAura: Boolean,
  hasHyperGlow: Boolean
) {
  val baseRadius = size.width * 0.28f
  val primaryColor = Color(equippedTool.primaryColorHex)
  val accentColor = Color(equippedTool.accentColorHex)

  // 1. Golden Aura (Cosmetic VFX)
  if (hasGoldenAura) {
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0x88FFD600),
          Color(0x44FFAA00),
          Color.Transparent
        ),
        center = Offset(centerX, centerY),
        radius = baseRadius * 1.85f * pulse
      ),
      radius = baseRadius * 1.85f * pulse,
      center = Offset(centerX, centerY)
    )
  }

  // 2. Hyper Glow Backdrop (Cosmetic VFX)
  if (hasHyperGlow || comboStreak >= 5) {
    val auraRadius = baseRadius * (1.4f + (comboStreak * 0.04f))
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          primaryColor.copy(alpha = 0.45f),
          accentColor.copy(alpha = 0.2f),
          Color.Transparent
        ),
        center = Offset(centerX, centerY),
        radius = auraRadius
      ),
      radius = auraRadius,
      center = Offset(centerX, centerY)
    )
  }

  // Draw Squash & Stretch transform
  withTransform({
    scale(scaleX * pulse, scaleY * pulse, Offset(centerX, centerY))
  }) {
    // Outer Tech Ring
    withTransform({
      rotate(rotOuter, Offset(centerX, centerY))
    }) {
      drawCircle(
        color = primaryColor.copy(alpha = 0.55f),
        radius = baseRadius * 1.18f,
        center = Offset(centerX, centerY),
        style = Stroke(
          width = 3.5f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 18f, 10f, 18f), 0f)
        )
      )
      // Small tech node studs on outer ring
      for (angleDeg in 0 until 360 step 60) {
        val rad = Math.toRadians(angleDeg.toDouble())
        val nx = centerX + cos(rad).toFloat() * (baseRadius * 1.18f)
        val ny = centerY + sin(rad).toFloat() * (baseRadius * 1.18f)
        drawCircle(color = accentColor, radius = 5f, center = Offset(nx, ny))
      }
    }

    // Inner Counter-Rotating Ring
    withTransform({
      rotate(rotInner, Offset(centerX, centerY))
    }) {
      drawCircle(
        color = accentColor.copy(alpha = 0.7f),
        radius = baseRadius * 0.94f,
        center = Offset(centerX, centerY),
        style = Stroke(
          width = 4.5f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(45f, 25f), 0f)
        )
      )
    }

    // Core Outer Glow Shield
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0xFF1E2244),
          Color(0xFF0F1122),
          primaryColor.copy(alpha = 0.6f)
        ),
        center = Offset(centerX, centerY),
        radius = baseRadius * 0.88f
      ),
      radius = baseRadius * 0.88f,
      center = Offset(centerX, centerY)
    )

    // Core Crystal / Power Reactor Orb
    val crystalRadius = baseRadius * 0.62f
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          Color.White,
          primaryColor,
          accentColor,
          Color(0xFF090A15)
        ),
        center = Offset(centerX - crystalRadius * 0.2f, centerY - crystalRadius * 0.25f),
        radius = crystalRadius
      ),
      radius = crystalRadius,
      center = Offset(centerX, centerY)
    )

    // Reactor Core Bright Specular Highlight
    drawCircle(
      color = Color.White.copy(alpha = 0.85f),
      radius = crystalRadius * 0.26f,
      center = Offset(centerX - crystalRadius * 0.25f, centerY - crystalRadius * 0.3f)
    )

    // Tool Icon / Glyph in Center
    drawToolGlyph(centerX, centerY, crystalRadius, equippedTool)
  }
}

private fun DrawScope.drawToolGlyph(
  cx: Float,
  cy: Float,
  radius: Float,
  tool: ToolType
) {
  val glyphColor = Color.White
  when (tool) {
    ToolType.WOODEN_POINTER -> {
      // Sleek arrowhead pointer
      val path = Path().apply {
        moveTo(cx, cy - radius * 0.45f)
        lineTo(cx + radius * 0.35f, cy + radius * 0.35f)
        lineTo(cx, cy + radius * 0.15f)
        lineTo(cx - radius * 0.35f, cy + radius * 0.35f)
        close()
      }
      drawPath(path, glyphColor)
    }
    ToolType.NEON_CYBER_DAGGER -> {
      // Diamond cyber blade
      val path = Path().apply {
        moveTo(cx, cy - radius * 0.5f)
        lineTo(cx + radius * 0.25f, cy)
        lineTo(cx, cy + radius * 0.5f)
        lineTo(cx - radius * 0.25f, cy)
        close()
      }
      drawPath(path, glyphColor)
      drawLine(Color(0xFF00F0FF), Offset(cx, cy - radius * 0.5f), Offset(cx, cy + radius * 0.5f), 3f)
    }
    ToolType.MJOLNIR_HAMMER -> {
      // Lightning hammer head
      drawRect(
        color = glyphColor,
        topLeft = Offset(cx - radius * 0.35f, cy - radius * 0.4f),
        size = Size(radius * 0.7f, radius * 0.35f)
      )
      drawRect(
        color = Color(0xFFFFE600),
        topLeft = Offset(cx - radius * 0.1f, cy - radius * 0.05f),
        size = Size(radius * 0.2f, radius * 0.5f)
      )
    }
    ToolType.COSMIC_SCYTHE -> {
      // Cosmic crescent curve
      drawArc(
        color = glyphColor,
        startAngle = -60f,
        sweepAngle = 200f,
        useCenter = false,
        topLeft = Offset(cx - radius * 0.4f, cy - radius * 0.45f),
        size = Size(radius * 0.8f, radius * 0.8f),
        style = Stroke(width = 6f, cap = StrokeCap.Round)
      )
      drawLine(Color(0xFFA855F7), Offset(cx, cy - radius * 0.4f), Offset(cx - radius * 0.2f, cy + radius * 0.45f), 4f)
    }
    ToolType.SOLAR_FLARE_STAFF -> {
      // Solar sunburst
      drawCircle(Color(0xFFFFD600), radius * 0.22f, Offset(cx, cy))
      for (deg in 0 until 360 step 45) {
        val rad = Math.toRadians(deg.toDouble())
        val x1 = cx + cos(rad).toFloat() * (radius * 0.28f)
        val y1 = cy + sin(rad).toFloat() * (radius * 0.28f)
        val x2 = cx + cos(rad).toFloat() * (radius * 0.48f)
        val y2 = cy + sin(rad).toFloat() * (radius * 0.48f)
        drawLine(Color(0xFFFF4D00), Offset(x1, y1), Offset(x2, y2), 4f, cap = StrokeCap.Round)
      }
    }
  }
}

// --- Floating Black Hole Vortex ---
private fun DrawScope.drawBlackHoleVortex(vortex: BlackHoleVortex) {
  val center = Offset(vortex.x, vortex.y)
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        Color.Black.copy(alpha = vortex.alpha),
        Color(0xFFA855F7).copy(alpha = vortex.alpha * 0.7f),
        Color.Transparent
      ),
      center = center,
      radius = vortex.radius
    ),
    radius = vortex.radius,
    center = center
  )
  drawCircle(
    color = Color.Black.copy(alpha = vortex.alpha),
    radius = vortex.radius * 0.35f,
    center = center
  )
  // Rotating swirl ring
  withTransform({
    rotate(vortex.rotation, center)
  }) {
    drawCircle(
      color = Color(0xFFEC4899).copy(alpha = vortex.alpha * 0.8f),
      radius = vortex.radius * 0.65f,
      center = center,
      style = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 25f), 0f)
      )
    )
  }
}

// --- Bonus Floating Node ---
private fun DrawScope.drawBonusNode(node: BonusNode) {
  val color = Color(node.kind.colorHex)
  val center = Offset(node.x, node.y)

  // Glowing pulse aura
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(color.copy(alpha = 0.7f), Color.Transparent),
      center = center,
      radius = node.radius * 2.2f
    ),
    radius = node.radius * 2.2f,
    center = center
  )

  // Solid Core
  drawCircle(color = color, radius = node.radius, center = center)
  drawCircle(color = Color.White, radius = node.radius * 0.45f, center = center)
}

// --- Floating Damage/Points Text ---
private fun DrawScope.drawFloatingText(item: FloatingTextItem) {
  val paint = android.graphics.Paint().apply {
    this.color = Color(item.colorHex).copy(alpha = item.alpha).toArgb()
    this.textSize = if (item.isCrit) 56f * item.scale else 42f * item.scale
    this.isFakeBoldText = true
    this.textAlign = android.graphics.Paint.Align.CENTER
    this.setShadowLayer(14f, 0f, 0f, android.graphics.Color.BLACK)
  }

  drawIntoCanvas { canvas ->
    canvas.nativeCanvas.drawText(
      item.text,
      item.x,
      item.y,
      paint
    )
  }
}
