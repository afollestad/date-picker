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
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.annotation.IntRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.internal.DateFormatter
import com.afollestad.date.controllers.DatePickerController
import com.afollestad.date.controllers.MinMaxController
import com.afollestad.date.controllers.VibratorController
import com.afollestad.date.internal.DayOfMonth
import com.afollestad.date.internal.DayOfWeek
import com.afollestad.date.internal.TypefaceHelper
import com.afollestad.date.internal.Util.createCircularSelector
import com.afollestad.date.internal.YearAdapter
import com.afollestad.date.internal.attachTopDivider
import com.afollestad.date.internal.color
import com.afollestad.date.internal.conceal
import com.afollestad.date.internal.concealAll
import com.afollestad.date.internal.font
import com.afollestad.date.internal.hide
import com.afollestad.date.internal.invalidateTopDividerNow
import com.afollestad.date.internal.isConcealed
import com.afollestad.date.internal.isVisible
import com.afollestad.date.internal.onClickDebounced
import com.afollestad.date.internal.resolveColor
import com.afollestad.date.internal.show
import com.afollestad.date.internal.showAll
import com.afollestad.date.internal.showOrConceal
import com.afollestad.date.renderers.DayOfMonthRenderer
import com.afollestad.date.renderers.WeekdayHeaderRenderer
import com.afollestad.date.snapshot.DateSnapshot
import com.afollestad.date.view.RatioTextView
import java.lang.Long.MAX_VALUE
import java.util.Calendar

typealias OnDateChanged = (date: Calendar) -> Unit

