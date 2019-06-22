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
package com.afollestad.date.renderers

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.view.Gravity.CENTER
import androidx.annotation.VisibleForTesting
import com.afollestad.date.R
import com.afollestad.date.controllers.MinMaxController
import com.afollestad.date.internal.DayOfMonth
import com.afollestad.date.internal.NO_DATE
import com.afollestad.date.internal.Util.coloredDrawable
import com.afollestad.date.internal.Util.createCircularSelector
import com.afollestad.date.internal.Util.createTextSelector
import com.afollestad.date.internal.color
import com.afollestad.date.internal.onClickDebounced
import com.afollestad.date.internal.resolveColor
import com.afollestad.date.internal.withAlpha
import com.afollestad.date.snapshot.DateSnapshot
import com.afollestad.date.view.DayOfMonthTextView

// TODO write unit tests
/** @author Aidan Follestad (@afollestad) */
internal class DayOfMonthRenderer(
  private val context: Context,
  typedArray: TypedArray,
  private val normalFont: Typeface,
  private val minMaxController: MinMaxController
) {
  internal val selectionColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_selection_color) {
      context.resolveColor(R.attr.colorAccent)
    }
  private val disabledBackgroundColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_disabled_background_color) {
      context.resolveColor(android.R.attr.textColorSecondary)
          .withAlpha(DEFAULT_DISABLED_BACKGROUND_OPACITY)
    }

  @VisibleForTesting fun render(
    dayOfMonth: DayOfMonth,
    textView: DayOfMonthTextView,
    onSelection: (Int) -> Unit
  ) {
    textView.setTextColor(createTextSelector(context, selectionColor))
    textView.text = dayOfMonth.date.positiveOrEmptyAsString()
    textView.typeface = normalFont
    textView.gravity = CENTER

    if (dayOfMonth.date == NO_DATE) {
      textView.isEnabled = false
      textView.isSelected = false
      textView.background = null
      textView.skipInset = false
      return
    }

    val currentDate = DateSnapshot(
        month = dayOfMonth.month.month,
        year = dayOfMonth.month.year,
        day = dayOfMonth.date
    )
    textView.isSelected = dayOfMonth.isSelected

    when {
      minMaxController.isOutOfMinRange(currentDate) -> {
        textView.skipInset = true
        val drawableRes = minMaxController.getOutOfMinRangeBackgroundRes(currentDate)
        textView.background = coloredDrawable(context, drawableRes, disabledBackgroundColor)
        textView.isEnabled = false
      }
      minMaxController.isOutOfMaxRange(currentDate) -> {
        textView.skipInset = true
        val drawable = minMaxController.getOutOfMaxRangeBackgroundRes(currentDate)
        textView.background = coloredDrawable(context, drawable, disabledBackgroundColor)
        textView.isEnabled = false
      }
      else -> {
        textView.skipInset = false
        textView.background = createCircularSelector(selectionColor)
        textView.isEnabled = textView.text.toString()
            .isNotEmpty()
        textView.onClickDebounced {
          onSelection(it.text.toString().toInt())
        }
      }
    }
  }

  fun renderAll(
    daysOfMonth: List<DayOfMonth>,
    views: List<DayOfMonthTextView>,
    onSelection: (Int) -> Unit
  ) {
    require(daysOfMonth.size == views.size) {
      "Days of month size (${daysOfMonth.size}) should equal views size (${views.size})."
    }
    daysOfMonth.forEachIndexed { index, dayOfMonth ->
      render(dayOfMonth, views[index], onSelection)
    }
  }

  private fun Int.positiveOrEmptyAsString(): String {
    return if (this < 1) "" else toString()
  }

  private companion object {
    const val DEFAULT_DISABLED_BACKGROUND_OPACITY: Float = 0.3f
  }
}
