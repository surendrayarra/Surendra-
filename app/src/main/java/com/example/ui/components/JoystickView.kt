package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ButtonTextBlack
import com.example.ui.theme.JoystickCenterBorder
import com.example.ui.theme.JoystickCenterWhite
import com.example.ui.theme.JoystickOrange
import com.example.ui.theme.JoystickOrangeDark
import com.example.ui.theme.JoystickRingDark
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Custom Analog Joystick reproducing the exact concentric orange design
 * from the controller reference image.
 */
@Composable
fun JoystickView(
  modifier: Modifier = Modifier,
  size: Dp = 190.dp,
  testTag: String = "analog_joystick",
  onMove: (x: Int, y: Int, isRelease: Boolean) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()

  // Track raw thumb offset in pixels relative to center
  val thumbOffsetX = remember { Animatable(0f) }
  val thumbOffsetY = remember { Animatable(0f) }

  var isDragging by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .size(size)
      .testTag(testTag)
      .pointerInput(Unit) {
        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
        val maxRadius = size.toPx() * 0.32f

        detectDragGestures(
          onDragStart = { offset ->
            isDragging = true
            val dx = offset.x - center.x
            val dy = offset.y - center.y
            val distance = sqrt(dx * dx + dy * dy)
            val angle = atan2(dy, dx)
            val clampedDist = min(distance, maxRadius)

            val targetX = clampedDist * cos(angle)
            val targetY = clampedDist * sin(angle)

            coroutineScope.launch {
              thumbOffsetX.snapTo(targetX)
              thumbOffsetY.snapTo(targetY)
            }

            val normX = ((targetX / maxRadius) * 100).roundToInt().coerceIn(-100, 100)
            val normY = ((-targetY / maxRadius) * 100).roundToInt().coerceIn(-100, 100)
            onMove(normX, normY, false)
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val newX = thumbOffsetX.value + dragAmount.x
            val newY = thumbOffsetY.value + dragAmount.y
            val distance = sqrt(newX * newX + newY * newY)
            val angle = atan2(newY, newX)
            val clampedDist = min(distance, maxRadius)

            val targetX = clampedDist * cos(angle)
            val targetY = clampedDist * sin(angle)

            coroutineScope.launch {
              thumbOffsetX.snapTo(targetX)
              thumbOffsetY.snapTo(targetY)
            }

            // Normalizing: X is -100 to 100 (left to right), Y is -100 to 100 (bottom to top, standard Cartesian)
            val normX = ((targetX / maxRadius) * 100).roundToInt().coerceIn(-100, 100)
            val normY = ((-targetY / maxRadius) * 100).roundToInt().coerceIn(-100, 100)
            onMove(normX, normY, false)
          },
          onDragEnd = {
            isDragging = false
            coroutineScope.launch {
              launch {
                thumbOffsetX.animateTo(
                  0f,
                  animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
              }
              launch {
                thumbOffsetY.animateTo(
                  0f,
                  animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
              }
            }
            onMove(0, 0, true)
          },
          onDragCancel = {
            isDragging = false
            coroutineScope.launch {
              thumbOffsetX.animateTo(0f)
              thumbOffsetY.animateTo(0f)
            }
            onMove(0, 0, true)
          }
        )
      },
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val canvasCenter = Offset(this.size.width / 2f, this.size.height / 2f)
      val radius = this.size.minDimension / 2f

      // 1. Outer Dark Shadow Bevel Ring
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(JoystickRingDark, Color(0xFF091F2C)),
          center = canvasCenter,
          radius = radius
        ),
        radius = radius,
        center = canvasCenter
      )

      // Outer border outline
      drawCircle(
        color = Color(0x33FFFFFF),
        radius = radius,
        center = canvasCenter,
        style = Stroke(width = 2.dp.toPx())
      )

      // 2. Wide Orange Base Ring (as seen in image)
      val baseOrangeRadius = radius * 0.94f
      drawCircle(
        brush = Brush.linearGradient(
          colors = listOf(Color(0xFFFF8000), JoystickOrange, JoystickOrangeDark),
          start = Offset(canvasCenter.x - radius, canvasCenter.y - radius),
          end = Offset(canvasCenter.x + radius, canvasCenter.y + radius)
        ),
        radius = baseOrangeRadius,
        center = canvasCenter
      )

      // 3. Inner Dark Teal Recessed Groove
      val grooveRadius = radius * 0.72f
      drawCircle(
        color = Color(0xFF0E3042),
        radius = grooveRadius,
        center = canvasCenter,
        style = Stroke(width = 4.dp.toPx())
      )

      // 4. Moving Thumb Stick Knob
      val currentThumbCenter = Offset(
        canvasCenter.x + thumbOffsetX.value,
        canvasCenter.y + thumbOffsetY.value
      )

      val thumbOuterRadius = radius * 0.54f

      // Drop shadow under thumb when moved
      if (isDragging) {
        drawCircle(
          color = Color(0x66000000),
          radius = thumbOuterRadius + 4.dp.toPx(),
          center = currentThumbCenter + Offset(0f, 6.dp.toPx())
        )
      }

      // Outer 3D Orange Ring of Thumb Knob
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(Color(0xFFFF851B), JoystickOrange, JoystickOrangeDark),
          center = currentThumbCenter - Offset(thumbOuterRadius * 0.3f, thumbOuterRadius * 0.3f),
          radius = thumbOuterRadius * 1.2f
        ),
        radius = thumbOuterRadius,
        center = currentThumbCenter
      )

      // Subtle metallic highlight ring on the orange thumb
      drawCircle(
        color = Color(0x44FFFFFF),
        radius = thumbOuterRadius * 0.96f,
        center = currentThumbCenter,
        style = Stroke(width = 2.dp.toPx())
      )

      // 5. White/Light Concave Center Thumb Cap
      val centerCapRadius = radius * 0.32f

      // Center inner shadow / border
      drawCircle(
        color = Color(0xFF94A3B8),
        radius = centerCapRadius + 2.dp.toPx(),
        center = currentThumbCenter
      )

      // Center White Dish
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White,
            JoystickCenterWhite,
            Color(0xFFE2E8F0)
          ),
          center = currentThumbCenter - Offset(centerCapRadius * 0.2f, centerCapRadius * 0.2f),
          radius = centerCapRadius
        ),
        radius = centerCapRadius,
        center = currentThumbCenter
      )

      // Gentle concave ring inset inside white pad
      drawCircle(
        color = Color(0x1F000000),
        radius = centerCapRadius * 0.68f,
        center = currentThumbCenter,
        style = Stroke(width = 1.5.dp.toPx())
      )
    }
  }
}
