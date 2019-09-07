/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.date.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.OVAL
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.afollestad.date.R

/** @author Aidan Follestad (@afollestad) */
internal object Util {

  /** @author Aidan Follestad (@afollestad) */
  @CheckResult fun createTextSelector(
    context: Context,
    @ColorInt selectedColor: Int,
    overColoredBackground: Boolean = true
  ): ColorStateList {
    val states = arrayOf(
        intArrayOf(-android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected)
    )
    val disabledTextColor = context.resolveColor(android.R.attr.textColorPrimaryDisableOnly)
    val primaryTextColor = context.resolveColor(android.R.attr.textColorPrimary)
    val colors = intArrayOf(
        disabledTextColor,
        primaryTextColor,
        if (overColoredBackground) {
          if (selectedColor.isColorDark()) Color.WHITE else Color.BLACK
        } else {
          selectedColor
        }
    )
    return ColorStateList(states, colors)
  }

  /** @author Aidan Follestad (@afollestad) */
  @CheckResult fun createCircularSelector(
    context: Context,
    @ColorInt selectedColor: Int
  ): Drawable {
    val selected: Drawable = circleShape(context, selectedColor)
    val activated: Drawable = circleShape(
        context = context,
        color = context.resolveColor(android.R.attr.textColorPrimary),
        outlineOnly = true
    )

    if (Build.VERSION.SDK_INT >= 21) {
      return RippleDrawable(
          ColorStateList.valueOf(selectedColor),
          StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_selected), selected)
            addState(intArrayOf(android.R.attr.state_activated), activated)
          },
          selected
      )
    }

    return StateListDrawable().apply {
      addState(
          intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed),
          selected.mutate().apply {
            alpha = (255 * 0.3).toInt()
          })
      addState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected), selected)
      addState(intArrayOf(android.R.attr.state_activated), activated)
    }
  }

  /** @author Aidan Follestad (@afollestad) */
  @CheckResult fun coloredDrawable(
    context: Context,
    @DrawableRes shapeRes: Int,
    @ColorInt color: Int
  ): Drawable {
    return ContextCompat.getDrawable(context, shapeRes)!!.apply {
      @Suppress("DEPRECATION")
      setColorFilter(color, SRC_IN)
      alpha = Color.alpha(color)
    }
  }

  private fun circleShape(
    context: Context,
    @ColorInt color: Int,
    outlineOnly: Boolean = false
  ): Drawable {
    return GradientDrawable().apply {
      shape = OVAL
      setStroke(context.dimenPx(R.dimen.day_of_month_today_border_width), color)
      if (!outlineOnly) {
        colors = intArrayOf(color, color)
      }
    }
  }
}
