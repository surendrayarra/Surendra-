package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches app name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Surendra Butterfly Connector", appName)

    val headerTitle = context.getString(R.string.header_title)
    assertEquals("Surendra Bluetooth Connector", headerTitle)
  }

  @Test
  fun `test viewModel commands and state`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(app)

    assertNotNull(viewModel.bluetoothService)
    assertEquals(Pair(0, 0), viewModel.joy1Pos.value)
    assertEquals(Pair(0, 0), viewModel.joy2Pos.value)

    // Test joystick coordinate updates
    viewModel.onJoy1Move(50, -75, isRelease = false)
    assertEquals(Pair(50, -75), viewModel.joy1Pos.value)

    viewModel.onJoy1Move(0, 0, isRelease = true)
    assertEquals(Pair(0, 0), viewModel.joy1Pos.value)

    // Test start and stop
    viewModel.sendStart()
    assertTrue(viewModel.isRunning.value)

    viewModel.sendStop()
    assertEquals(false, viewModel.isRunning.value)

    // Test light on and off
    viewModel.sendLightOn()
    assertTrue(viewModel.isLightOn.value)

    viewModel.sendLightOff()
    assertEquals(false, viewModel.isLightOn.value)
  }
}
