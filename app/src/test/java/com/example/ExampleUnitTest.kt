package com.example

import com.example.model.AutoClickerType
import com.example.model.ToolType
import com.example.model.UpgradeType
import com.example.viewmodel.ClickerViewModel
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testToolProperties() {
    val pointer = ToolType.WOODEN_POINTER
    assertEquals(1.0, pointer.multiplier, 0.001)
    assertEquals(0.0, pointer.critBonus, 0.001)

    val dagger = ToolType.NEON_CYBER_DAGGER
    assertEquals(2.5, dagger.multiplier, 0.001)
    assertEquals(0.05, dagger.critBonus, 0.001)

    val mjolnir = ToolType.MJOLNIR_HAMMER
    assertEquals(6.0, mjolnir.multiplier, 0.001)
    assertEquals(0.10, mjolnir.critBonus, 0.001)

    val scythe = ToolType.COSMIC_SCYTHE
    assertEquals(15.0, scythe.multiplier, 0.001)
    assertEquals(0.15, scythe.critBonus, 0.001)

    val solar = ToolType.SOLAR_FLARE_STAFF
    assertEquals(40.0, solar.multiplier, 0.001)
    assertEquals(0.25, solar.critBonus, 0.001)
  }

  @Test
  fun testAutoClickerScaling() {
    val bot = AutoClickerType.CLICK_BOT
    assertEquals(15.0, bot.getCost(0), 0.01)
    assertTrue(bot.getCost(1) > bot.getCost(0))
    assertTrue(bot.getCost(5) > bot.getCost(1))
  }

  @Test
  fun testUpgradeScaling() {
    val powerUpg = UpgradeType.BASE_CLICK_POWER
    assertEquals(25.0, powerUpg.getCost(0), 0.01)
    assertTrue(powerUpg.getCost(1) > powerUpg.getCost(0))
  }

  @Test
  fun testFormatNumber() {
    assertEquals("500", ClickerViewModel.formatNumber(500.0))
    assertEquals("1.5K", ClickerViewModel.formatNumber(1500.0))
    assertEquals("2.50M", ClickerViewModel.formatNumber(2500000.0))
    assertEquals("4.00B", ClickerViewModel.formatNumber(4000000000.0))
  }

  @Test
  fun testToolLookup() {
    assertEquals(ToolType.MJOLNIR_HAMMER, ToolType.fromId("mjolnir_hammer"))
    assertEquals(ToolType.WOODEN_POINTER, ToolType.fromId("unknown_id"))
  }
}
