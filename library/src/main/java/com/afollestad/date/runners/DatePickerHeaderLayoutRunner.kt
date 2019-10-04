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
import android.graphics.drawable.ColorDrawable
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.View.MeasureSpec.getSize
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat.getPaddingStart
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.R
import com.afollestad.date.data.snapshot.DateSnapshot
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.runners.Mode.INPUT_EDIT
import com.afollestad.date.runners.Mode.YEAR_LIST
import com.afollestad.date.runners.base.Bounds
import com.afollestad.date.runners.base.LayoutRunner
import com.afollestad.date.runners.base.Orientation.PORTRAIT
import com.afollestad.date.runners.base.Size
import com.afollestad.date.runners.calendar.DAYS_IN_WEEK
import com.afollestad.date.util.Util.createCircularSelector
import com.afollestad.date.util.dimenPx
import com.afollestad.date.util.onClickDebounced
import com.afollestad.date.util.placeAt
import com.afollestad.date.util.resolveColor
import com.afollestad.date.util.toggleMode

/** @author Aidan Follestad (@afollestad) */
internal class DatePickerHeaderLayoutRunner(
  context: Context,
  config: DatePickerConfig,
  root: ViewGroup
) : LayoutRunner(context, config) {
  private val pickerTitleView: TextView = root.findViewById(R.id.picker_title)
  private val selectedDateView: TextView = root.findViewById(R.id.current_date)
  private val editModeToggleView: ImageView = root.findViewById(R.id.edit_mode_toggle)
  private val editModeToggleSize: Int = context.dimenPx(R.dimen.edit_mode_toggle_size)

  init {
    setupTitle()
    setupSelectedDate()
    setupEditModeToggle()

    config.currentMode.on { mode ->
      pickerTitleView.isSelected = mode == YEAR_LIST
      selectedDateView.isSelected = mode == CALENDAR
      editModeToggleView.setImageResource(
          if (mode == CALENDAR) R.drawable.ic_edit else R.drawable.ic_calendar
      )
    }
  }

  fun setCurrentDate(selectedDate: DateSnapshot) {
    selectedDate.asCalendar()
        .let { selectedDateView.text = config.dateFormatter.date(it) }
  }

  private fun setupTitle() {
    pickerTitleView.apply {
      background = ColorDrawable(config.headerBackgroundColor)
      typeface = config.normalFont
      text = config.title
    }
  }

  private fun setupSelectedDate() {
    selectedDateView.apply {
      isSelected = true
      background = ColorDrawable(config.headerBackgroundColor)
      typeface = config.normalFont
    }
  }

  private fun setupEditModeToggle() {
    editModeToggleView.apply {
      background = createCircularSelector(
          context, context.resolveColor(android.R.attr.textColorPrimaryInverse)
      )
      onClickDebounced { config.currentMode.toggleMode(INPUT_EDIT) }
    }
  }

  override fun measure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
    totalHeightSoFar: Int
  ): Size {
    val parentWidth: Int = getSize(widthMeasureSpec)
    val parentHeight: Int = getSize(heightMeasureSpec)
    val headersWidth: Int = getHeadersWidth(parentWidth)

    if (!pickerTitleView.text.isNullOrBlank()) {
      pickerTitleView.measure(
          makeMeasureSpec(headersWidth, EXACTLY),
          makeMeasureSpec(0, UNSPECIFIED)
      )
    }
    selectedDateView.measure(
        makeMeasureSpec(headersWidth, EXACTLY),
        if (parentHeight <= 0 || orientation == PORTRAIT) {
          makeMeasureSpec(0, UNSPECIFIED)
        } else {
          makeMeasureSpec(parentHeight - pickerTitleView.measuredHeight, EXACTLY)
        }
    )
    editModeToggleView.measure(
        makeMeasureSpec(editModeToggleSize, EXACTLY),
        makeMeasureSpec(editModeToggleSize, EXACTLY)
    )

    return size.apply {
      width = headersWidth
      height = (pickerTitleView.measuredHeight +
          selectedDateView.measuredHeight)
    }
  }

  override fun layout(
    top: Int,
    left: Int,
    right: Int,
    parentWidth: Int
  ): Bounds {
    // Title and current date
    if (!pickerTitleView.text.isNullOrBlank()) {
      pickerTitleView.placeAt(top = top)
      selectedDateView.placeAt(top = pickerTitleView.bottom)
    } else {
      selectedDateView.placeAt(top = top)
    }

    // Manual input mode toggle
    if (orientation == PORTRAIT) {
      val chevronWidthAndHeight = (getRecyclerViewWidth(parentWidth) / DAYS_IN_WEEK)
      val chevronHalfWidth = (chevronWidthAndHeight / 2) + config.horizontalPadding
      val editToggleHalfWidth = (editModeToggleView.measuredWidth / 2)

      editModeToggleView.placeAt(
          top = selectedDateView.top + (editModeToggleView.measuredHeight / 2),
          left = (right - chevronHalfWidth - editToggleHalfWidth)
      )
    } else {
      // At bottom left of header in landscape
      val editToggleTop = (selectedDateView.bottom -
          (selectedDateView.paddingBottom / 2) -
          editModeToggleView.measuredHeight)
      editModeToggleView.placeAt(
          top = editToggleTop,
          left = getPaddingStart(editModeToggleView)
      )
    }

    return bounds.apply {
      this.top = top
      this.left = left
      this.right = selectedDateView.right
      this.bottom = top + selectedDateView.bottom
    }
  }
}
