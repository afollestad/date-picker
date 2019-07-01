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
package com.afollestad.date.managers

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.R
import com.afollestad.date.R.integer
import com.afollestad.date.adapters.MonthAdapter
import com.afollestad.date.adapters.MonthItemAdapter
import com.afollestad.date.adapters.YearAdapter
import com.afollestad.date.controllers.VibratorController
import com.afollestad.date.data.DateFormatter
import com.afollestad.date.util.TypefaceHelper
import com.afollestad.date.util.Util.createCircularSelector
import com.afollestad.date.util.attachTopDivider
import com.afollestad.date.util.color
import com.afollestad.date.util.conceal
import com.afollestad.date.util.font
import com.afollestad.date.util.hide
import com.afollestad.date.util.invalidateTopDividerNow
import com.afollestad.date.util.isConcealed
import com.afollestad.date.util.isVisible
import com.afollestad.date.util.onClickDebounced
import com.afollestad.date.util.resolveColor
import com.afollestad.date.util.show
import com.afollestad.date.util.showOrConceal
import com.afollestad.date.util.updateMargin
import com.afollestad.date.util.updatePadding
import com.afollestad.date.view.ChevronImageView
import java.util.Calendar

// TODO write unit tests
/** @author Aidan Follestad (@afollestad) */
internal class DatePickerLayoutManager(
  context: Context,
  typedArray: TypedArray,
  root: ViewGroup,
  private val vibrator: VibratorController
) {
  val selectionColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_selection_color) {
      context.resolveColor(R.attr.colorAccent)
    }
  private val headerBackgroundColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_header_background_color) {
      context.resolveColor(R.attr.colorAccent)
    }
  private val normalFont: Typeface =
    typedArray.font(context, R.styleable.DatePicker_date_picker_normal_font) {
      TypefaceHelper.create("sans-serif")
    }
  private val mediumFont: Typeface =
    typedArray.font(context, R.styleable.DatePicker_date_picker_medium_font) {
      TypefaceHelper.create("sans-serif-medium")
    }
  private val calendarHorizontalPadding: Int =
    typedArray.getDimensionPixelSize(
        R.styleable.DatePicker_date_picker_calendar_horizontal_padding, 0
    )

  private var selectedYearView: TextView = root.findViewById(R.id.current_year)
  private var selectedDateView: TextView = root.findViewById(R.id.current_date)

  private var goPreviousMonthView: ChevronImageView = root.findViewById(R.id.left_chevron)
  private var visibleMonthView: TextView = root.findViewById(R.id.current_month)
  private var goNextMonthView: ChevronImageView = root.findViewById(R.id.right_chevron)

  private var listsDividerView: View = root.findViewById(R.id.year_month_list_divider)
  private var daysRecyclerView: RecyclerView = root.findViewById(R.id.day_list)
  private var yearsRecyclerView: RecyclerView = root.findViewById(R.id.year_list)
  private var monthRecyclerView: RecyclerView = root.findViewById(R.id.month_list)

  private val dateFormatter = DateFormatter()

  fun onFinishInflate(
    onGoToPrevious: () -> Unit,
    onGoToNext: () -> Unit
  ) {
    setupHeaderViews()
    setupNavigationViews(onGoToPrevious, onGoToNext)
    setupListViews()
  }

  fun setAdapters(
    monthItemAdapter: MonthItemAdapter,
    yearAdapter: YearAdapter,
    monthAdapter: MonthAdapter
  ) {
    daysRecyclerView.adapter = monthItemAdapter
    yearsRecyclerView.adapter = yearAdapter
    monthRecyclerView.adapter = monthAdapter
  }

  fun showOrHideGoPrevious(show: Boolean) = goPreviousMonthView.showOrConceal(show)

  fun showOrHideGoNext(show: Boolean) = goNextMonthView.showOrConceal(show)

  fun setHeadersContent(
    currentMonth: Calendar,
    selectedDate: Calendar
  ) {
    visibleMonthView.text = dateFormatter.monthAndYear(currentMonth)
    selectedYearView.text = dateFormatter.year(selectedDate)
    selectedDateView.text = dateFormatter.date(selectedDate)
  }

  fun scrollToYearPosition(pos: Int) = yearsRecyclerView.scrollToPosition(pos - 2)

  fun scrollToMonthPosition(pos: Int) = monthRecyclerView.scrollToPosition(pos - 2)

  private fun setupHeaderViews() {
    selectedYearView.apply {
      background = ColorDrawable(headerBackgroundColor)
      typeface = normalFont
      onClickDebounced { switchToYearMode() }
    }
    selectedDateView.apply {
      isSelected = true
      background = ColorDrawable(headerBackgroundColor)
      typeface = mediumFont
      onClickDebounced { switchToDaysOfMonthMode() }
    }
  }

  private fun setupNavigationViews(
    onGoToPrevious: () -> Unit,
    onGoToNext: () -> Unit
  ) {
    goPreviousMonthView.apply {
      background = createCircularSelector(selectionColor)
      onClickDebounced { onGoToPrevious() }
      attach(daysRecyclerView)
      updateMargin(
          left = calendarHorizontalPadding,
          right = calendarHorizontalPadding
      )
    }
    visibleMonthView.apply {
      typeface = mediumFont
      onClickDebounced { switchToMonthMode() }
    }
    goNextMonthView.apply {
      background = createCircularSelector(selectionColor)
      onClickDebounced { onGoToNext() }
      attach(daysRecyclerView)
      updateMargin(
          left = calendarHorizontalPadding,
          right = calendarHorizontalPadding
      )
    }
  }

  private fun setupListViews() {
    daysRecyclerView.apply {
      layoutManager = GridLayoutManager(context, resources.getInteger(integer.day_grid_span))
      attachTopDivider(listsDividerView)
      updatePadding(
          left = calendarHorizontalPadding,
          right = calendarHorizontalPadding
      )
    }
    yearsRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
      attachTopDivider(listsDividerView)
    }
    monthRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
      attachTopDivider(listsDividerView)
    }
  }

  fun switchToDaysOfMonthMode() {
    if (yearsRecyclerView.isConcealed() && monthRecyclerView.isConcealed()) {
      return
    }
    yearsRecyclerView.conceal()
    monthRecyclerView.conceal()
    daysRecyclerView.show()
    listsDividerView.hide()

    selectedYearView.apply {
      isSelected = false
      typeface = normalFont
    }
    selectedDateView.apply {
      isSelected = true
      typeface = mediumFont
    }
    vibrator.vibrateForSelection()
  }

  private fun switchToMonthMode() {
    if (monthRecyclerView.isVisible()) return
    monthRecyclerView.show()
    monthRecyclerView.invalidateTopDividerNow(listsDividerView)
    yearsRecyclerView.conceal()
    daysRecyclerView.conceal()

    selectedYearView.apply {
      isSelected = false
      typeface = mediumFont
    }
    selectedDateView.apply {
      isSelected = false
      typeface = normalFont
    }
    vibrator.vibrateForSelection()
  }

  private fun switchToYearMode() {
    if (yearsRecyclerView.isVisible()) return
    yearsRecyclerView.show()
    yearsRecyclerView.invalidateTopDividerNow(listsDividerView)
    monthRecyclerView.conceal()
    daysRecyclerView.conceal()

    selectedYearView.apply {
      isSelected = true
      typeface = mediumFont
    }
    selectedDateView.apply {
      isSelected = false
      typeface = normalFont
    }
    vibrator.vibrateForSelection()
  }

  companion object {
    @CheckResult fun inflateInto(
      context: Context,
      typedArray: TypedArray,
      container: ViewGroup
    ): DatePickerLayoutManager {
      View.inflate(context, R.layout.date_picker, container)
      val vibrator = VibratorController(context, typedArray)
      return DatePickerLayoutManager(context, typedArray, container, vibrator)
    }
  }
}
