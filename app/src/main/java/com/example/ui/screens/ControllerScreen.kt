package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.MainViewModel
import com.example.ui.components.BluetoothDeviceDialog
import com.example.ui.components.ButterflyChassis
import com.example.ui.components.ConsoleTerminalDialog
import com.example.ui.components.ControllerHeaderBar
import com.example.ui.components.JoystickView
import com.example.ui.components.LightActionButton
import com.example.ui.components.MainControlButton
import com.example.ui.theme.DarkNavyBg

@Composable
fun ControllerScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val connectionState by viewModel.connectionState.collectAsState()
  val discoveredDevices by viewModel.discoveredDevices.collectAsState()
  val logs by viewModel.logs.collectAsState()

  val joy1Pos by viewModel.joy1Pos.collectAsState()
  val joy2Pos by viewModel.joy2Pos.collectAsState()
  val isLightOn by viewModel.isLightOn.collectAsState()
  val isRunning by viewModel.isRunning.collectAsState()

  val showConnectDialog by viewModel.showConnectDialog.collectAsState()
  val showTerminalDialog by viewModel.showTerminalDialog.collectAsState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkNavyBg)
  ) {
    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // Top Header Bar
      ControllerHeaderBar(
        title = "Surendra Bluetooth Connector",
        connectionState = connectionState,
        joy1Pos = joy1Pos,
        joy2Pos = joy2Pos,
        onConnectClick = { viewModel.openConnectDialog() },
        onTerminalClick = { viewModel.openTerminalDialog() }
      )

      // Main Controller Area
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        BoxWithConstraints(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          val isLandscape = maxWidth > maxHeight

          if (isLandscape) {
            // Landscape layout: precisely matches the provided photo!
            val chassisHeight = min(maxHeight - 20.dp, (maxWidth / 2.3f))
            val chassisWidth = min(maxWidth - 24.dp, chassisHeight * 2.35f)
            val joystickSize = min(chassisHeight * 0.72f, 220.dp)

            ButterflyChassis(
              modifier = Modifier
                .width(chassisWidth)
                .height(chassisHeight)
            ) {
              // Left Joystick (centered inside left wing)
              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .width(chassisHeight)
                  .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
              ) {
                JoystickView(
                  size = joystickSize,
                  testTag = "joystick_left",
                  onMove = { x, y, isRelease ->
                    viewModel.onJoy1Move(x, y, isRelease)
                  }
                )
              }

              // Right Joystick (centered inside right wing)
              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .width(chassisHeight)
                  .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
              ) {
                JoystickView(
                  size = joystickSize,
                  testTag = "joystick_right",
                  onMove = { x, y, isRelease ->
                    viewModel.onJoy2Move(x, y, isRelease)
                  }
                )
              }

              // Center Control Cluster
              Column(
                modifier = Modifier
                  .align(Alignment.Center)
                  .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                // Action Buttons: LIGHT ON & LIGHT OFF
                Row(
                  horizontalArrangement = Arrangement.spacedBy(28.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  LightActionButton(
                    label = "LIGHT\nON",
                    isActive = isLightOn,
                    testTag = "btn_light_on",
                    onClick = { viewModel.sendLightOn() }
                  )

                  LightActionButton(
                    label = "LIGHT\nOFF",
                    isActive = !isLightOn,
                    testTag = "btn_light_off",
                    onClick = { viewModel.sendLightOff() }
                  )
                }

                // Main Control Buttons: START & STOP stacked
                Column(
                  verticalArrangement = Arrangement.spacedBy(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  MainControlButton(
                    label = "START",
                    isActive = isRunning,
                    testTag = "btn_start",
                    onClick = { viewModel.sendStart() }
                  )

                  MainControlButton(
                    label = "STOP",
                    isActive = !isRunning,
                    testTag = "btn_stop",
                    onClick = { viewModel.sendStop() }
                  )
                }
              }
            }
          } else {
            // Portrait Layout: Adapts cleanly so user can still play or test comfortably
            val chassisWidth = min(maxWidth - 16.dp, 500.dp)
            val chassisHeight = chassisWidth / 1.75f
            val joystickSize = min(chassisHeight * 0.68f, 150.dp)

            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              ButterflyChassis(
                modifier = Modifier
                  .width(chassisWidth)
                  .height(chassisHeight)
              ) {
                // Left Joystick
                Box(
                  modifier = Modifier
                    .fillMaxHeight()
                    .width(chassisHeight)
                    .align(Alignment.CenterStart),
                  contentAlignment = Alignment.Center
                ) {
                  JoystickView(
                    size = joystickSize,
                    testTag = "joystick_left",
                    onMove = { x, y, isRelease ->
                      viewModel.onJoy1Move(x, y, isRelease)
                    }
                  )
                }

                // Right Joystick
                Box(
                  modifier = Modifier
                    .fillMaxHeight()
                    .width(chassisHeight)
                    .align(Alignment.CenterEnd),
                  contentAlignment = Alignment.Center
                ) {
                  JoystickView(
                    size = joystickSize,
                    testTag = "joystick_right",
                    onMove = { x, y, isRelease ->
                      viewModel.onJoy2Move(x, y, isRelease)
                    }
                  )
                }

                // Center Cluster
                Column(
                  modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    LightActionButton(
                      label = "LIGHT\nON",
                      isActive = isLightOn,
                      testTag = "btn_light_on",
                      modifier = Modifier.size(width = 72.dp, height = 48.dp),
                      onClick = { viewModel.sendLightOn() }
                    )

                    LightActionButton(
                      label = "LIGHT\nOFF",
                      isActive = !isLightOn,
                      testTag = "btn_light_off",
                      modifier = Modifier.size(width = 72.dp, height = 48.dp),
                      onClick = { viewModel.sendLightOff() }
                    )
                  }

                  Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    MainControlButton(
                      label = "START",
                      isActive = isRunning,
                      testTag = "btn_start",
                      modifier = Modifier.size(width = 110.dp, height = 46.dp),
                      onClick = { viewModel.sendStart() }
                    )

                    MainControlButton(
                      label = "STOP",
                      isActive = !isRunning,
                      testTag = "btn_stop",
                      modifier = Modifier.size(width = 110.dp, height = 46.dp),
                      onClick = { viewModel.sendStop() }
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Dialogs
    if (showConnectDialog) {
      BluetoothDeviceDialog(
        connectionState = connectionState,
        discoveredDevices = discoveredDevices,
        onStartScan = { viewModel.startDiscovery() },
        onConnectDevice = { device -> viewModel.connectDevice(device) },
        onConnectVirtual = { viewModel.connectVirtualESP32() },
        onDisconnect = { viewModel.disconnect() },
        onDismiss = { viewModel.closeConnectDialog() }
      )
    }

    if (showTerminalDialog) {
      ConsoleTerminalDialog(
        logs = logs,
        onSendCommand = { cmd -> viewModel.bluetoothService.sendCommand(cmd) },
        onClearLogs = { viewModel.clearLogs() },
        onDismiss = { viewModel.closeTerminalDialog() }
      )
    }
  }
}
