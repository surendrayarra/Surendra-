package com.example.bluetooth

enum class BluetoothType {
  CLASSIC,
  BLE,
  SIMULATED
}

data class DiscoveredDevice(
  val name: String,
  val address: String,
  val type: BluetoothType = BluetoothType.CLASSIC,
  val isBonded: Boolean = false,
  val rssi: Int? = null
)

sealed interface ConnectionState {
  data object Disconnected : ConnectionState
  data object Scanning : ConnectionState
  data class Connecting(val deviceName: String) : ConnectionState
  data class Connected(
    val deviceName: String,
    val address: String,
    val isSimulated: Boolean = false
  ) : ConnectionState
  data class Error(val message: String) : ConnectionState
}

enum class LogDirection {
  TX, RX, SYSTEM
}

data class ConsoleLog(
  val timestamp: Long = System.currentTimeMillis(),
  val direction: LogDirection,
  val text: String
)
