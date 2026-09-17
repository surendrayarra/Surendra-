package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ControllerNavy
import com.example.ui.theme.ControllerNavyBorder
import com.example.ui.theme.ControllerNavyDark

/**
 * Draws the distinctive butterfly gamepad chassis matching the reference image:
 * rounded outer wing lobes, straight top bridge, and curved concave bottom waist.
 */
@Composable
fun ButterflyChassis(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit
) {
  Box(modifier = modifier) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      if (w <= 0 || h <= 0) return@Canvas

      // Butterfly path
      // Radius of the left and right circular wings
      val wingR = h / 2f
      val leftCenter = Offset(wingR, wingR)
      val rightCenter = Offset(w - wingR, wingR)

      val path = Path().apply {
        // Start top-left of the bridge
        moveTo(wingR, 0f)

        // Line across top bridge to right wing
        lineTo(w - wingR, 0f)

        // Arc around right wing (from top to bottom: -90 deg to +90 deg)
        arcTo(
          rect = Rect(
            left = w - 2 * wingR,
            top = 0f,
            right = w,
            bottom = h
          ),
          startAngleDegrees = -90f,
          sweepAngleDegrees = 180f,
          forceMoveTo = false
        )

        // Curve across bottom bridge with concave waist dip in middle
        val bottomDipDepth = h * 0.22f
        val waistLeft = w * 0.38f
        val waistRight = w * 0.62f
        val waistCenter = w * 0.5f

        // Line from right wing to waist right
        lineTo(w - wingR, h)
        // Smooth bezier dip upward into the waist
        cubicTo(
          x1 = w - wingR * 0.8f, y1 = h,
          x2 = waistRight, y2 = h - bottomDipDepth,
          x3 = waistCenter, y3 = h - bottomDipDepth
        )
        cubicTo(
          x1 = waistLeft, y1 = h - bottomDipDepth,
          x2 = wingR * 0.8f, y2 = h,
          x3 = wingR, y3 = h
        )

        // Arc around left wing (from bottom to top: 90 deg to 270 deg)
        arcTo(
          rect = Rect(
            left = 0f,
            top = 0f,
            right = 2 * wingR,
            bottom = h
          ),
          startAngleDegrees = 90f,
          sweepAngleDegrees = 180f,
          forceMoveTo = false
        )

        close()
      }

      // 1. Soft ambient drop shadow around controller body
      drawPath(
        path = path,
        color = Color(0x66000000)
      )

      // 2. Chassis body fill with subtle depth gradient
      drawPath(
        path = path,
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xFF1E4861),
            ControllerNavy,
            ControllerNavyDark
          )
        )
      )

      // 3. Subtle rim stroke
      drawPath(
        path = path,
        color = ControllerNavyBorder,
        style = Stroke(width = 2.dp.toPx())
      )

      // 4. Subtle inner highlight along top edge
      drawLine(
        brush = Brush.horizontalGradient(
          colors = listOf(
            Color.Transparent,
            Color(0x33FFFFFF),
            Color.Transparent
          ),
          startX = wingR * 0.5f,
          endX = w - wingR * 0.5f
        ),
        start = Offset(wingR * 0.5f, 2.dp.toPx()),
        end = Offset(w - wingR * 0.5f, 2.dp.toPx()),
        strokeWidth = 2.dp.toPx()
      )
    }

    content()
  }
}
