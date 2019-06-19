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
package com.afollestad.date.internal

import androidx.annotation.CheckResult
import com.afollestad.date.dayOfMonth
import com.afollestad.date.month
import com.afollestad.date.year
import java.util.Calendar
import java.util.Locale

/** @author Aidan Follestad (@afollestad) */
internal data class DateSnapshot(
  val month: Int,
  val day: Int,
  val year: Int
) {
  fun asCalendar(): Calendar {
    val newMonth = month
    val newDay = day
    val newYear = year
    return Calendar.getInstance(Locale.getDefault())
        .apply {
          this.year = newYear
          this.month = newMonth
          this.dayOfMonth = newDay
        }
  }
}

/** @author Aidan Follestad (@afollestad) */
@CheckResult internal fun DateSnapshot?.isBefore(other: DateSnapshot?): Boolean {
  if (this == null || other == null) return false
  if (year < other.year) return true
  if (year == other.year && month < other.month) return true
  if (year == other.year && month == other.month && day < other.day) return true
  return false
}

/** @author Aidan Follestad (@afollestad) */
@CheckResult internal fun DateSnapshot?.isAfter(other: DateSnapshot?): Boolean {
  if (this == null || other == null) return false
  if (year > other.year) return true
  if (year == other.year && month > other.month) return true
  if (year == other.year && month == other.month && day > other.day) return true
  return false
}

/** @author Aidan Follestad (@afollestad) */
@CheckResult internal fun Calendar.snapshot(): DateSnapshot {
  return DateSnapshot(
      month = this.month,
      day = this.dayOfMonth,
      year = this.year
  )
}
