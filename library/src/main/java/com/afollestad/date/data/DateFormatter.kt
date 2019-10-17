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
package com.afollestad.date.data

import androidx.annotation.CheckResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** @author Aidan Follestad (@afollestad) */
internal class DateFormatter {
  val dateInputFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
  private val monthAndYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
  private val yearFormatter = SimpleDateFormat("yyyy", Locale.getDefault())
  private val dateFormatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
  private val monthFormatter = SimpleDateFormat("MMMM", Locale.getDefault())
  private val weekdayFormatter = SimpleDateFormat("E", Locale.getDefault())

  /** July 1995 */
  @CheckResult fun monthAndYear(calendar: Calendar): String =
    monthAndYearFormatter.format(calendar.time)

  /** 1995 */
  @CheckResult fun year(calendar: Calendar): String =
    yearFormatter.format(calendar.time)

  /** Fri, Jul 28 */
  @CheckResult fun date(calendar: Calendar): String =
    dateFormatter.format(calendar.time)

  /** 07/28/1995 */
  @CheckResult fun inputDate(calendar: Calendar): String =
    dateInputFormatter.format(calendar.time)

  /** July */
  @CheckResult fun month(calendar: Calendar): String =
    monthFormatter.format(calendar.time)

  /** F */
  @CheckResult fun weekdayAbbreviation(calendar: Calendar): String =
    weekdayFormatter.format(calendar.time).first().toUpperCase().toString()
}
