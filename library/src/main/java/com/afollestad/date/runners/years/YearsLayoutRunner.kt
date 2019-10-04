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
package com.afollestad.date.runners.years

import android.content.Context
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.R
import com.afollestad.date.R.integer
import com.afollestad.date.adapters.YearAdapter
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.runners.Mode.INPUT_EDIT
import com.afollestad.date.runners.Mode.YEAR_LIST
import com.afollestad.date.runners.base.Bounds
import com.afollestad.date.runners.base.LayoutRunner
import com.afollestad.date.runners.base.Size
import com.afollestad.date.util.attachTopDivider
import com.afollestad.date.util.invalidateTopDividerNow
import com.afollestad.date.util.showOrConceal
import com.afollestad.date.util.showOrHide

/** @author Aidan Follestad (@afollestad) */
internal class YearsLayoutRunner(
  context: Context,
  config: DatePickerConfig,
  root: ViewGroup
) : LayoutRunner(context, config) {
  private val calendarRecyclerView: RecyclerView = root.findViewById(R.id.day_list)
  private val yearsRecyclerView: RecyclerView = root.findViewById(R.id.year_grid)
  private val listsDividerView: View = root.findViewById(R.id.year_grid_divider)
  private val gridSpan: Int = context.resources.getInteger(integer.year_grid_span)

  init {
    setupListViews()
    config.currentMode.on { mode ->
      when (mode) {
        CALENDAR -> {
          yearsRecyclerView.showOrConceal(false)
        }
        YEAR_LIST -> {
          yearsRecyclerView.invalidateTopDividerNow(listsDividerView)
          yearsRecyclerView.showOrConceal(true)
        }
        INPUT_EDIT -> {
          yearsRecyclerView.showOrHide(false)
        }
      }
    }
  }

  fun setAdapter(yearAdapter: YearAdapter) {
    yearsRecyclerView.adapter = yearAdapter
  }

  fun scrollToPosition(pos: Int) = yearsRecyclerView.scrollToPosition(pos - 2)

  private fun setupListViews() {
    yearsRecyclerView.apply {
      layoutManager = GridLayoutManager(context, gridSpan)
      attachTopDivider(listsDividerView)
    }
  }

  override fun measure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
    totalHeightSoFar: Int
  ): Size {
    yearsRecyclerView.measure(
        makeMeasureSpec(calendarRecyclerView.measuredWidth, EXACTLY),
        makeMeasureSpec(calendarRecyclerView.measuredHeight, EXACTLY)
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
    yearsRecyclerView.layout(
        calendarRecyclerView.left,
        calendarRecyclerView.top,
        calendarRecyclerView.right,
        calendarRecyclerView.bottom
    )

    return bounds.apply {
      this.top = top
      this.left = left
      this.right = right
      this.bottom = top + yearsRecyclerView.bottom
    }
  }
}
