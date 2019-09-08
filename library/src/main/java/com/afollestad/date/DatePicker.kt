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
@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.afollestad.date

import android.content.Context
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.annotation.IntRange
import com.afollestad.date.adapters.MonthItemAdapter
import com.afollestad.date.adapters.YearAdapter
import com.afollestad.date.controllers.DatePickerController
import com.afollestad.date.controllers.MinMaxController
import com.afollestad.date.controllers.VibratorController
import com.afollestad.date.data.DateFormatter
import com.afollestad.date.data.MonthItem
import com.afollestad.date.data.MonthItem.DayOfMonth
import com.afollestad.date.runners.DatePickerLayoutRunner
import com.afollestad.date.runners.DatePickerLayoutRunner.Mode.CALENDAR
import com.afollestad.date.renderers.MonthItemRenderer
import com.afollestad.date.util.TypefaceHelper
import com.afollestad.date.util.font
import com.afollestad.date.view.DatePickerSavedState
import java.lang.Long.MAX_VALUE
import java.util.Calendar

typealias OnDateChanged = (previous: Calendar, date: Calendar) -> Unit

/** @author Aidan Follestad (@afollestad) */
class DatePicker(
  context: Context,
  attrs: AttributeSet?
) : ViewGroup(context, attrs) {

  internal val controller: DatePickerController
  internal val minMaxController = MinMaxController()
  private val layoutRunner: DatePickerLayoutRunner

  private val monthItemAdapter: MonthItemAdapter
  private val yearAdapter: YearAdapter
  private val monthItemRenderer: MonthItemRenderer
  private val dateFormatter: DateFormatter

  init {
    val ta = context.obtainStyledAttributes(attrs, R.styleable.DatePicker)
    val normalFont: Typeface

    try {
      dateFormatter = DateFormatter()
      layoutRunner = DatePickerLayoutRunner.inflateInto(
          context = context,
          typedArray = ta,
          container = this,
          dateFormatter = dateFormatter,
          onDateInput = ::maybeSetDateFromInput
      )
      controller = DatePickerController(
          vibrator = VibratorController(context, ta),
          minMaxController = minMaxController,
          renderHeaders = layoutRunner::setHeadersContent,
          renderMonthItems = ::renderMonthItems,
          goBackVisibility = layoutRunner::showOrHideGoPrevious,
          goForwardVisibility = layoutRunner::showOrHideGoNext,
          switchToDaysOfMonthMode = { layoutRunner.setMode(CALENDAR) },
          dateFormatter = dateFormatter
      )

      normalFont = ta.font(context, R.styleable.DatePicker_date_picker_normal_font) {
        TypefaceHelper.create("sans-serif")
      }
      monthItemRenderer = MonthItemRenderer(
          context = context,
          typedArray = ta,
          normalFont = normalFont,
          minMaxController = minMaxController,
          dateFormatter = dateFormatter
      )
    } finally {
      ta.recycle()
    }

    monthItemAdapter = MonthItemAdapter(
        itemRenderer = monthItemRenderer
    ) { controller.setDayOfMonth(it.date) }
    yearAdapter = YearAdapter(
        normalFont = normalFont,
        selectionColor = layoutRunner.selectionColor
    ) { controller.setYear(it) }

    layoutRunner.setAdapters(monthItemAdapter, yearAdapter)
  }

  /** Sets the date displayed in the view, along with the selected date. */
  fun setDate(
    calendar: Calendar,
    notifyListeners: Boolean = true
  ) = controller.setFullDate(calendar, notifyListeners)

  /** Sets the date and year displayed in the view, along with the selected date (optionally). */
  fun setDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int? = null,
    @IntRange(from = MONTH_MIN, to = MONTH_MAX) month: Int,
    @IntRange(from = 1, to = 31) selectedDate: Int? = null,
    notifyListeners: Boolean = true
  ) = controller.setFullDate(
      year = year, month = month, selectedDate = selectedDate, notifyListeners = notifyListeners
  )

  /** Gets the selected date, if any. */
  @CheckResult fun getDate(): Calendar? = controller.getFullDate()

  /** Gets the min date, if any. */
  fun getMinDate(): Calendar? = minMaxController.getMinDate()

  /** Sets a min date. Dates before this are not selectable. */
  fun setMinDate(calendar: Calendar) {
    minMaxController.setMinDate(calendar)
    controller.render()
  }

  /** Sets a min date. Dates before this are not selectable. */
  fun setMinDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int,
    @IntRange(from = MONTH_MIN, to = MONTH_MAX) month: Int,
    @IntRange(from = 1, to = 31) dayOfMonth: Int
  ) {
    minMaxController.setMinDate(year = year, month = month, dayOfMonth = dayOfMonth)
    controller.render()
  }

  /** Gets the max date, if any. */
  fun getMaxDate(): Calendar? = minMaxController.getMaxDate()

  /** Sets a max date. Dates after this are not selectable. */
  fun setMaxDate(calendar: Calendar) {
    minMaxController.setMaxDate(calendar)
    controller.render()
  }

  /** Sets a max date. Dates after this are not selectable. */
  fun setMaxDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int,
    @IntRange(from = MONTH_MIN, to = MONTH_MAX) month: Int,
    @IntRange(from = 1, to = 31) dayOfMonth: Int
  ) {
    minMaxController.setMaxDate(year = year, month = month, dayOfMonth = dayOfMonth)
    controller.render()
  }

  @Deprecated(
      message = "Use addOnDateChanged instead.",
      replaceWith = ReplaceWith("addOnDateChanged(block)")
  )
  fun onDateChanged(block: (date: Calendar) -> Unit) =
    controller.addDateChangedListener { _, newDate -> block(newDate) }

  /** Appends a listener that is invoked when the selected date changes. */
  fun addOnDateChanged(block: OnDateChanged) = controller.addDateChangedListener(block)

  /** Clears all listeners added via [addOnDateChanged]. */
  fun clearOnDateChanged() = controller.clearDateChangedListeners()

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    controller.maybeInit()
  }

  override fun onSaveInstanceState(): Parcelable? =
    DatePickerSavedState(getDate(), super.onSaveInstanceState())

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state is DatePickerSavedState) {
      super.onRestoreInstanceState(state.superState)
      state.selectedDate?.let { controller.setFullDate(it, false) }
    } else {
      super.onRestoreInstanceState(state)
    }
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    layoutRunner.onNavigate(
        controller::previousMonth,
        controller::nextMonth
    )
  }

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    layoutRunner.onMeasure(widthMeasureSpec, heightMeasureSpec)
        .let { (width, height) -> setMeasuredDimension(width, height) }
  }

  override fun onLayout(
    changed: Boolean,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
  ) = layoutRunner.onLayout(left = left, top = top)

  private fun renderMonthItems(days: List<MonthItem>) {
    val firstDayOfMonth = days.first { it is DayOfMonth } as DayOfMonth
    yearAdapter.selectedYear = firstDayOfMonth
        .month
        .year
    yearAdapter.getSelectedPosition()
        ?.let(layoutRunner::scrollToYearPosition)
    monthItemAdapter.items = days
  }

  private fun maybeSetDateFromInput(input: CharSequence) {
    controller.maybeSetDateFromInput(input)
  }

  private companion object {
    const val MONTH_MIN: Long = 0 // Calendar.JANUARY
    const val MONTH_MAX: Long = 11 // Calendar.DECEMBER
  }
}
