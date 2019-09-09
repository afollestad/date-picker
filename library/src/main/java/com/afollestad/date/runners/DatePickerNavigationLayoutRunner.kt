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

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.getSize
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.date.R
import com.afollestad.date.data.DateFormatter
import com.afollestad.date.data.snapshot.MonthSnapshot
import com.afollestad.date.data.snapshot.asCalendar
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.runners.Mode.INPUT_EDIT
import com.afollestad.date.runners.Mode.YEAR_LIST
import com.afollestad.date.runners.base.Bounds
import com.afollestad.date.runners.base.LayoutRunner
import com.afollestad.date.runners.base.Size
import com.afollestad.date.util.TypefaceHelper
import com.afollestad.date.util.Util.createCircularSelector
import com.afollestad.date.util.color
import com.afollestad.date.util.dimenPx
import com.afollestad.date.util.drawable
import com.afollestad.date.util.font
import com.afollestad.date.util.onClickDebounced
import com.afollestad.date.util.placeAt
import com.afollestad.date.util.resolveColor
import com.afollestad.date.util.setCompoundDrawablesCompat
import com.afollestad.date.util.showOrHide
import kotlin.math.max

/** @author Aidan Follestad (@afollestad) */
internal class DatePickerNavigationLayoutRunner(
  private val context: Context,
  root: ViewGroup,
  typedArray: TypedArray,
  private val dateFormatter: DateFormatter,
  private val parentRunner: DatePickerLayoutRunner
) : LayoutRunner(context, typedArray) {
  private val goPreviousMonthView: ImageView = root.findViewById(R.id.left_chevron)
  private val visibleMonthView: TextView = root.findViewById(R.id.current_month)
  private val goNextMonthView: ImageView = root.findViewById(R.id.right_chevron)
  private val listsDividerView: View = root.findViewById(R.id.year_month_list_divider)

  private val currentMonthHeight: Int =
    context.dimenPx(R.dimen.current_month_header_height)
  private val currentMonthTopMargin: Int =
    context.dimenPx(R.dimen.current_month_top_margin)
  private val dividerHeight: Int =
    context.dimenPx(R.dimen.divider_height)

  private val selectionColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_selection_color) {
      context.resolveColor(R.attr.colorAccent)
    }
  private val mediumFont: Typeface =
    typedArray.font(context, R.styleable.DatePicker_date_picker_medium_font) {
      TypefaceHelper.create("sans-serif-medium")
    }

  init {
    setupVisibleMonth()
    setupChevrons()
  }

  fun setCurrentDate(currentMonth: MonthSnapshot) {
    visibleMonthView.text = dateFormatter.monthAndYear(currentMonth.asCalendar(1))
  }

  fun onNavigate(
    onGoToPrevious: () -> Unit,
    onGoToNext: () -> Unit
  ) {
    goPreviousMonthView.onClickDebounced { onGoToPrevious() }
    goNextMonthView.onClickDebounced { onGoToNext() }
  }

  private fun invalidateCurrentMonthChevron() {
    val expanded = lastMode == YEAR_LIST
    val currentMonthChevronDrawable = context.drawable(
        if (expanded) {
          R.drawable.ic_chevron_up
        } else {
          R.drawable.ic_chevron_down
        },
        context.resolveColor(android.R.attr.textColorSecondary)
    )
    visibleMonthView.setCompoundDrawablesCompat(end = currentMonthChevronDrawable)
  }

  private fun setupVisibleMonth() {
    visibleMonthView.apply {
      typeface = mediumFont
      onClickDebounced { parentRunner.toggleMode() }
    }
  }

  private fun setupChevrons() {
    goPreviousMonthView.background = createCircularSelector(context, selectionColor)
    invalidateCurrentMonthChevron()
    goNextMonthView.background = createCircularSelector(context, selectionColor)
  }

  override fun measure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
    totalHeightSoFar: Int
  ): Size {
    val parentWidth: Int = getSize(widthMeasureSpec)
    val nonHeadersWidth = getNonHeadersWidth(parentWidth)
    val chevronWidthAndHeight = (getRecyclerViewWidth(parentWidth) / DAYS_IN_WEEK)

    // Calendar/year toggle
    visibleMonthView.measure(
        makeMeasureSpec(nonHeadersWidth, AT_MOST),
        makeMeasureSpec(currentMonthHeight, EXACTLY)
    )

    // Then the go back/go forward chevrons
    goPreviousMonthView.measure(
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY),
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY)
    )
    goNextMonthView.measure(
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY),
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY)
    )

    // Divider
    listsDividerView.measure(
        makeMeasureSpec(nonHeadersWidth, EXACTLY),
        makeMeasureSpec(dividerHeight, EXACTLY)
    )

    val totalHeight = max(visibleMonthView.measuredHeight, chevronWidthAndHeight) +
        listsDividerView.measuredHeight +
        currentMonthTopMargin
    return size.apply {
      width = parentWidth
      height = totalHeight
    }
  }

  override fun layout(
    top: Int,
    left: Int,
    right: Int,
    parentWidth: Int
  ): Bounds {
    // Current month
    visibleMonthView.placeAt(
        top = top + currentMonthTopMargin,
        left = left
    )
    // Divider
    listsDividerView.placeAt(
        top = visibleMonthView.bottom,
        left = left
    )

    // Then the go back / go forward chevrons
    val visibleMonthHalfHeight = (visibleMonthView.measuredHeight / 2)
    val chevronsMiddleY = (visibleMonthView.bottom - visibleMonthHalfHeight)
    val chevronsHalfHeight = (goPreviousMonthView.measuredHeight / 2)
    val chevronsTop = ((chevronsMiddleY - chevronsHalfHeight) + currentMonthTopMargin)
    val nextChevronLeft = (right -
        (calendarHorizontalPadding * 2) -
        goNextMonthView.measuredWidth)

    goPreviousMonthView.placeAt(
        left = (nextChevronLeft - goPreviousMonthView.measuredWidth),
        top = chevronsTop
    )
    goNextMonthView.placeAt(
        left = nextChevronLeft,
        top = chevronsTop
    )

    return bounds.apply {
      this.top = top
      this.left = left
      this.right = right
      this.bottom = top + visibleMonthView.bottom
    }
  }

  override fun setMode(mode: Mode) {
    when (mode) {
      CALENDAR -> {
        visibleMonthView.showOrHide(true)
        goPreviousMonthView.showOrHide(true)
        goNextMonthView.showOrHide(true)
      }
      YEAR_LIST -> {
        visibleMonthView.showOrHide(true)
        goPreviousMonthView.showOrHide(true)
        goNextMonthView.showOrHide(true)
      }
      INPUT_EDIT -> {
        visibleMonthView.showOrHide(false)
        goPreviousMonthView.showOrHide(false)
        goNextMonthView.showOrHide(false)
      }
    }
    super.setMode(mode)
  }
}
