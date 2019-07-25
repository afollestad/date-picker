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
package com.afollestad.date.controllers

import androidx.annotation.CheckResult
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import com.afollestad.date.R
import com.afollestad.date.dayOfMonth
import com.afollestad.date.decrementMonth
import com.afollestad.date.incrementMonth
import com.afollestad.date.data.snapshot.DateSnapshot
import com.afollestad.date.data.snapshot.MonthSnapshot
import com.afollestad.date.data.snapshot.asCalendar
import com.afollestad.date.data.snapshot.snapshot
import com.afollestad.date.totalDaysInMonth
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal class MinMaxController {
  private var minDate: DateSnapshot? = null
  private var maxDate: DateSnapshot? = null

  @CheckResult fun getMinDate(): Calendar? = minDate?.asCalendar()

  fun setMinDate(date: Calendar) {
    this.minDate = date.snapshot()
    validateMinAndMax()
  }

  fun setMinDate(
    @IntRange(from = 1, to = Long.MAX_VALUE) year: Int,
    month: Int,
    @IntRange(from = 1, to = 31) dayOfMonth: Int
  ) {
    this.minDate = DateSnapshot(month = month, year = year, day = dayOfMonth)
    validateMinAndMax()
  }

  @CheckResult fun getMaxDate(): Calendar? = maxDate?.asCalendar()

  fun setMaxDate(date: Calendar) {
    this.maxDate = date.snapshot()
    validateMinAndMax()
  }

  fun setMaxDate(
    @IntRange(from = 1, to = Long.MAX_VALUE) year: Int,
    month: Int,
    @IntRange(from = 1, to = 31) dayOfMonth: Int
  ) {
    this.maxDate = DateSnapshot(month = month, year = year, day = dayOfMonth)
    validateMinAndMax()
  }

  @CheckResult fun canGoBack(from: MonthSnapshot): Boolean {
    if (minDate == null) return true
    val lastMonth = from.asCalendar(1).decrementMonth()
        .snapshot()
    return !isOutOfMinRange(lastMonth)
  }

  @CheckResult fun canGoForward(from: MonthSnapshot): Boolean {
    if (maxDate == null) return true
    val nextMonth = from.asCalendar(1).incrementMonth()
        .snapshot()
    return !isOutOfMaxRange(nextMonth)
  }

  @CheckResult fun isOutOfMinRange(date: DateSnapshot?): Boolean {
    if (date == null || minDate == null) return false
    return date < minDate!!
  }

  @DrawableRes @CheckResult
  fun getOutOfMinRangeBackgroundRes(date: DateSnapshot): Int {
    val calendar = date.asCalendar()
    val isLastInMonth = calendar.dayOfMonth == calendar.totalDaysInMonth
    return when {
      isLastInMonth -> R.drawable.ic_tube_end
      date.day == 1 -> R.drawable.ic_tube_start
      date.day == minDate!!.day - 1 &&
          date.month == minDate!!.month &&
          date.year == minDate!!.year -> R.drawable.ic_tube_end
      else -> R.drawable.ic_tube_middle
    }
  }

  @CheckResult fun isOutOfMaxRange(date: DateSnapshot?): Boolean {
    if (date == null || maxDate == null) return false
    return date > maxDate!!
  }

  @DrawableRes @CheckResult
  fun getOutOfMaxRangeBackgroundRes(date: DateSnapshot): Int {
    val calendar = date.asCalendar()
    val isLastInMonth = calendar.dayOfMonth == calendar.totalDaysInMonth
    return when {
      date.day == 1 -> R.drawable.ic_tube_start
      date.day == maxDate!!.day + 1 &&
          date.month == maxDate!!.month &&
          date.year == maxDate!!.year -> R.drawable.ic_tube_start
      isLastInMonth -> R.drawable.ic_tube_end
      else -> R.drawable.ic_tube_middle
    }
  }

  private fun validateMinAndMax() {
    if (minDate != null && maxDate != null) {
      check(minDate!! < maxDate!!) {
        "Min date must be less than max date."
      }
    }
  }
}
