package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundAndHapticManager(private val context: Context) {

  private var toneGenerator: ToneGenerator? = null
  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  var soundEnabled: Boolean = true
  var hapticsEnabled: Boolean = true

  private val soundScope = CoroutineScope(Dispatchers.Default)

  init {
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
    } catch (_: Exception) {
      toneGenerator = null
    }
  }

  fun playClickSound() {
    if (!soundEnabled) return
    soundScope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
      } catch (_: Exception) {}
    }
  }

  fun playCritSound() {
    if (!soundEnabled) return
    soundScope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
      } catch (_: Exception) {}
    }
  }

  fun playComboSound(comboStreak: Int) {
    if (!soundEnabled) return
    soundScope.launch {
      try {
        val tone = if (comboStreak >= 8) {
          ToneGenerator.TONE_CDMA_PIP
        } else {
          ToneGenerator.TONE_PROP_BEEP2
        }
        toneGenerator?.startTone(tone, 35)
      } catch (_: Exception) {}
    }
  }

  fun playLevelUpSound() {
    if (!soundEnabled) return
    soundScope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 220)
      } catch (_: Exception) {}
    }
  }

  fun playMjolnirSound() {
    if (!soundEnabled) return
    soundScope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 80)
      } catch (_: Exception) {}
    }
  }

  fun playCosmicSound() {
    if (!soundEnabled) return
    soundScope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 90)
      } catch (_: Exception) {}
    }
  }

  fun playSolarSound() {
    if (!soundEnabled) return
    soundScope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 110)
      } catch (_: Exception) {}
    }
  }

  fun playBonusNodeSound() {
    if (!soundEnabled) return
    soundScope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
      } catch (_: Exception) {}
    }
  }

  // --- Haptics ---

  fun vibrateClick() {
    if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(12)
      }
    } catch (_: Exception) {}
  }

  fun vibrateCrit() {
    if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(55, 255))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(55)
      }
    } catch (_: Exception) {}
  }

  fun vibrateMjolnir() {
    if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 30, 20, 60)
        val amplitudes = intArrayOf(0, 180, 0, 255)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(80)
      }
    } catch (_: Exception) {}
  }

  fun vibrateLevelUp() {
    if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 40, 30, 70, 30, 120)
        val amplitudes = intArrayOf(0, 160, 0, 220, 0, 255)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(180)
      }
    } catch (_: Exception) {}
  }

  fun vibrateRebirth() {
    if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 50, 40, 80, 40, 140, 40, 200)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(250)
      }
    } catch (_: Exception) {}
  }

  fun release() {
    toneGenerator?.release()
    toneGenerator = null
  }
}
