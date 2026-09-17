package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bluetooth.ConsoleLog
import com.example.bluetooth.LogDirection
import com.example.ui.theme.ButtonOrange
import com.example.ui.theme.ControllerNavyBorder
import com.example.ui.theme.ControllerNavyDark
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.StatusGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConsoleTerminalDialog(
  logs: List<ConsoleLog>,
  onSendCommand: (String) -> Unit,
  onClearLogs: () -> Unit,
  onDismiss: () -> Unit
) {
  var customCommand by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }

  LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) {
      listState.animateScrollToItem(logs.size - 1)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .padding(16.dp)
        .testTag("console_terminal_dialog"),
      shape = RoundedCornerShape(20.dp),
      color = ControllerNavyDark,
      tonalElevation = 6.dp,
      border = androidx.compose.foundation.BorderStroke(1.5.dp, ControllerNavyBorder)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Terminal,
              contentDescription = "Terminal",
              tint = ButtonOrange,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Serial Terminal & Logs",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }

          Row {
            IconButton(onClick = onClearLogs) {
              Icon(
                imageVector = Icons.Default.ClearAll,
                contentDescription = "Clear logs",
                tint = Color(0xFF94A3B8)
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Log Console Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkNavyBg)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(8.dp)
        ) {
          if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
              Text(
                text = "No logs yet. Commands will appear here.",
                color = Color(0xFF64748B),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
              )
            }
          } else {
            LazyColumn(
              state = listState,
              modifier = Modifier.fillMaxWidth()
            ) {
              items(logs) { log ->
                val timeStr = timeFormat.format(Date(log.timestamp))
                val dirColor = when (log.direction) {
                  LogDirection.TX -> ButtonOrange
                  LogDirection.RX -> StatusGreen
                  LogDirection.SYSTEM -> Color(0xFF38BDF8)
                }
                val dirPrefix = when (log.direction) {
                  LogDirection.TX -> "[TX >]"
                  LogDirection.RX -> "[RX <]"
                  LogDirection.SYSTEM -> "[SYS]"
                }

                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                  Text(
                    text = "$timeStr ",
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                  )
                  Text(
                    text = "$dirPrefix ",
                    color = dirColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                  Text(
                    text = log.text,
                    color = Color(0xFFF1F5F9),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Send Custom Command input
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customCommand,
            onValueChange = { customCommand = it },
            modifier = Modifier.weight(1f).height(50.dp),
            placeholder = { Text("Enter command (e.g., START)", fontSize = 12.sp, color = Color(0xFF64748B)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
              focusedContainerColor = DarkNavyBg,
              unfocusedContainerColor = DarkNavyBg,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedIndicatorColor = ButtonOrange,
              unfocusedIndicatorColor = ControllerNavyBorder
            )
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              if (customCommand.isNotBlank()) {
                onSendCommand(customCommand.trim())
                customCommand = ""
              }
            },
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(ButtonOrange)
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "Send",
              tint = Color.Black,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}
