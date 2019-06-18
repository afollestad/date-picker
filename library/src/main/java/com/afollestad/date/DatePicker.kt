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
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.internal.DateSnapshot
import com.afollestad.date.internal.MonthGraph
import com.afollestad.date.internal.Util.createCircularSelector
import com.afollestad.date.internal.YearAdapter
import com.afollestad.date.internal.attachTopDivider
import com.afollestad.date.internal.conceal
import com.afollestad.date.internal.dayOfMonth
import com.afollestad.date.internal.hasVibratePermission
import com.afollestad.date.internal.hide
import com.afollestad.date.internal.invalidateTopDividerNow
import com.afollestad.date.internal.isConcealed
import com.afollestad.date.internal.isVisible
import com.afollestad.date.internal.month
import com.afollestad.date.internal.onClickDebounced
import com.afollestad.date.internal.resolveColor
import com.afollestad.date.internal.show
import com.afollestad.date.internal.snapshot
import com.afollestad.date.internal.vibrator
import com.afollestad.date.internal.year
import com.afollestad.date.view.WeekRowView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
  private lateinit var weekRowViews: MutableList<WeekRowView>
  private lateinit var yearsRecyclerView: RecyclerView
  private lateinit var yearsDividerView: View

  // Config properties
  private val selectionVibrates: Boolean
  private val selectionColor: Int
  private val headerBackgroundColor: Int

  init {
    inflate(context, R.layout.date_picker, this)
    val ta = context.obtainStyledAttributes(attrs, R.styleable.DatePicker)
    try {
      val accentColor = context.resolveColor(R.attr.colorAccent)
      selectionVibrates =
        ta.getBoolean(R.styleable.DatePicker_date_picker_selection_vibrates, true)
      selectionColor = ta.getColor(R.styleable.DatePicker_date_picker_selection_color, accentColor)
      headerBackgroundColor =
        ta.getColor(R.styleable.DatePicker_date_picker_header_background_color, accentColor)
    } finally {
      ta.recycle()
    }

    yearAdapter = YearAdapter(selectionColor, ::onYearSelected)
  }

  /** Sets the month displayed in the view, along with the selected date. */
  fun setDate(calendar: Calendar) {
    this.didInit = true
    this.selectedDate = calendar.snapshot()
    this.monthGraph = MonthGraph(calendar)
    updateAll()
  }

  /** Sets the month and year displayed in the view, along with the selected selectedDate (optionally). */
  fun setDate(
    year: Int? = null,
    month: Int,
    selectedDate: Int? = null
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

  internal fun onSelectedDate(dateSnapshot: DateSnapshot) {
    selectedDate = dateSnapshot
    vibrateForSelection()
    updateHeaders()
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
    visibleMonthView = findViewById(R.id.current_month)
    selectedYearView = findViewById<TextView>(R.id.current_year).apply {
      background = ColorDrawable(headerBackgroundColor)
      onClickDebounced { switchToYearMode() }
    }
    selectedDateView = findViewById<TextView>(R.id.current_date).apply {
      background = ColorDrawable(headerBackgroundColor)
      onClickDebounced { switchToMonthMode() }
    }

    weekRowViews = mutableListOf(
        findViewById<WeekRowView>(R.id.row_one).setSelectionColor(selectionColor),
        findViewById<WeekRowView>(R.id.row_two).setSelectionColor(selectionColor),
        findViewById<WeekRowView>(R.id.row_three).setSelectionColor(selectionColor),
        findViewById<WeekRowView>(R.id.row_four).setSelectionColor(selectionColor),
        findViewById<WeekRowView>(R.id.row_five).setSelectionColor(selectionColor),
        findViewById<WeekRowView>(R.id.row_six).setSelectionColor(selectionColor),
        findViewById<WeekRowView>(R.id.row_seven).setSelectionColor(selectionColor)
    )
    yearsDividerView = findViewById(R.id.year_list_divider)
    yearsRecyclerView = findViewById<RecyclerView>(R.id.year_list).apply {
      layoutManager = LinearLayoutManager(context)
      adapter = yearAdapter
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
      attachTopDivider(yearsDividerView)
    }

    findViewById<View>(R.id.left_chevron).apply {
      background = createCircularSelector(selectionColor)
      onClickDebounced { previousMonth() }
    }
    findViewById<View>(R.id.right_chevron).apply {
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
    selectedYearView.setTextColor(context.resolveColor(android.R.attr.textColorPrimaryInverse))
    selectedDateView.setTextColor(context.resolveColor(android.R.attr.textColorSecondaryInverse))
    vibrateForSelection()
  }

  private fun switchToMonthMode() {
    if (yearsRecyclerView.isConcealed()) return
    yearsRecyclerView.conceal()
    weekRowViews.forEach { it.show() }
    yearsDividerView.hide()
    selectedYearView.setTextColor(context.resolveColor(android.R.attr.textColorSecondaryInverse))
    selectedDateView.setTextColor(context.resolveColor(android.R.attr.textColorPrimaryInverse))
    vibrateForSelection()
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
        view.setState(null, monthGraph, null)
      } else {
        val week = weeks[index - 1]
        view.setState(week, monthGraph, getSelectedDate())
      }
    }
  }

  private fun updateHeaders() {
    visibleMonthView.text = monthFormatter.format(monthGraph.calendar.time)
    val selectedDateCalendar = selectedDate?.asCalendar() ?: return
    selectedYearView.text = yearFormatter.format(selectedDateCalendar.time)
    selectedDateView.text = dateFormatter.format(selectedDateCalendar.time)
  }

  private fun getSelectedDate(): Int? {
    if (selectedDate?.month != monthGraph.currentMonth()) {
      return null
    }
    return selectedDate?.day
  }

  private companion object {
    const val VIBRATION_DURATION: Long = 20L
  }
}
