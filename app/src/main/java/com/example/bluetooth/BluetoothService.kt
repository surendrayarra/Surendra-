package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothService(private val context: Context) {

  companion object {
    private const val TAG = "BluetoothService"

    // Standard SPP UUID for Bluetooth Classic RFCOMM (used by ESP32 BluetoothSerial)
    val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Nordic UART Service UUIDs commonly used on ESP32 BLE
    val NUS_SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    val NUS_RX_CHAR_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    val NUS_TX_CHAR_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
  }

  private val bluetoothManager: BluetoothManager? =
    context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
  private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

  private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

  private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
  val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

  private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
  val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

  private val _logs = MutableStateFlow<List<ConsoleLog>>(emptyList())
  val logs: StateFlow<List<ConsoleLog>> = _logs.asStateFlow()

  private var activeSocket: BluetoothSocket? = null
  private var outputStream: OutputStream? = null
  private var inputStream: InputStream? = null
  private var readJob: Job? = null

  private var activeGatt: BluetoothGatt? = null
  private var rxCharacteristic: BluetoothGattCharacteristic? = null

  private var isSimulated = false
  private var receiverRegistered = false

  private val classicReceiver = object : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
      when (intent?.action) {
        BluetoothDevice.ACTION_FOUND -> {
          val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
          } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
          }
          val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
          device?.let { dev ->
            val devName = try { dev.name ?: "Unknown Device" } catch (e: SecurityException) { "Unknown Device" }
            val address = dev.address ?: return
            addDiscoveredDevice(
              DiscoveredDevice(
                name = devName,
                address = address,
                type = BluetoothType.CLASSIC,
                isBonded = dev.bondState == BluetoothDevice.BOND_BONDED,
                rssi = if (rssi != Short.MIN_VALUE.toInt()) rssi else null
              )
            )
          }
        }
        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
          if (_connectionState.value is ConnectionState.Scanning) {
            _connectionState.value = ConnectionState.Disconnected
            addLog(LogDirection.SYSTEM, "Discovery finished.")
          }
        }
      }
    }
  }

  private val bleScanCallback = object : ScanCallback() {
    @SuppressLint("MissingPermission")
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
      result?.device?.let { dev ->
        val devName = try { dev.name ?: result.scanRecord?.deviceName ?: "BLE Device" } catch (e: SecurityException) { "BLE Device" }
        val address = dev.address ?: return
        addDiscoveredDevice(
          DiscoveredDevice(
            name = devName,
            address = address,
            type = BluetoothType.BLE,
            isBonded = dev.bondState == BluetoothDevice.BOND_BONDED,
            rssi = result.rssi
          )
        )
      }
    }

    override fun onScanFailed(errorCode: Int) {
      Log.e(TAG, "BLE Scan failed: $errorCode")
      addLog(LogDirection.SYSTEM, "BLE scan error: $errorCode")
    }
  }

  init {
    loadPairedDevices()
  }

  fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

  fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

  @SuppressLint("MissingPermission")
  fun loadPairedDevices() {
    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
    try {
      val bonded = bluetoothAdapter.bondedDevices.orEmpty()
      val pairedList = bonded.map { dev ->
        DiscoveredDevice(
          name = dev.name ?: "Paired ESP32",
          address = dev.address,
          type = BluetoothType.CLASSIC,
          isBonded = true
        )
      }
      _discoveredDevices.value = pairedList
      addLog(LogDirection.SYSTEM, "Loaded ${pairedList.size} paired devices")
    } catch (e: SecurityException) {
      Log.w(TAG, "SecurityException loading paired devices", e)
      addLog(LogDirection.SYSTEM, "Bluetooth permission needed to read paired devices")
    }
  }

  @SuppressLint("MissingPermission")
  fun startDiscovery() {
    if (bluetoothAdapter == null) {
      addLog(LogDirection.SYSTEM, "Bluetooth is not supported on this device")
      return
    }

    _connectionState.value = ConnectionState.Scanning
    loadPairedDevices()

    try {
      if (!receiverRegistered) {
        val filter = IntentFilter().apply {
          addAction(BluetoothDevice.ACTION_FOUND)
          addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(classicReceiver, filter)
        receiverRegistered = true
      }

      if (bluetoothAdapter.isDiscovering) {
        bluetoothAdapter.cancelDiscovery()
      }
      bluetoothAdapter.startDiscovery()
      addLog(LogDirection.SYSTEM, "Scanning for Bluetooth & ESP32 devices...")

      // Also start BLE scan if available
      bluetoothAdapter.bluetoothLeScanner?.startScan(bleScanCallback)

      // Auto stop scan after 12 seconds
      serviceScope.launch {
        delay(12000)
        stopDiscovery()
      }
    } catch (e: SecurityException) {
      _connectionState.value = ConnectionState.Error("Bluetooth permission not granted")
      addLog(LogDirection.SYSTEM, "Error: Missing Bluetooth scan permission")
    } catch (e: Exception) {
      _connectionState.value = ConnectionState.Error(e.message ?: "Failed to start discovery")
      addLog(LogDirection.SYSTEM, "Failed to start scan: ${e.message}")
    }
  }

  @SuppressLint("MissingPermission")
  fun stopDiscovery() {
    try {
      if (bluetoothAdapter?.isDiscovering == true) {
        bluetoothAdapter.cancelDiscovery()
      }
      bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleScanCallback)
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping discovery", e)
    }
    if (_connectionState.value is ConnectionState.Scanning) {
      _connectionState.value = ConnectionState.Disconnected
    }
  }

  @SuppressLint("MissingPermission")
  fun connect(device: DiscoveredDevice) {
    disconnect()
    stopDiscovery()

    if (device.type == BluetoothType.SIMULATED) {
      connectSimulated(device.name)
      return
    }

    _connectionState.value = ConnectionState.Connecting(device.name)
    addLog(LogDirection.SYSTEM, "Connecting to ${device.name} (${device.address})...")

    serviceScope.launch {
      if (device.type == BluetoothType.BLE) {
        connectBleDevice(device)
      } else {
        connectClassicDevice(device)
      }
    }
  }

  @SuppressLint("MissingPermission")
  private suspend fun connectClassicDevice(device: DiscoveredDevice) {
    withContext(Dispatchers.IO) {
      try {
        val btDevice = bluetoothAdapter?.getRemoteDevice(device.address)
        if (btDevice == null) {
          _connectionState.value = ConnectionState.Error("Device not found")
          addLog(LogDirection.SYSTEM, "Device not found: ${device.address}")
          return@withContext
        }

        // Cancel discovery before connecting to ensure reliable connection speed
        try { bluetoothAdapter.cancelDiscovery() } catch (_: Exception) {}

        val socket = btDevice.createRfcommSocketToServiceRecord(SPP_UUID)
        socket.connect()

        activeSocket = socket
        outputStream = socket.outputStream
        inputStream = socket.inputStream
        isSimulated = false

        _connectionState.value = ConnectionState.Connected(
          deviceName = device.name,
          address = device.address,
          isSimulated = false
        )
        addLog(LogDirection.SYSTEM, "Connected to ESP32: ${device.name} [Classic BT]")

        startSocketReader(socket)
      } catch (e: Exception) {
        Log.e(TAG, "Classic connection failed", e)
        disconnect()
        _connectionState.value = ConnectionState.Error("Connection failed: ${e.localizedMessage}")
        addLog(LogDirection.SYSTEM, "Failed to connect: ${e.localizedMessage}")
      }
    }
  }

  @SuppressLint("MissingPermission")
  private fun connectBleDevice(device: DiscoveredDevice) {
    try {
      val btDevice = bluetoothAdapter?.getRemoteDevice(device.address) ?: return
      activeGatt = btDevice.connectGatt(context, false, object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
          if (newState == BluetoothProfile.STATE_CONNECTED) {
            addLog(LogDirection.SYSTEM, "BLE Connected, discovering services...")
            gatt?.discoverServices()
          } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            disconnect()
            _connectionState.value = ConnectionState.Disconnected
            addLog(LogDirection.SYSTEM, "BLE Disconnected")
          }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
          if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
            var foundRx = false
            for (service in gatt.services) {
              for (characteristic in service.characteristics) {
                // Find writable characteristic (either NUS RX or writable property)
                val isWritable = (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) ||
                    (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0)
                if (isWritable) {
                  rxCharacteristic = characteristic
                  foundRx = true
                  break
                }
              }
              if (foundRx) break
            }

            _connectionState.value = ConnectionState.Connected(
              deviceName = device.name,
              address = device.address,
              isSimulated = false
            )
            addLog(LogDirection.SYSTEM, "Connected to ESP32: ${device.name} [BLE]")
          }
        }
      })
    } catch (e: Exception) {
      _connectionState.value = ConnectionState.Error("BLE connection failed: ${e.message}")
      addLog(LogDirection.SYSTEM, "BLE error: ${e.message}")
    }
  }

  fun connectSimulated(name: String = "ESP32-Simulator (Virtual)") {
    disconnect()
    isSimulated = true
    _connectionState.value = ConnectionState.Connected(
      deviceName = name,
      address = "VIRTUAL:ESP32:00:1",
      isSimulated = true
    )
    addLog(LogDirection.SYSTEM, "Connected to $name")
    addLog(LogDirection.RX, "ESP32 Ready! Awaiting commands (START, STOP, LIGHT_ON, LIGHT_OFF, JOY1, JOY2)")
  }

  private fun startSocketReader(socket: BluetoothSocket) {
    readJob?.cancel()
    readJob = serviceScope.launch {
      val buffer = ByteArray(1024)
      val inStream = inputStream ?: return@launch
      while (isActive && socket.isConnected) {
        try {
          val bytesRead = inStream.read(buffer)
          if (bytesRead > 0) {
            val receivedText = String(buffer, 0, bytesRead).trim()
            if (receivedText.isNotEmpty()) {
              addLog(LogDirection.RX, receivedText)
            }
          }
        } catch (e: IOException) {
          if (isActive) {
            disconnect()
            _connectionState.value = ConnectionState.Disconnected
            addLog(LogDirection.SYSTEM, "Connection lost.")
          }
          break
        }
      }
    }
  }

  @SuppressLint("MissingPermission")
  fun sendCommand(text: String) {
    val cleanText = if (text.endsWith("\n")) text else "$text\n"
    addLog(LogDirection.TX, cleanText.trimEnd())

    if (isSimulated) {
      handleSimulatedResponse(cleanText.trim())
      return
    }

    serviceScope.launch(Dispatchers.IO) {
      // 1. Try Classic Bluetooth RFCOMM stream
      outputStream?.let { stream ->
        try {
          stream.write(cleanText.toByteArray(Charsets.UTF_8))
          stream.flush()
          return@launch
        } catch (e: IOException) {
          Log.e(TAG, "Error writing to RFCOMM socket", e)
          addLog(LogDirection.SYSTEM, "Send error: ${e.message}")
        }
      }

      // 2. Try BLE write
      activeGatt?.let { gatt ->
        rxCharacteristic?.let { char ->
          try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              gatt.writeCharacteristic(
                char,
                cleanText.toByteArray(Charsets.UTF_8),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
              )
            } else {
              @Suppress("DEPRECATION")
              char.value = cleanText.toByteArray(Charsets.UTF_8)
              @Suppress("DEPRECATION")
              gatt.writeCharacteristic(char)
            }
          } catch (e: Exception) {
            Log.e(TAG, "Error writing to BLE characteristic", e)
            addLog(LogDirection.SYSTEM, "BLE write error: ${e.message}")
          }
        }
      }
    }
  }

  private fun handleSimulatedResponse(cmd: String) {
    serviceScope.launch {
      delay(40)
      when {
        cmd == "START" -> addLog(LogDirection.RX, "ESP32: Motors Enabled [SYSTEM STARTED]")
        cmd == "STOP" -> addLog(LogDirection.RX, "ESP32: Motors Halted [SYSTEM STOPPED]")
        cmd == "LIGHT_ON" -> addLog(LogDirection.RX, "ESP32: Headlights ON (GPIO2 HIGH)")
        cmd == "LIGHT_OFF" -> addLog(LogDirection.RX, "ESP32: Headlights OFF (GPIO2 LOW)")
        cmd.startsWith("JOY1:") -> {
          // Keep log light for joystick
        }
        cmd.startsWith("JOY2:") -> {
          // Keep log light for joystick
        }
        else -> addLog(LogDirection.RX, "ESP32 ACK: $cmd")
      }
    }
  }

  @SuppressLint("MissingPermission")
  fun disconnect() {
    readJob?.cancel()
    readJob = null

    try {
      inputStream?.close()
      outputStream?.close()
      activeSocket?.close()
    } catch (_: Exception) {}

    inputStream = null
    outputStream = null
    activeSocket = null

    try {
      activeGatt?.close()
    } catch (_: Exception) {}
    activeGatt = null
    rxCharacteristic = null

    isSimulated = false
    _connectionState.value = ConnectionState.Disconnected
  }

  fun clearLogs() {
    _logs.value = emptyList()
  }

  private fun addLog(direction: LogDirection, text: String) {
    val newLog = ConsoleLog(direction = direction, text = text)
    val current = _logs.value.takeLast(199)
    _logs.value = current + newLog
  }

  private fun addDiscoveredDevice(device: DiscoveredDevice) {
    val current = _discoveredDevices.value.toMutableList()
    val index = current.indexOfFirst { it.address.equals(device.address, ignoreCase = true) }
    if (index >= 0) {
      current[index] = device
    } else {
      current.add(device)
    }
    _discoveredDevices.value = current
  }

  fun onDestroy() {
    disconnect()
    try {
      if (receiverRegistered) {
        context.unregisterReceiver(classicReceiver)
        receiverRegistered = false
      }
    } catch (_: Exception) {}
  }
}
