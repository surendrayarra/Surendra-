package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ButtonOrange
import com.example.ui.theme.ButtonOrangeDark
import com.example.ui.theme.ButtonOrangeLight
import com.example.ui.theme.ButtonTextBlack

/**
 * Orange pill action button for "LIGHT ON" and "LIGHT OFF"
 */
@Composable
fun LightActionButton(
  label: String,
  isActive: Boolean = false,
  testTag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (isPressed) 0.93f else 1f, label = "button_scale")

  val shape = RoundedCornerShape(22.dp)

  Box(
    modifier = modifier
      .scale(scale)
      .width(96.dp)
      .height(64.dp)
      .shadow(elevation = if (isPressed) 2.dp else 6.dp, shape = shape, spotColor = Color.Black)
      .clip(shape)
      .background(
        brush = Brush.verticalGradient(
          colors = if (isPressed) {
            listOf(ButtonOrangeDark, ButtonOrange)
          } else {
            listOf(ButtonOrangeLight, ButtonOrange, ButtonOrangeDark)
          }
        )
      )
      .border(
        width = 2.dp,
        color = if (isActive) Color(0xFFFFD54F) else Color(0x66000000),
        shape = shape
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onClick()
        }
      )
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = ButtonTextBlack,
      fontSize = 15.sp,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif,
      letterSpacing = 1.sp,
      lineHeight = 18.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 4.dp)
    )
  }
}

/**
 * Large stacked orange rounded rectangle button for "START" and "STOP"
 */
@Composable
fun MainControlButton(
  label: String,
  isActive: Boolean = false,
  testTag: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "main_btn_scale")

  val shape = RoundedCornerShape(24.dp)

  Box(
    modifier = modifier
      .scale(scale)
      .width(136.dp)
      .height(62.dp)
      .shadow(elevation = if (isPressed) 2.dp else 8.dp, shape = shape, spotColor = Color.Black)
      .clip(shape)
      .background(
        brush = Brush.verticalGradient(
          colors = if (isPressed) {
            listOf(ButtonOrangeDark, ButtonOrange)
          } else {
            listOf(ButtonOrangeLight, ButtonOrange, ButtonOrangeDark)
          }
        )
      )
      .border(
        width = 2.dp,
        color = if (isActive) Color(0xFFFFD54F) else Color(0x66000000),
        shape = shape
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onClick()
        }
      )
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = ButtonTextBlack,
      fontSize = 22.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.SansSerif,
      letterSpacing = 2.sp,
      textAlign = TextAlign.Center
    )
  }
}
