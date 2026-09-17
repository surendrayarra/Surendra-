package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.ControllerScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun controller_screen_components_displayed() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(app)

    composeTestRule.setContent {
      MyApplicationTheme {
        ControllerScreen(viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithTag("joystick_left").assertIsDisplayed()
    composeTestRule.onNodeWithTag("joystick_right").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_light_on").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_light_off").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_start").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_stop").assertIsDisplayed()
  }
}
