package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.ConnectionState
import com.example.ui.theme.BluetoothBlue
import com.example.ui.theme.BluetoothBlueLight
import com.example.ui.theme.ButtonOrange
import com.example.ui.theme.ControllerNavy
import com.example.ui.theme.ControllerNavyBorder
import com.example.ui.theme.ControllerNavyDark
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

@Composable
fun ControllerHeaderBar(
  title: String = "Surendra Bluetooth Connector",
  connectionState: ConnectionState,
  joy1Pos: Pair<Int, Int>,
  joy2Pos: Pair<Int, Int>,
  onConnectClick: () -> Unit,
  onTerminalClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(DarkNavyBg)
      .border(
        width = 1.dp,
        color = ControllerNavyBorder,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
      )
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: App Title and Sub-status
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = null,
            tint = BluetoothBlueLight,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }

        // Real-time telemetry coordinates: JOY1 and JOY2
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(
                when (connectionState) {
                  is ConnectionState.Connected -> StatusGreen
                  is ConnectionState.Connecting -> StatusYellow
                  is ConnectionState.Scanning -> BluetoothBlueLight
                  else -> StatusRed
                }
              )
          )
          Spacer(modifier = Modifier.width(6.dp))
          val statusText = when (connectionState) {
            is ConnectionState.Connected -> "Connected: ${connectionState.deviceName}"
            is ConnectionState.Connecting -> "Connecting..."
            is ConnectionState.Scanning -> "Scanning..."
            is ConnectionState.Error -> "Error: ${connectionState.message}"
            ConnectionState.Disconnected -> "Disconnected"
          }
          Text(
            text = statusText,
            fontSize = 11.sp,
            color = when (connectionState) {
              is ConnectionState.Connected -> StatusGreen
              is ConnectionState.Connecting -> StatusYellow
              else -> Color(0xFF94A3B8)
            }
          )

          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "J1:[${joy1Pos.first},${joy1Pos.second}]  J2:[${joy2Pos.first},${joy2Pos.second}]",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ButtonOrange
          )
        }
      }

      // Right: Action Buttons (Connect Bluetooth & Terminal)
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onTerminalClick,
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ControllerNavy)
            .border(1.dp, ControllerNavyBorder, RoundedCornerShape(10.dp))
            .testTag("btn_header_terminal")
        ) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = "Serial Monitor",
            tint = ButtonOrange,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        ElevatedButton(
          onClick = onConnectClick,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (connectionState is ConnectionState.Connected) Color(0xFF065F46) else BluetoothBlue,
            contentColor = Color.White
          ),
          modifier = Modifier.testTag("btn_connect_bluetooth")
        ) {
          Icon(
            imageVector = if (connectionState is ConnectionState.Connected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (connectionState is ConnectionState.Connected) "Connected" else "Connect Bluetooth",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}
