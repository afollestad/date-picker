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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.internal.DateSnapshot
import com.afollestad.date.internal.MonthGraph
import com.afollestad.date.internal.Util.createCircularSelector
import com.afollestad.date.internal.YearAdapter
import com.afollestad.date.internal.attachTopDivider
import com.afollestad.date.internal.color
import com.afollestad.date.internal.conceal
import com.afollestad.date.internal.font
import com.afollestad.date.internal.hasVibratePermission
import com.afollestad.date.internal.hide
import com.afollestad.date.internal.invalidateTopDividerNow
import com.afollestad.date.internal.isAfter
import com.afollestad.date.internal.isBefore
import com.afollestad.date.internal.isConcealed
import com.afollestad.date.internal.isVisible
import com.afollestad.date.internal.onClickDebounced
import com.afollestad.date.internal.resolveColor
import com.afollestad.date.internal.show
import com.afollestad.date.internal.snapshot
import com.afollestad.date.internal.vibrator
import com.afollestad.date.internal.withAlpha
import com.afollestad.date.view.WeekRowView
import java.lang.Long.MAX_VALUE
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

typealias OnDateChanged = (date: Calendar) -> Unit

/** @author Aidan Follestad (@afollestad) */
class DatePicker(
  context: Context,
  attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

  // Non-nullables
  private val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
  private val yearFormatter = SimpleDateFormat("yyyy", Locale.getDefault())
  private val dateFormatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
  private val yearAdapter: YearAdapter
  private var didInit: Boolean = false

  // Nullables
  internal var selectedView: View? = null
  private var selectedDate: DateSnapshot? = null

  // Late inits
  private lateinit var monthGraph: MonthGraph
  private lateinit var selectedYearView: TextView
  private lateinit var selectedDateView: TextView
  private lateinit var visibleMonthView: TextView
  private lateinit var goPreviousMonthView: View
  private lateinit var goNextMonthView: View
  private lateinit var weekRowViews: MutableList<WeekRowView>
  private lateinit var yearsRecyclerView: RecyclerView
  private lateinit var yearsDividerView: View

  // Config properties
  private val selectionVibrates: Boolean
  internal val selectionColor: Int
  internal val disabledBackgroundColor: Int
  private val headerBackgroundColor: Int
  internal val normalFont: Typeface
  private val mediumFont: Typeface
  internal var minDate: DateSnapshot? = null
  internal var maxDate: DateSnapshot? = null
  private val dateChangedListeners: MutableList<OnDateChanged> = mutableListOf()

  init {
    inflate(context, R.layout.date_picker, this)
    val ta = context.obtainStyledAttributes(attrs, R.styleable.DatePicker)
    try {
      selectionVibrates =
        ta.getBoolean(R.styleable.DatePicker_date_picker_selection_vibrates, true)
      selectionColor = ta.color(R.styleable.DatePicker_date_picker_selection_color) {
        context.resolveColor(R.attr.colorAccent)
      }
      disabledBackgroundColor =
        ta.color(R.styleable.DatePicker_date_picker_disabled_background_color) {
          context.resolveColor(android.R.attr.textColorSecondary)
              .withAlpha(0.3f)
        }
      headerBackgroundColor = ta.color(R.styleable.DatePicker_date_picker_header_background_color) {
        context.resolveColor(R.attr.colorAccent)
      }
      normalFont = ta.font(context, R.styleable.DatePicker_date_picker_normal_font) {
        Typeface.SANS_SERIF
      }
      mediumFont = ta.font(context, R.styleable.DatePicker_date_picker_medium_font) {
        Typeface.DEFAULT_BOLD
      }
    } finally {
      ta.recycle()
    }

    yearAdapter = YearAdapter(selectionColor, ::onYearSelected)
  }

  /** Sets the month displayed in the view, along with the selected date. */
  fun setDate(calendar: Calendar) {
    this.didInit = true
    this.selectedDate = calendar.snapshot()
    // We clone to send to the callback, since MonthGraph mutates [calendar[
    if (this.dateChangedListeners.isNotEmpty()) {
      val calendarCopy = calendar.clone() as Calendar
      this.dateChangedListeners.forEach { it(calendarCopy) }
    }
    this.monthGraph = MonthGraph(calendar)
    updateAll()
  }

  /** Sets the month and year displayed in the view, along with the selected selectedDate (optionally). */
  fun setDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int? = null,
    month: Int,
    @IntRange(from = 1, to = 31) selectedDate: Int? = null
  ) = setDate(
      Calendar.getInstance().apply {
        if (year != null) {
          this.year = year
        }
        this.month = month
        if (selectedDate != null) {
          this.dayOfMonth = selectedDate
        }
      }
  )

  /** Gets the selected date, if any. */
  fun getDate(): Calendar? = selectedDate?.asCalendar()

  /** Gets the min date, if any. */
  fun getMinDate(): Calendar? = this.minDate?.asCalendar()

  /** Sets a min date. Dates before this are not selectable. */
  fun setMinDate(calendar: Calendar) {
    this.minDate = calendar.snapshot()
    weekRowViews.forEach { it.minDate = minDate }
  }

  /** Sets a min date. Dates before this are not selectable. */
  fun setMinDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int,
    month: Int,
    @IntRange(from = 1, to = 31) selectedDate: Int? = null
  ) {
    this.minDate = DateSnapshot(
        month = month,
        day = selectedDate ?: 1,
        year = year
    )
    weekRowViews.forEach { it.minDate = minDate }
  }

  /** Gets the max date, if any. */
  fun getMaxDate(): Calendar? = this.maxDate?.asCalendar()

  /** Sets a max date. Dates after this are not selectable. */
  fun setMaxDate(calendar: Calendar) {
    this.maxDate = calendar.snapshot()
    weekRowViews.forEach { it.maxDate = maxDate }
  }

  /** Sets a max date. Dates after this are not selectable. */
  fun setMaxDate(
    @IntRange(from = 1, to = MAX_VALUE) year: Int,
    month: Int,
    @IntRange(from = 1, to = 31) selectedDate: Int? = null
  ) {
    this.maxDate = DateSnapshot(
        month = month,
        day = selectedDate ?: 1,
        year = year
    )
    weekRowViews.forEach { it.maxDate = maxDate }
  }

  /** Appends a listener that is invoked when the selected date changes. */
  fun onDateChanged(block: OnDateChanged) {
    dateChangedListeners.add(block)
  }

  /** Removes a listener that is invoked when the selected date changes. */
  fun removeOnDateChanged(block: OnDateChanged): Boolean {
    return dateChangedListeners.remove(block)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    // If we haven't received a date by the time the view attaches to the window,
    // set a default date.
    if (!didInit) {
      setDate(Calendar.getInstance())
    }
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

    weekRowViews = mutableListOf(
        findViewById<WeekRowView>(R.id.row_one).setup(this),
        findViewById<WeekRowView>(R.id.row_two).setup(this),
        findViewById<WeekRowView>(R.id.row_three).setup(this),
        findViewById<WeekRowView>(R.id.row_four).setup(this),
        findViewById<WeekRowView>(R.id.row_five).setup(this),
        findViewById<WeekRowView>(R.id.row_six).setup(this),
        findViewById<WeekRowView>(R.id.row_seven).setup(this)
    )

    yearsDividerView = findViewById(R.id.year_list_divider)
    yearsRecyclerView = findViewById<RecyclerView>(R.id.year_list).apply {
      layoutManager = LinearLayoutManager(context)
      adapter = yearAdapter
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
      attachTopDivider(yearsDividerView)
    }

    goPreviousMonthView = findViewById<View>(R.id.left_chevron).apply {
      background = createCircularSelector(selectionColor)
      onClickDebounced { previousMonth() }
    }
    goNextMonthView = findViewById<View>(R.id.right_chevron).apply {
      background = createCircularSelector(selectionColor)
      onClickDebounced { nextMonth() }
    }
  }

  @Suppress("DEPRECATION")
  @SuppressLint("MissingPermission")
  private fun vibrateForSelection() {
    if (selectionVibrates && context.hasVibratePermission()) {
      context.vibrator()
          .vibrate(VIBRATION_DURATION)
    }
  }

  private fun nextMonth() {
    monthGraph.nextMonth()
    updateAll()
    vibrateForSelection()
  }

  private fun previousMonth() {
    monthGraph.previousMonth()
    updateAll()
    vibrateForSelection()
  }

  private fun switchToYearMode() {
    if (yearsRecyclerView.isVisible()) return
    yearsRecyclerView.show()
    yearsRecyclerView.invalidateTopDividerNow(yearsDividerView)
    weekRowViews.forEach { it.conceal() }
    selectedYearView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorPrimaryInverse))
      typeface = mediumFont
    }
    selectedDateView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorSecondaryInverse))
      typeface = normalFont
    }
    vibrateForSelection()
  }

  private fun switchToMonthMode() {
    if (yearsRecyclerView.isConcealed()) return
    yearsRecyclerView.conceal()
    weekRowViews.forEach { it.show() }
    yearsDividerView.hide()
    selectedYearView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorSecondaryInverse))
      typeface = normalFont
    }
    selectedDateView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorPrimaryInverse))
      typeface = mediumFont
    }
    vibrateForSelection()
  }

  internal fun onDateSelected(date: Int) {
    val calendar = (monthGraph.calendar.clone() as Calendar).apply {
      dayOfMonth = date
    }
    selectedDate = calendar.snapshot()
    vibrateForSelection()
    updateHeaders()
    this.dateChangedListeners.forEach { it(calendar) }
  }

  private fun onYearSelected(year: Int) {
    switchToMonthMode()
    setDate(
        month = selectedDate!!.month,
        year = year,
        selectedDate = selectedDate!!.day
    )
  }

  private fun updateAll() {
    val selectedDateCalendar = selectedDate?.asCalendar() ?: return
    val weeks = monthGraph.getWeeks()
    updateHeaders()

    yearAdapter.selectedYear = selectedDateCalendar.year
    yearAdapter.getSelectedPosition()
        ?.let { yearsRecyclerView.scrollToPosition(it - 2) }

    for ((index, view) in weekRowViews.withIndex()) {
      if (index == 0) {
        view.renderDaysOfWeek(monthGraph.orderedWeekDays)
      } else {
        val week = weeks[index - 1]
        view.renderWeek(week, getValidatedSelectedDate())
      }
    }

    // Disable chevrons if they are out of the min/max date range
    goPreviousMonthView.isEnabled = monthGraph.canGoBack(minDate)
    goNextMonthView.isEnabled = monthGraph.canGoForward(maxDate)
  }

  private fun updateHeaders() {
    visibleMonthView.text = monthFormatter.format(monthGraph.calendar.time)
    val selectedDateCalendar = selectedDate?.asCalendar() ?: return
    selectedYearView.text = yearFormatter.format(selectedDateCalendar.time)
    selectedDateView.text = dateFormatter.format(selectedDateCalendar.time)
  }

  private fun getValidatedSelectedDate(): Int? {
    if (selectedDate.isBefore(minDate) || selectedDate.isAfter(maxDate)) {
      return null
    }
    if (selectedDate?.month != monthGraph.calendar.month) {
      return null
    }
    return selectedDate?.day
  }

  private companion object {
    const val VIBRATION_DURATION: Long = 20L
  }
}