/** @author Aidan Follestad (@afollestad) */
class DatePicker(
  context: Context,
  attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

  // Non-nullables
  internal val controller: DatePickerController
  internal val minMaxController = MinMaxController()

  private val vibrator: VibratorController
  private val dateFormatter = DateFormatter()
  private val yearAdapter: YearAdapter
  private val weekdayHeaderRenderer: WeekdayHeaderRenderer
  private val dayOfMonthRenderer: DayOfMonthRenderer

  // Late inits
  private lateinit var selectedYearView: TextView
  private lateinit var selectedDateView: TextView
  private lateinit var visibleMonthView: TextView
  private lateinit var goPreviousMonthView: View
  private lateinit var goNextMonthView: View
  private lateinit var weekdayHeaderViews: MutableList<TextView>
  private lateinit var dayOfMonthViews: MutableList<RatioTextView>
  private lateinit var yearsRecyclerView: RecyclerView
  private lateinit var yearsDividerView: View

  // Config properties
  private val headerBackgroundColor: Int
  internal val normalFont: Typeface
  private val mediumFont: Typeface

  init {
    inflate(context, R.layout.date_picker, this)
    val ta = context.obtainStyledAttributes(attrs, R.styleable.DatePicker)
    try {
      vibrator = VibratorController(context, ta)
      controller = DatePickerController(
          vibrator = vibrator,
          minMaxController = minMaxController,
          renderHeaders = ::renderHeaders,
          renderDaysOfWeek = ::renderDaysOfWeek,
          renderDaysOfMonth = ::renderDaysOfMonth,
          goBackVisibility = { goPreviousMonthView.showOrConceal(it) },
          goForwardVisibility = { goNextMonthView.showOrConceal(it) },
          switchToMonthMode = ::switchToMonthMode
      )

      headerBackgroundColor = ta.color(R.styleable.DatePicker_date_picker_header_background_color) {
        context.resolveColor(R.attr.colorAccent)
      }
      normalFont = ta.font(context, R.styleable.DatePicker_date_picker_normal_font) {
        TypefaceHelper.create("sans-serif")
      }
      mediumFont = ta.font(context, R.styleable.DatePicker_date_picker_medium_font) {
        TypefaceHelper.create("sans-serif-medium")
      }

      weekdayHeaderRenderer = WeekdayHeaderRenderer(normalFont)
      dayOfMonthRenderer = DayOfMonthRenderer(
          context = context,
          typedArray = ta,
          normalFont = normalFont,
          minMaxController = minMaxController
      )
    } finally {
      ta.recycle()
    }
    yearAdapter = YearAdapter(dayOfMonthRenderer.selectionColor) {
      controller.setYear(it)
    }
  }

  /** Sets the month displayed in the view, along with the selected date. */
  fun setDate(
    calendar: Calendar,
    notifyListeners: Boolean = true
  ) = controller.setFullDate(calendar, notifyListeners)

  /** Sets the month and year displayed in the view, along with the selected selectedDate (optionally). */
  fun setDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int? = null,
    month: Int,
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
  fun setMinDate(calendar: Calendar) = minMaxController.setMinDate(calendar)

  /** Sets a min date. Dates before this are not selectable. */
  fun setMinDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int,
    month: Int,
    @IntRange(from = 1, to = 31) dayOfMonth: Int
  ) = minMaxController.setMinDate(year = year, month = month, dayOfMonth = dayOfMonth)

  /** Gets the max date, if any. */
  fun getMaxDate(): Calendar? = minMaxController.getMaxDate()

  /** Sets a max date. Dates after this are not selectable. */
  fun setMaxDate(calendar: Calendar) = minMaxController.setMaxDate(calendar)

  /** Sets a max date. Dates after this are not selectable. */
  fun setMaxDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int,
    month: Int,
    @IntRange(from = 1, to = 31) dayOfMonth: Int
  ) = minMaxController.setMaxDate(year = year, month = month, dayOfMonth = dayOfMonth)

  /** Appends a listener that is invoked when the selected date changes. */
  fun onDateChanged(block: OnDateChanged) = controller.addDateChangedListener(block)

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    controller.maybeInit()
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    visibleMonthView = findViewById<TextView>(R.id.current_month).apply {
      typeface = mediumFont
    }
    selectedYearView = findViewById<TextView>(R.id.current_year).apply {
      background = ColorDrawable(headerBackgroundColor)
      typeface = normalFont
      onClickDebounced { switchToYearMode() }
    }
    selectedDateView = findViewById<TextView>(R.id.current_date).apply {
      background = ColorDrawable(headerBackgroundColor)
      typeface = mediumFont
      onClickDebounced { switchToMonthMode() }
    }

    weekdayHeaderViews = mutableListOf()
    dayOfMonthViews = mutableListOf()
    for (childIndex in 0 until childCount) {
      val child = getChildAt(childIndex) ?: break
      if (child.tag == "weekday_header") {
        weekdayHeaderViews.add(child as TextView)
      } else if (child.tag == "day_of_month") {
        dayOfMonthViews.add(child as RatioTextView)
      }
    }

    yearsDividerView = findViewById(R.id.year_list_divider)
    yearsRecyclerView = findViewById<RecyclerView>(R.id.year_list).apply {
      layoutManager = LinearLayoutManager(context)
      adapter = yearAdapter
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
      attachTopDivider(yearsDividerView)
    }

    goPreviousMonthView = findViewById<View>(R.id.left_chevron).apply {
      background = createCircularSelector(dayOfMonthRenderer.selectionColor)
      onClickDebounced { controller.previousMonth() }
    }
    goNextMonthView = findViewById<View>(R.id.right_chevron).apply {
      background = createCircularSelector(dayOfMonthRenderer.selectionColor)
      onClickDebounced { controller.nextMonth() }
    }
  }

  private fun switchToYearMode() {
    if (yearsRecyclerView.isVisible()) return
    yearsRecyclerView.show()
    yearsRecyclerView.invalidateTopDividerNow(yearsDividerView)
    weekdayHeaderViews.concealAll()
    dayOfMonthViews.concealAll()
    selectedYearView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorPrimaryInverse))
      typeface = mediumFont
    }
    selectedDateView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorSecondaryInverse))
      typeface = normalFont
    }
    vibrator.vibrateForSelection()
  }

  private fun switchToMonthMode() {
    if (yearsRecyclerView.isConcealed()) return
    yearsRecyclerView.conceal()
    weekdayHeaderViews.showAll()
    dayOfMonthViews.showAll()
    yearsDividerView.hide()
    selectedYearView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorSecondaryInverse))
      typeface = normalFont
    }
    selectedDateView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorPrimaryInverse))
      typeface = mediumFont
    }
    vibrator.vibrateForSelection()
  }

  private fun renderHeaders(
    currentMonth: Calendar,
    selectedDate: DateSnapshot
  ) {
    visibleMonthView.text = dateFormatter.month(currentMonth)
    val selectedCalendar = selectedDate.asCalendar()
    selectedYearView.text = dateFormatter.year(selectedCalendar)
    selectedDateView.text = dateFormatter.date(selectedCalendar)
  }

  private fun renderDaysOfWeek(daysOfWeek: List<DayOfWeek>) {
    weekdayHeaderRenderer.renderAll(
        daysOfWeek = daysOfWeek,
        views = weekdayHeaderViews
    )
  }

  private fun renderDaysOfMonth(days: List<DayOfMonth>) {
    yearAdapter.selectedYear = days.first()
        .month
        .year
    yearAdapter.getSelectedPosition()
        ?.let { yearsRecyclerView.scrollToPosition(it - 2) }

    dayOfMonthRenderer.renderAll(
        daysOfMonth = days,
        views = dayOfMonthViews
    ) {
      controller.setDayOfMonth(it)
    }
  }
}
