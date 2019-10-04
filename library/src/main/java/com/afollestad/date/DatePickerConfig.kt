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
package com.afollestad.date

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.afollestad.date.controllers.VibratorController
import com.afollestad.date.data.DateFormatter
import com.afollestad.date.runners.Mode
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.util.ObservableValue
import com.afollestad.date.util.TypefaceHelper
import com.afollestad.date.util.color
import com.afollestad.date.util.font
import com.afollestad.date.util.resolveColor
import com.afollestad.date.util.string
import com.afollestad.date.util.withAlpha

/** @author Aidan Follestad (@afollestad) */
internal data class DatePickerConfig(
  @ColorInt val headerBackgroundColor: Int,
  @ColorInt val selectionColor: Int,
  val normalFont: Typeface,
  val mediumFont: Typeface,
  @Px val horizontalPadding: Int,
  @ColorInt val todayStrokeColor: Int,
  val title: CharSequence,
  val manualInputLabel: CharSequence,
  val defaultMode: Mode,
  val vibrator: VibratorController?,
  val dateFormatter: DateFormatter = DateFormatter(),
  var currentMode: ObservableValue<Mode> = ObservableValue(defaultMode)
) {
  companion object {
    const val DEFAULT_TODAY_STROKE_OPACITY: Float = 0.6f
  }
}

@CheckResult
internal fun DatePickerConfig.Companion.create(
  context: Context,
  attrs: AttributeSet?
): DatePickerConfig {
  val ta = context.obtainStyledAttributes(attrs, R.styleable.DatePicker)
  val selectionVibrates = ta.getBoolean(
      R.styleable.DatePicker_date_picker_selection_vibrates, true
  )
  return DatePickerConfig(
      headerBackgroundColor = ta.color(R.styleable.DatePicker_date_picker_header_background_color) {
        context.resolveColor(R.attr.colorAccent)
      },
      selectionColor = ta.color(R.styleable.DatePicker_date_picker_selection_color) {
        context.resolveColor(R.attr.colorAccent)
      },
      normalFont = ta.font(context, R.styleable.DatePicker_date_picker_normal_font) {
        TypefaceHelper.create("sans-serif")
      },
      mediumFont = ta.font(context, R.styleable.DatePicker_date_picker_medium_font) {
        TypefaceHelper.create("sans-serif-medium")
      },
      horizontalPadding = ta.getDimensionPixelSize(
          R.styleable.DatePicker_date_picker_calendar_horizontal_padding, 0
      ),
      todayStrokeColor = ta.color(R.styleable.DatePicker_date_picker_calendar_today_stroke_color) {
        context.resolveColor(android.R.attr.textColorPrimary)
            .withAlpha(DEFAULT_TODAY_STROKE_OPACITY)
      },
      title = ta.string(context, R.styleable.DatePicker_date_picker_title) {
        context.getString(R.string.select_date)
      },
      manualInputLabel = ta.string(context, R.styleable.DatePicker_date_picker_manual_input_label) {
        context.getString(R.string.enter_date)
      },
      defaultMode = ta.getInt(R.styleable.DatePicker_date_picker_default_mode, CALENDAR.rawValue)
          .let { Mode.fromRawValue(it) },
      vibrator = if (selectionVibrates) VibratorController(context) else null
  ).also { ta.recycle() }
}
