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
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.OVAL
import android.graphics.drawable.GradientDrawable.RECTANGLE
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
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
    @ColorInt selectedColor: Int,
    @ColorInt todayStrokeColor: Int? = null
  ): Drawable {
    val selected: Drawable = circleShape(context, selectedColor)
    val activated: Drawable? = todayStrokeColor?.let {
      circleShape(
          context = context,
          color = it,
          outlineOnly = true
      )
    }

    if (Build.VERSION.SDK_INT >= 21) {
      return RippleDrawable(
          ColorStateList.valueOf(selectedColor),
          StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_selected), selected)
            activated?.let { addState(intArrayOf(android.R.attr.state_activated), it) }
          },
          activated ?: selected
      )
    }

    return StateListDrawable().apply {
      addState(
          intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed),
          selected.mutate().apply {
            alpha = (255 * 0.3).toInt()
          })
      addState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected), selected)
      activated?.let { addState(intArrayOf(android.R.attr.state_activated), it) }
    }
  }

  /** @author Aidan Follestad (@afollestad) */
  @CheckResult fun createRoundedRectangleSelector(
    context: Context,
    @ColorInt selectedColor: Int
  ): Drawable {
    val selected: Drawable = roundedRectangleShape(context, selectedColor)

    if (Build.VERSION.SDK_INT >= 21) {
      return RippleDrawable(
          ColorStateList.valueOf(selectedColor),
          StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_selected), selected)
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
    }
  }

  private fun circleShape(
    context: Context,
    @ColorInt color: Int,
    outlineOnly: Boolean = false
  ): Drawable {
    val result = GradientDrawable().apply {
      shape = OVAL
      if (outlineOnly) {
        setStroke(context.dimenPx(R.dimen.day_of_month_today_border_width), color)
      } else {
        colors = intArrayOf(color, color)
      }
    }
    return if (outlineOnly) {
      val inset = (context.dimenPx(R.dimen.day_of_month_circle_inset) * 0.25f).toInt()
      InsetDrawable(result, inset, inset, inset, inset)
    } else {
      result
    }
  }

  private fun roundedRectangleShape(
    context: Context,
    @ColorInt color: Int
  ): Drawable {
    return GradientDrawable().apply {
      shape = RECTANGLE
      colors = intArrayOf(color, color)
      cornerRadius = context.resources.getDimension(R.dimen.rounded_rectangle_radius)
    }
  }
}
