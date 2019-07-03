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
package com.afollestad.date.layout

import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.View.MeasureSpec
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.core.widget.AutoSizeableTextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.R
import com.afollestad.date.adapters.MonthAdapter
import com.afollestad.date.adapters.MonthItemAdapter
import com.afollestad.date.adapters.YearAdapter
import com.afollestad.date.controllers.VibratorController
import com.afollestad.date.data.DateFormatter
import com.afollestad.date.layout.DatePickerLayoutManager.Mode.CALENDAR
import com.afollestad.date.layout.DatePickerLayoutManager.Mode.MONTH_LIST
import com.afollestad.date.layout.DatePickerLayoutManager.Mode.YEAR_LIST
import com.afollestad.date.layout.DatePickerLayoutManager.Orientation.PORTRAIT
import com.afollestad.date.util.TypefaceHelper
import com.afollestad.date.util.Util.createCircularSelector
import com.afollestad.date.util.attachTopDivider
import com.afollestad.date.util.color
import com.afollestad.date.util.font
import com.afollestad.date.util.invalidateTopDividerNow
import com.afollestad.date.util.onClickDebounced
import com.afollestad.date.util.placeAt
import com.afollestad.date.util.resolveColor
import com.afollestad.date.util.showOrConceal
import com.afollestad.date.util.updatePadding
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

  private var goPreviousMonthView: ImageView = root.findViewById(R.id.left_chevron)
  private var visibleMonthView: TextView = root.findViewById(R.id.current_month)
  private var goNextMonthView: ImageView = root.findViewById(R.id.right_chevron)

  private var listsDividerView: View = root.findViewById(R.id.year_month_list_divider)
  private var daysRecyclerView: RecyclerView = root.findViewById(R.id.day_list)
  private var yearsRecyclerView: RecyclerView = root.findViewById(R.id.year_list)
  private var monthRecyclerView: RecyclerView = root.findViewById(R.id.month_list)

  private val currentMonthTopMargin: Int =
    context.resources.getDimensionPixelSize(R.dimen.current_month_top_margin)
  private val chevronsTopMargin: Int =
    context.resources.getDimensionPixelSize(R.dimen.chevrons_top_margin)
  private val currentMonthHeight: Int =
    context.resources.getDimensionPixelSize(R.dimen.current_month_header_height)
  private val dividerHeight: Int =
    context.resources.getDimensionPixelSize(R.dimen.divider_height)
  private val headersWithFactor: Int =
    context.resources.getInteger(R.integer.headers_width_factor)

  private val dateFormatter = DateFormatter()
  private val size = Size(0, 0)
  private val orientation = Orientation.get(context)

  init {
    setupHeaderViews()
    setupNavigationViews()
    setupListViews()
  }

  @CheckResult fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ): Size {
    val parentWidth: Int = MeasureSpec.getSize(widthMeasureSpec)
    val parentHeight: Int = MeasureSpec.getSize(heightMeasureSpec)

    // First header views
    val headersWidth: Int = (parentWidth / headersWithFactor)
    selectedYearView.measure(
        makeMeasureSpec(headersWidth, EXACTLY),
        makeMeasureSpec(0, UNSPECIFIED)
    )
    selectedDateView.measure(
        makeMeasureSpec(headersWidth, EXACTLY),
        if (parentHeight <= 0 || orientation == PORTRAIT) {
          makeMeasureSpec(0, UNSPECIFIED)
        } else {
          makeMeasureSpec(parentHeight - selectedYearView.measuredHeight, EXACTLY)
        }
    )

    // And the current month
    val nonHeadersWidth: Int = if (orientation == PORTRAIT) {
      parentWidth
    } else {
      parentWidth - headersWidth
    }
    visibleMonthView.measure(
        makeMeasureSpec(nonHeadersWidth, AT_MOST),
        makeMeasureSpec(currentMonthHeight, EXACTLY)
    )

    // Then the divider
    listsDividerView.measure(
        makeMeasureSpec(nonHeadersWidth, EXACTLY),
        makeMeasureSpec(dividerHeight, EXACTLY)
    )

    // Then the calendar recycler view
    val heightSoFar = if (orientation == PORTRAIT) {
      selectedYearView.measuredHeight +
          selectedDateView.measuredHeight +
          visibleMonthView.measuredHeight +
          listsDividerView.measuredHeight
    } else {
      visibleMonthView.measuredHeight +
          listsDividerView.measuredHeight
    }
    val recyclerViewsWidth: Int = (nonHeadersWidth - (calendarHorizontalPadding * 2))
    daysRecyclerView.measure(
        makeMeasureSpec(recyclerViewsWidth, EXACTLY),
        if (parentHeight > 0) {
          makeMeasureSpec(parentHeight - heightSoFar, AT_MOST)
        } else {
          makeMeasureSpec(0, UNSPECIFIED)
        }
    )

    // Then the go back / go forward chevrons
    val chevronWidthAndHeight: Int = (recyclerViewsWidth / DAYS_IN_WEEK)
    goPreviousMonthView.measure(
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY),
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY)
    )
    goNextMonthView.measure(
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY),
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY)
    )

    // Then the year and month recycler views
    yearsRecyclerView.measure(
        makeMeasureSpec(daysRecyclerView.measuredWidth, EXACTLY),
        makeMeasureSpec(daysRecyclerView.measuredHeight, EXACTLY)
    )
    monthRecyclerView.measure(
        makeMeasureSpec(daysRecyclerView.measuredWidth, EXACTLY),
        makeMeasureSpec(daysRecyclerView.measuredHeight, EXACTLY)
    )

    // Calculate a total
    return size.apply {
      width = parentWidth
      // Vertical margins are included here
      height = (heightSoFar +
          daysRecyclerView.measuredHeight +
          chevronsTopMargin +
          currentMonthTopMargin)
    }
  }

  fun onLayout(
    left: Int,
    top: Int,
    right: Int
  ) {
    // First header views
    selectedYearView.placeAt(top = top)
    selectedDateView.placeAt(top = selectedYearView.bottom)

    val nonHeaderLeft = if (orientation == PORTRAIT) {
      left
    } else {
      selectedDateView.right
    }
    val nonHeaderWidth = right - nonHeaderLeft

    // And the current month
    val middleX = (right - (nonHeaderWidth / 2))
    visibleMonthView.placeAt(
        left = (middleX - (visibleMonthView.measuredWidth / 2)),
        top = if (orientation == PORTRAIT) {
          selectedDateView.bottom + currentMonthTopMargin
        } else {
          currentMonthTopMargin
        }
    )

    // Then the divider
    listsDividerView.placeAt(
        top = visibleMonthView.bottom,
        left = nonHeaderLeft
    )

    // Then the calendar recycler view
    daysRecyclerView.placeAt(
        left = (nonHeaderLeft + calendarHorizontalPadding),
        top = listsDividerView.bottom
    )

    // Then the go back / go forward chevrons
    val chevronsMiddleY = (visibleMonthView.bottom - (visibleMonthView.measuredHeight / 2))
    val chevronsTop =
      ((chevronsMiddleY - (goPreviousMonthView.measuredHeight / 2)) + chevronsTopMargin)
    goPreviousMonthView.placeAt(
        left = (daysRecyclerView.left + calendarHorizontalPadding),
        top = chevronsTop
    )
    goNextMonthView.placeAt(
        left = (daysRecyclerView.right -
            goNextMonthView.measuredWidth -
            calendarHorizontalPadding),
        top = chevronsTop
    )

    // Then the year and month recycler views
    yearsRecyclerView.layout(
        /* left   = */daysRecyclerView.left,
        /* top    = */daysRecyclerView.top,
        /* right  = */daysRecyclerView.right,
        /* bottom = */daysRecyclerView.bottom
    )
    monthRecyclerView.layout(
        /* left   = */daysRecyclerView.left,
        /* top    = */daysRecyclerView.top,
        /* right  = */daysRecyclerView.right,
        /* bottom = */daysRecyclerView.bottom
    )
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

  fun onNavigate(
    onGoToPrevious: () -> Unit,
    onGoToNext: () -> Unit
  ) {
    goPreviousMonthView.onClickDebounced { onGoToPrevious() }
    goNextMonthView.onClickDebounced { onGoToNext() }
  }

  private fun setupHeaderViews() {
    selectedYearView.apply {
      background = ColorDrawable(headerBackgroundColor)
      typeface = normalFont
      onClickDebounced { setMode(YEAR_LIST) }
    }
    selectedDateView.apply {
      isSelected = true
      background = ColorDrawable(headerBackgroundColor)
      typeface = mediumFont
      onClickDebounced { setMode(CALENDAR) }
    }
  }

  private fun setupNavigationViews() {
    goPreviousMonthView.background = createCircularSelector(selectionColor)
    visibleMonthView.apply {
      typeface = mediumFont
      onClickDebounced { setMode(MONTH_LIST) }
    }
    goNextMonthView.background = createCircularSelector(selectionColor)
  }

  private fun setupListViews() {
    daysRecyclerView.apply {
      layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.day_grid_span))
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

  fun setMode(mode: Mode) {
    daysRecyclerView.showOrConceal(mode == CALENDAR)
    yearsRecyclerView.showOrConceal(mode == YEAR_LIST)
    monthRecyclerView.showOrConceal(mode == MONTH_LIST)

    when (mode) {
      CALENDAR -> daysRecyclerView.invalidateTopDividerNow(listsDividerView)
      MONTH_LIST -> monthRecyclerView.invalidateTopDividerNow(listsDividerView)
      YEAR_LIST -> yearsRecyclerView.invalidateTopDividerNow(listsDividerView)
    }

    selectedYearView.apply {
      isSelected = mode == YEAR_LIST
      typeface = if (mode == YEAR_LIST) mediumFont else normalFont
    }
    selectedDateView.apply {
      isSelected = mode == CALENDAR
      typeface = if (mode == CALENDAR) mediumFont else normalFont
    }
    vibrator.vibrateForSelection()
  }

  enum class Mode {
    CALENDAR,
    MONTH_LIST,
    YEAR_LIST
  }

  enum class Orientation {
    PORTRAIT,
    LANDSCAPE;

    companion object {
      fun get(context: Context): Orientation {
        return if (context.resources.configuration.orientation == ORIENTATION_PORTRAIT) {
          PORTRAIT
        } else {
          LANDSCAPE
        }
      }
    }
  }

  data class Size(
    var width: Int,
    var height: Int
  )

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

    private const val DAYS_IN_WEEK = 7
  }
}
