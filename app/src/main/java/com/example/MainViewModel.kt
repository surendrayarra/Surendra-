package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.BluetoothService
import com.example.bluetooth.ConnectionState
import com.example.bluetooth.ConsoleLog
import com.example.bluetooth.DiscoveredDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  val bluetoothService = BluetoothService(application.applicationContext)

  val connectionState: StateFlow<ConnectionState> = bluetoothService.connectionState
  val discoveredDevices: StateFlow<List<DiscoveredDevice>> = bluetoothService.discoveredDevices
  val logs: StateFlow<List<ConsoleLog>> = bluetoothService.logs

  // UI States
  private val _joy1Pos = MutableStateFlow(Pair(0, 0))
  val joy1Pos: StateFlow<Pair<Int, Int>> = _joy1Pos.asStateFlow()

  private val _joy2Pos = MutableStateFlow(Pair(0, 0))
  val joy2Pos: StateFlow<Pair<Int, Int>> = _joy2Pos.asStateFlow()

  private val _isLightOn = MutableStateFlow(false)
  val isLightOn: StateFlow<Boolean> = _isLightOn.asStateFlow()

  private val _isRunning = MutableStateFlow(false)
  val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

  private val _showConnectDialog = MutableStateFlow(false)
  val showConnectDialog: StateFlow<Boolean> = _showConnectDialog.asStateFlow()

  private val _showTerminalDialog = MutableStateFlow(false)
  val showTerminalDialog: StateFlow<Boolean> = _showTerminalDialog.asStateFlow()

  // Throttling for joysticks
  private var lastJoy1SendTime = 0L
  private var lastJoy2SendTime = 0L
  private var lastJoy1SentX = 0
  private var lastJoy1SentY = 0
  private var lastJoy2SentX = 0
  private var lastJoy2SentY = 0

  fun onJoy1Move(x: Int, y: Int, isRelease: Boolean = false) {
    _joy1Pos.value = Pair(x, y)
    val now = System.currentTimeMillis()
    if (isRelease) {
      lastJoy1SentX = 0
      lastJoy1SentY = 0
      bluetoothService.sendCommand("JOY1:0,0\n")
      return
    }

    if (now - lastJoy1SendTime > 50 || (Math.abs(x - lastJoy1SentX) > 5 || Math.abs(y - lastJoy1SentY) > 5)) {
      lastJoy1SendTime = now
      lastJoy1SentX = x
      lastJoy1SentY = y
      bluetoothService.sendCommand("JOY1:$x,$y\n")
    }
  }

  fun onJoy2Move(x: Int, y: Int, isRelease: Boolean = false) {
    _joy2Pos.value = Pair(x, y)
    val now = System.currentTimeMillis()
    if (isRelease) {
      lastJoy2SentX = 0
      lastJoy2SentY = 0
      bluetoothService.sendCommand("JOY2:0,0\n")
      return
    }

    if (now - lastJoy2SendTime > 50 || (Math.abs(x - lastJoy2SentX) > 5 || Math.abs(y - lastJoy2SentY) > 5)) {
      lastJoy2SendTime = now
      lastJoy2SentX = x
      lastJoy2SentY = y
      bluetoothService.sendCommand("JOY2:$x,$y\n")
    }
  }

  fun sendLightOn() {
    _isLightOn.value = true
    bluetoothService.sendCommand("LIGHT_ON\n")
  }

  fun sendLightOff() {
    _isLightOn.value = false
    bluetoothService.sendCommand("LIGHT_OFF\n")
  }

  fun sendStart() {
    _isRunning.value = true
    bluetoothService.sendCommand("START\n")
  }

  fun sendStop() {
    _isRunning.value = false
    bluetoothService.sendCommand("STOP\n")
  }

  fun openConnectDialog() {
    bluetoothService.loadPairedDevices()
    _showConnectDialog.value = true
  }

  fun closeConnectDialog() {
    _showConnectDialog.value = false
  }

  fun openTerminalDialog() {
    _showTerminalDialog.value = true
  }

  fun closeTerminalDialog() {
    _showTerminalDialog.value = false
  }

  fun startDiscovery() {
    bluetoothService.startDiscovery()
  }

  fun connectDevice(device: DiscoveredDevice) {
    bluetoothService.connect(device)
    _showConnectDialog.value = false
  }

  fun connectVirtualESP32() {
    bluetoothService.connectSimulated("ESP32-Butterfly-Robot")
    _showConnectDialog.value = false
  }

  fun disconnect() {
    bluetoothService.disconnect()
  }

  fun clearLogs() {
    bluetoothService.clearLogs()
  }

  override fun onCleared() {
    super.onCleared()
    bluetoothService.onDestroy()
  }
}
