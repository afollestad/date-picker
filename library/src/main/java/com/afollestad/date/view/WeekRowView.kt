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
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.date.DatePicker
import com.afollestad.date.R
import com.afollestad.date.controllers.MinMaxController
import com.afollestad.date.snapshot.DateSnapshot
import com.afollestad.date.internal.DayOfWeek
import com.afollestad.date.internal.NO_DATE
import com.afollestad.date.internal.Week
import com.afollestad.date.internal.Util.createCircularSelector
import com.afollestad.date.internal.Util.createTextSelector
import com.afollestad.date.internal.Util.coloredDrawable
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

  // Specific week mode
  private var week: Week? = null

  // Days of week mode
  private var daysOfWeek: List<DayOfWeek>? = null

  // Config properties
  private var selectionColor: Int by Delegates.notNull()
  private var disabledBackgroundColor: Int by Delegates.notNull()

  private lateinit var minMaxController: MinMaxController
  private lateinit var views: MutableList<TextView>

  init {
    inflate(context, R.layout.week_row_view, this)
  }

  fun renderWeek(week: Week) {
    this.week = week
    render()
  }

  fun renderDaysOfWeek(days: List<DayOfWeek>) {
    this.daysOfWeek = days
    render()
  }

  fun setup(from: DatePicker): WeekRowView {
    this.minMaxController = from.minMaxController
    this.selectionColor = from.selectionColor
    this.disabledBackgroundColor = from.disabledBackgroundColor
    this.views.forEach { it.typeface = from.normalFont }
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
  }

  private fun render() {
    if (week == null) {
      renderDaysOfWeek()
    } else {
      renderSpecificWeek()
    }
  }

  private fun renderSpecificWeek() {
    check(daysOfWeek == null) { "If a week is provided, daysOfWeek should NOT be." }
    val currentWeek = week ?: error("Week must be provided!")

    for ((index, textView) in views.withIndex()) {
      val dayOfMonth = currentWeek.dates[index]
      textView.setTextColor(createTextSelector(context, selectionColor))
      textView.text = dayOfMonth.date.positiveOrEmptyAsString()

      if (dayOfMonth.date == NO_DATE) {
        textView.isEnabled = false
        textView.isSelected = false
        textView.background = null
        continue
      }

      val currentDate = DateSnapshot(
          month = currentWeek.month,
          year = currentWeek.year,
          day = dayOfMonth.date
      )
      textView.isSelected = dayOfMonth.isSelected

      when {
        minMaxController.isOutOfMinRange(currentDate) -> {
          val drawableRes = minMaxController.getOutOfMinRangeBackgroundRes(currentDate)
          textView.background = coloredDrawable(context, drawableRes, disabledBackgroundColor)
          textView.isEnabled = false
        }
        minMaxController.isOutOfMaxRange(currentDate) -> {
          val drawable = minMaxController.getOutOfMaxRangeBackgroundRes(currentDate)
          textView.background = coloredDrawable(context, drawable, disabledBackgroundColor)
          textView.isEnabled = false
        }
        else -> {
          textView.background = createCircularSelector(selectionColor)
          textView.isEnabled = textView.text.toString()
              .isNotEmpty()
        }
      }
    }
  }

  private fun renderDaysOfWeek() {
    check(week == null) { "If daysOfWeek are provided, a week should NOT be." }
    val actualDaysOfWeek = daysOfWeek ?: error("daysOfWeek must be provided.")

    for ((index, textView) in views.withIndex()) {
      textView.setTextColor(context.resolveColor(android.R.attr.textColorSecondary))
      val dayOfWeek = actualDaysOfWeek[index]
      textView.text = dayOfWeek.name.first()
          .toString()
      textView.isEnabled = false
    }
  }

  private fun onColumnClicked(view: TextView) {
    val value = view.text.toString()
    check(value.isNotEmpty()) { "Clickable views cannot have empty text." }
    datePicker.controller.setDayOfMonth(value.toInt())
  }

  private fun Int.positiveOrEmptyAsString(): String {
    return if (this < 1) "" else toString()
  }
}
