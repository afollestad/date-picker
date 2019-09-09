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
package com.afollestad.date.runners

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.view.View
import android.view.View.MeasureSpec.getSize
import android.view.ViewGroup
import androidx.annotation.CheckResult
import com.afollestad.date.R
import com.afollestad.date.adapters.MonthItemAdapter
import com.afollestad.date.adapters.YearAdapter
import com.afollestad.date.controllers.VibratorController
import com.afollestad.date.data.DateFormatter
import com.afollestad.date.data.snapshot.DateSnapshot
import com.afollestad.date.data.snapshot.MonthSnapshot
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.runners.Mode.INPUT_EDIT
import com.afollestad.date.runners.Mode.YEAR_LIST
import com.afollestad.date.runners.base.Bounds
import com.afollestad.date.runners.base.LayoutRunner
import com.afollestad.date.runners.base.Orientation.PORTRAIT
import com.afollestad.date.runners.base.Size

/** @author Aidan Follestad (@afollestad) */
internal class DatePickerLayoutRunner(
  context: Context,
  typedArray: TypedArray,
  root: ViewGroup,
  private val vibrator: VibratorController,
  dateFormatter: DateFormatter,
  onDateInput: (CharSequence) -> Unit
) : LayoutRunner(context, typedArray) {
  private val headerLayoutRunner =
    DatePickerHeaderLayoutRunner(context, root, typedArray, dateFormatter, this)
  private val navigationLayoutRunner =
    DatePickerNavigationLayoutRunner(context, root, typedArray, dateFormatter, this)
  private val calendarLayoutRunner =
    DatePickerCalendarLayoutRunner(context, root, typedArray)
  private val yearsLayoutRunner =
    DatePickerYearsLayoutRunner(context, root, typedArray)
  private val manualInputLayoutRunner =
    DatePickerManualInputLayoutRunner(context, root, typedArray, dateFormatter, onDateInput)

  init {
    typedArray.getInt(R.styleable.DatePicker_date_picker_default_mode, CALENDAR.rawValue)
        .let { Mode.fromRawValue(it) }
        .let { if (it != CALENDAR) setMode(it) }
  }

  fun setAdapters(
    monthItemAdapter: MonthItemAdapter,
    yearAdapter: YearAdapter
  ) {
    calendarLayoutRunner.setAdapter(monthItemAdapter)
    yearsLayoutRunner.setAdapter(yearAdapter)
  }

  fun setHeadersContent(
    currentMonth: MonthSnapshot,
    selectedDate: DateSnapshot,
    fromUserEditInput: Boolean
  ) {
    headerLayoutRunner.setCurrentDate(selectedDate)
    navigationLayoutRunner.setCurrentDate(currentMonth)
    if (!fromUserEditInput) {
      manualInputLayoutRunner.setCurrentDate(selectedDate)
    }
  }

  fun scrollToYearPosition(pos: Int) = yearsLayoutRunner.scrollToPosition(pos)

  @SuppressLint("CheckResult")
  override fun measure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
    totalHeightSoFar: Int
  ): Size {
    val parentWidth: Int = getSize(widthMeasureSpec)
    var heightSoFar: Int = totalHeightSoFar

    heightSoFar += nonZeroIf(
        headerLayoutRunner.measure(widthMeasureSpec, heightMeasureSpec, heightSoFar).height,
        orientation == PORTRAIT
    )
    heightSoFar += navigationLayoutRunner
        .measure(widthMeasureSpec, heightMeasureSpec, heightSoFar)
        .height
    heightSoFar += calendarLayoutRunner
        .measure(widthMeasureSpec, heightMeasureSpec, heightSoFar)
        .height

    // These last two don't get put into the overall height because they overlay over the above.
    yearsLayoutRunner.measure(widthMeasureSpec, heightMeasureSpec, heightSoFar)
    manualInputLayoutRunner.measure(widthMeasureSpec, heightMeasureSpec, heightSoFar)

    return Size(width = parentWidth, height = heightSoFar)
  }

  private fun nonZeroIf(
    value: Int,
    condition: Boolean
  ): Int {
    return if (condition) value else 0
  }

  @SuppressLint("CheckResult")
  override fun layout(
    top: Int,
    left: Int,
    right: Int,
    parentWidth: Int
  ): Bounds {
    val headerBounds = headerLayoutRunner.layout(
        top = top,
        left = left,
        right = right,
        parentWidth = parentWidth
    )

    val navigationAndInputTop = if (orientation == PORTRAIT) headerBounds.bottom else 0
    val nonHeaderLeft = if (orientation == PORTRAIT) 0 else headerBounds.right

    val navigationBounds = navigationLayoutRunner.layout(
        top = navigationAndInputTop,
        left = nonHeaderLeft,
        right = right,
        parentWidth = parentWidth
    )
    val calendarBounds = calendarLayoutRunner.layout(
        top = navigationBounds.bottom,
        left = nonHeaderLeft,
        right = right,
        parentWidth = parentWidth
    )

    yearsLayoutRunner.layout(
        top = calendarBounds.top,
        left = calendarBounds.left,
        right = calendarBounds.right,
        parentWidth = parentWidth
    )
    manualInputLayoutRunner.layout(
        top = navigationAndInputTop,
        left = nonHeaderLeft,
        right = right,
        parentWidth = parentWidth
    )

    return bounds.apply {
      this.left = headerBounds.left
      this.top = headerBounds.top
      this.right = headerBounds.right
      this.bottom = calendarBounds.bottom
    }
  }

  fun onNavigate(
    onGoToPrevious: () -> Unit,
    onGoToNext: () -> Unit
  ) = navigationLayoutRunner.onNavigate(onGoToPrevious, onGoToNext)

  override fun setMode(mode: Mode) {
    headerLayoutRunner.setMode(mode)
    navigationLayoutRunner.setMode(mode)
    calendarLayoutRunner.setMode(mode)
    yearsLayoutRunner.setMode(mode)
    manualInputLayoutRunner.setMode(mode)
    vibrator.vibrateForSelection()
    super.setMode(mode)
  }

  fun toggleMode(wantedMode: Mode = YEAR_LIST) {
    when (lastMode) {
      null, CALENDAR -> setMode(wantedMode)
      INPUT_EDIT -> setMode(CALENDAR)
      else -> setMode(CALENDAR)
    }
  }

  companion object {
    @CheckResult fun inflateInto(
      context: Context,
      typedArray: TypedArray,
      container: ViewGroup,
      dateFormatter: DateFormatter,
      onDateInput: (CharSequence) -> Unit
    ): DatePickerLayoutRunner {
      View.inflate(context, R.layout.date_picker, container)
      val vibrator = VibratorController(context, typedArray)
      return DatePickerLayoutRunner(
          context, typedArray, container, vibrator, dateFormatter, onDateInput
      )
    }
  }
}

internal enum class Mode(val rawValue: Int) {
  CALENDAR(1),
  YEAR_LIST(2),
  INPUT_EDIT(3);

  companion object {
    fun fromRawValue(value: Int): Mode {
      return values().single { it.rawValue == value }
    }
  }
}
