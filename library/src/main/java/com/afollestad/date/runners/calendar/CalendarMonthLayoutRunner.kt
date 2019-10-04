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
package com.afollestad.date.runners.calendar

import android.content.Context
import android.view.View
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.View.MeasureSpec.getSize
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.R
import com.afollestad.date.adapters.MonthItemAdapter
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.runners.Mode.INPUT_EDIT
import com.afollestad.date.runners.Mode.YEAR_LIST
import com.afollestad.date.runners.base.Bounds
import com.afollestad.date.runners.base.LayoutRunner
import com.afollestad.date.runners.base.Size
import com.afollestad.date.util.attachTopDivider
import com.afollestad.date.util.invalidateTopDividerNow
import com.afollestad.date.util.placeAt
import com.afollestad.date.util.showOrConceal
import com.afollestad.date.util.showOrHide
import com.afollestad.date.util.updatePadding

internal const val DAYS_IN_WEEK = 7

/** @author Aidan Follestad (@afollestad) */
internal class DatePickerCalendarLayoutRunner(
  context: Context,
  config: DatePickerConfig,
  root: ViewGroup
) : LayoutRunner(context, config) {

  private val calendarRecyclerView: RecyclerView = root.findViewById(R.id.day_list)
  private val listsDividerView: View = root.findViewById(R.id.year_grid_divider)
  private val gridSpan: Int = context.resources.getInteger(R.integer.day_grid_span)

  init {
    setupCalendar()
    config.currentMode.on { mode ->
      when (mode) {
        CALENDAR -> {
          calendarRecyclerView.invalidateTopDividerNow(listsDividerView)
          calendarRecyclerView.showOrConceal(true)
        }
        YEAR_LIST -> {
          calendarRecyclerView.showOrConceal(false)
        }
        INPUT_EDIT -> {
          calendarRecyclerView.showOrHide(false)
        }
      }
    }
  }

  fun setAdapter(monthItemAdapter: MonthItemAdapter) {
    calendarRecyclerView.adapter = monthItemAdapter
  }

  private fun setupCalendar() {
    calendarRecyclerView.apply {
      layoutManager = GridLayoutManager(context, gridSpan)
      attachTopDivider(listsDividerView)
      updatePadding(
          left = config.horizontalPadding,
          right = config.horizontalPadding
      )
    }
  }

  override fun measure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
    totalHeightSoFar: Int
  ): Size {
    val parentWidth: Int = getSize(widthMeasureSpec)
    val parentHeight: Int = getSize(heightMeasureSpec)
    val recyclerViewsWidth: Int = getRecyclerViewWidth(parentWidth)

    calendarRecyclerView.measure(
        makeMeasureSpec(recyclerViewsWidth, EXACTLY),
        if (parentHeight > 0) {
          makeMeasureSpec(parentHeight - totalHeightSoFar, AT_MOST)
        } else {
          makeMeasureSpec(0, UNSPECIFIED)
        }
    )

    return size.apply {
      width = calendarRecyclerView.measuredWidth
      height = calendarRecyclerView.measuredHeight
    }
  }

  override fun layout(
    top: Int,
    left: Int,
    right: Int,
    parentWidth: Int
  ): Bounds {
    val daysRecyclerViewLeft = (left + config.horizontalPadding)
    calendarRecyclerView.placeAt(
        left = daysRecyclerViewLeft,
        top = listsDividerView.bottom
    )

    return bounds.apply {
      this.top = top
      this.left = left
      this.right = right
      this.bottom = top + calendarRecyclerView.bottom
    }
  }
}
