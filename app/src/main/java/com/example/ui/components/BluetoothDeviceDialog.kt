package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bluetooth.BluetoothType
import com.example.bluetooth.ConnectionState
import com.example.bluetooth.DiscoveredDevice
import com.example.ui.theme.BluetoothBlue
import com.example.ui.theme.BluetoothBlueLight
import com.example.ui.theme.ButtonOrange
import com.example.ui.theme.ControllerNavy
import com.example.ui.theme.ControllerNavyBorder
import com.example.ui.theme.ControllerNavyDark
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun BluetoothDeviceDialog(
  connectionState: ConnectionState,
  discoveredDevices: List<DiscoveredDevice>,
  onStartScan: () -> Unit,
  onConnectDevice: (DiscoveredDevice) -> Unit,
  onConnectVirtual: () -> Unit,
  onDisconnect: () -> Unit,
  onDismiss: () -> Unit
) {
  val isScanning = connectionState is ConnectionState.Scanning

  // Permission launcher for Android 12+ and legacy Android
  val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
      Manifest.permission.BLUETOOTH_SCAN,
      Manifest.permission.BLUETOOTH_CONNECT
    )
  } else {
    arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.BLUETOOTH,
      Manifest.permission.BLUETOOTH_ADMIN
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val allGranted = permissions.values.all { it }
    if (allGranted) {
      onStartScan()
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(16.dp)
        .testTag("bluetooth_device_dialog"),
      shape = RoundedCornerShape(20.dp),
      color = ControllerNavyDark,
      tonalElevation = 6.dp,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, ControllerNavyBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Bluetooth,
              contentDescription = "Bluetooth",
              tint = BluetoothBlueLight,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "ESP32 Connection",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF94A3B8)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Active Connection Status Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ControllerNavy)
            .border(1.dp, ControllerNavyBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Status",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
              )
              Spacer(modifier = Modifier.height(2.dp))
              when (connectionState) {
                is ConnectionState.Connected -> {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(StatusGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Connected: ${connectionState.deviceName}",
                      fontSize = 14.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = StatusGreen
                    )
                  }
                  Text(
                    text = connectionState.address,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                  )
                }
                is ConnectionState.Connecting -> {
                  Text(
                    text = "Connecting to ${connectionState.deviceName}...",
                    fontSize = 14.sp,
                    color = BluetoothBlueLight
                  )
                }
                is ConnectionState.Scanning -> {
                  Text(
                    text = "Scanning nearby devices...",
                    fontSize = 14.sp,
                    color = BluetoothBlueLight
                  )
                }
                is ConnectionState.Error -> {
                  Text(
                    text = connectionState.message,
                    fontSize = 13.sp,
                    color = StatusRed
                  )
                }
                ConnectionState.Disconnected -> {
                  Text(
                    text = "Not connected to any device",
                    fontSize = 14.sp,
                    color = Color(0xFFCBD5E1)
                  )
                }
              }
            }

            if (connectionState is ConnectionState.Connected) {
              OutlinedButton(
                onClick = onDisconnect,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                modifier = Modifier.testTag("btn_disconnect")
              ) {
                Text("Disconnect", fontSize = 12.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row: Scan & Simulator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = {
              permissionLauncher.launch(permissionsToRequest)
            },
            modifier = Modifier
              .weight(1f)
              .testTag("btn_scan_devices"),
            colors = ButtonDefaults.buttonColors(
              containerColor = BluetoothBlue,
              contentColor = Color.White
            )
          ) {
            if (isScanning) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Scanning...")
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Scan Devices")
            }
          }

          OutlinedButton(
            onClick = onConnectVirtual,
            modifier = Modifier.testTag("btn_virtual_esp32"),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = ButtonOrange
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, ButtonOrange)
          ) {
            Icon(
              imageVector = Icons.Default.DeveloperBoard,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simulate ESP32")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Device List Section
        Text(
          text = "Devices (${discoveredDevices.size})",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (discoveredDevices.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(DarkNavyBg),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.BluetoothSearching,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No devices found yet",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
              )
              Text(
                text = "Tap 'Scan Devices' or use 'Simulate ESP32'",
                color = Color(0xFF64748B),
                fontSize = 11.sp
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 240.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(DarkNavyBg)
          ) {
            items(discoveredDevices) { device ->
              DeviceListItem(
                device = device,
                onClick = { onConnectDevice(device) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DeviceListItem(
  device: DiscoveredDevice,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag("device_item_${device.address}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(ControllerNavy),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Bluetooth,
          contentDescription = null,
          tint = if (device.isBonded) BluetoothBlueLight else Color(0xFF94A3B8),
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = device.name,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
          )
          if (device.isBonded) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E3A8A))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = "PAIRED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = BluetoothBlueLight
              )
            }
          }
        }
        Text(
          text = "${device.address} • ${device.type.name}",
          color = Color(0xFF94A3B8),
          fontSize = 11.sp
        )
      }
    }

    TextButton(onClick = onClick) {
      Text("Connect", color = ButtonOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
  }
}
