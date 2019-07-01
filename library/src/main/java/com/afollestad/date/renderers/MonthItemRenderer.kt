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
import android.view.View
import android.widget.TextView
import com.afollestad.date.R
import com.afollestad.date.controllers.MinMaxController
import com.afollestad.date.data.DayOfWeek
import com.afollestad.date.data.MonthItem
import com.afollestad.date.data.MonthItem.DayOfMonth
import com.afollestad.date.data.MonthItem.WeekHeader
import com.afollestad.date.data.NO_DATE
import com.afollestad.date.util.Util.coloredDrawable
import com.afollestad.date.util.Util.createCircularSelector
import com.afollestad.date.util.Util.createTextSelector
import com.afollestad.date.util.color
import com.afollestad.date.util.resolveColor
import com.afollestad.date.util.withAlpha
import com.afollestad.date.data.snapshot.DateSnapshot
import com.afollestad.date.util.onClickDebounced

// TODO write unit tests
/** @author Aidan Follestad (@afollestad) */
internal class MonthItemRenderer(
  private val context: Context,
  typedArray: TypedArray,
  private val normalFont: Typeface,
  private val minMaxController: MinMaxController
) {
  private val selectionColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_selection_color) {
      context.resolveColor(R.attr.colorAccent)
    }
  private val disabledBackgroundColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_disabled_background_color) {
      context.resolveColor(android.R.attr.textColorSecondary)
          .withAlpha(DEFAULT_DISABLED_BACKGROUND_OPACITY)
    }

  fun render(
    item: MonthItem,
    rootView: View,
    textView: TextView,
    onSelection: (DayOfMonth) -> Unit
  ) {
    when (item) {
      is WeekHeader -> renderWeekHeader(item.dayOfWeek, textView)
      is DayOfMonth -> renderDayOfMonth(item, rootView, textView, onSelection)
    }
  }

  private fun renderWeekHeader(
    dayOfWeek: DayOfWeek,
    textView: TextView
  ) {
    textView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorSecondary))
      text = dayOfWeek.name.first()
          .toString()
      typeface = normalFont
    }
  }

  private fun renderDayOfMonth(
    dayOfMonth: DayOfMonth,
    rootView: View,
    textView: TextView,
    onSelection: (DayOfMonth) -> Unit
  ) {
    rootView.background = null
    textView.apply {
      setTextColor(createTextSelector(context, selectionColor))
      text = dayOfMonth.date.positiveOrEmptyAsString()
      typeface = normalFont
      gravity = CENTER
      background = null
      setOnClickListener(null)
    }

    if (dayOfMonth.date == NO_DATE) {
      rootView.isEnabled = false
      textView.isSelected = false
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
        val drawableRes = minMaxController.getOutOfMinRangeBackgroundRes(currentDate)
        rootView.apply {
          background = coloredDrawable(context, drawableRes, disabledBackgroundColor)
          isEnabled = false
        }
      }
      minMaxController.isOutOfMaxRange(currentDate) -> {
        val drawable = minMaxController.getOutOfMaxRangeBackgroundRes(currentDate)
        rootView.apply {
          background = coloredDrawable(context, drawable, disabledBackgroundColor)
          isEnabled = false
        }
      }
      else -> {
        rootView.isEnabled = textView.text.toString()
            .isNotEmpty()
        textView.apply {
          background = createCircularSelector(selectionColor)
          onClickDebounced { onSelection(dayOfMonth) }
        }
      }
    }
  }

  private fun Int.positiveOrEmptyAsString(): String {
    return if (this < 1) "" else toString()
  }

  private companion object {
    const val DEFAULT_DISABLED_BACKGROUND_OPACITY: Float = 0.3f
  }
}
