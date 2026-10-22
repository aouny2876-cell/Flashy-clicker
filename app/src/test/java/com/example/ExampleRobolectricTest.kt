package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ClickerRepository
import com.example.model.ToolType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Flashy Clicker", appName)
  }

  @Test
  fun `repository loads and saves game state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = ClickerRepository(context)
    val initialState = repository.loadGame()
    assertNotNull(initialState)
    assertEquals(ToolType.WOODEN_POINTER.id, initialState.equippedToolId)

    val updatedState = initialState.copy(points = 1000.0, playerLevel = 5)
    repository.saveGame(updatedState)

    val reloadedState = repository.loadGame()
    assertEquals(1000.0, reloadedState.points, 0.001)
    assertEquals(5, reloadedState.playerLevel)
  }
}
