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
package com.afollestad.date.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.date.DatePicker
import com.afollestad.date.R
import com.afollestad.date.internal.MonthGraph
import com.afollestad.date.internal.Week
import com.afollestad.date.internal.Util.createCircularSelector
import com.afollestad.date.internal.Util.createTextSelector
import com.afollestad.date.internal.onClickDebounced
import com.afollestad.date.internal.resolveColor
import kotlin.properties.Delegates

/** @author Aidan Follestad (@afollestad) */
internal class WeekRowView(
  context: Context,
  attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

  private val datePicker: DatePicker by lazy {
    parent as? DatePicker ?: error("Parent ($parent) should be a DatePicker!")
  }
  private var week: Week? = null
  private var monthGraph: MonthGraph? = null
  private var selectedDate: Int? = null
  private var selectionColor: Int by Delegates.notNull()
  private lateinit var views: MutableList<TextView>

  init {
    inflate(context, R.layout.week_row_view, this)
  }

  fun setState(
    week: Week?,
    graph: MonthGraph,
    selectedDate: Int?
  ) {
    this.week = week
    this.monthGraph = graph
    this.selectedDate = selectedDate
    update()
  }

  fun setColorAndFont(
    @ColorInt selectionColor: Int,
    typeface: Typeface
  ): WeekRowView {
    this.selectionColor = selectionColor
    views.forEach {
      it.background = createCircularSelector(selectionColor)
      it.typeface = typeface
    }
    return this
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    views = mutableListOf(
        findViewById<TextView>(R.id.one).onClickDebounced(::onColumnClicked),
        findViewById<TextView>(R.id.two).onClickDebounced(::onColumnClicked),
        findViewById<TextView>(R.id.three).onClickDebounced(::onColumnClicked),
        findViewById<TextView>(R.id.four).onClickDebounced(::onColumnClicked),
        findViewById<TextView>(R.id.five).onClickDebounced(::onColumnClicked),
        findViewById<TextView>(R.id.six).onClickDebounced(::onColumnClicked),
        findViewById<TextView>(R.id.seven).onClickDebounced(::onColumnClicked)
    )
    update()
  }

  private fun update() {
    if (monthGraph == null) {
      return
    }
    for ((index, textView) in views.withIndex()) {
      textView.text = when {
        week == null -> {
          val dayOfWeek = monthGraph!!.orderedWeekDays[index]
          dayOfWeek.name.first()
              .toString()
        }
        index < week!!.dates.size -> {
          val date = week!!.dates[index]
          textView.isSelected = (selectedDate == date.date)
          if (textView.isSelected) {
            datePicker.selectedView = textView
          }
          date.date.emptyOrPositiveAsString()
        }
        else -> ""
      }
      if (week == null) {
        textView.setTextColor(context.resolveColor(android.R.attr.textColorSecondary))
      } else {
        textView.setTextColor(createTextSelector(context, selectionColor))
      }
      textView.isEnabled = if (week == null) {
        false
      } else {
        textView.text.isNotEmpty()
      }
    }
  }

  private fun onColumnClicked(view: TextView) {
    val value = view.text.toString()
    check(value.isNotEmpty()) { "Clickable views cannot have empty text." }

    datePicker.selectedView?.isSelected = false
    datePicker.onDateSelected(value.toInt())
    view.isSelected = true
    datePicker.selectedView = view
  }

  private fun Int.emptyOrPositiveAsString(): String {
    return if (this < 1) "" else toString()
  }
}
