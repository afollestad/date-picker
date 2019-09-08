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

import com.afollestad.date.data.DayOfWeek.FRIDAY
import com.afollestad.date.data.DayOfWeek.MONDAY
import com.afollestad.date.data.DayOfWeek.SATURDAY
import com.afollestad.date.data.DayOfWeek.SUNDAY
import com.afollestad.date.data.DayOfWeek.THURSDAY
import com.afollestad.date.data.DayOfWeek.TUESDAY
import com.afollestad.date.data.DayOfWeek.WEDNESDAY
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal enum class DayOfWeek(val rawValue: Int) {
  SUNDAY(Calendar.SUNDAY),
  MONDAY(Calendar.MONDAY),
  TUESDAY(Calendar.TUESDAY),
  WEDNESDAY(Calendar.WEDNESDAY),
  THURSDAY(Calendar.THURSDAY),
  FRIDAY(Calendar.FRIDAY),
  SATURDAY(Calendar.SATURDAY)
}

/** @author Aidan Follestad (@afollestad) */
internal fun Int.asDayOfWeek(): DayOfWeek {
  return DayOfWeek.values()
      .single { it.rawValue == this }
}

/** @author Aidan Follestad (@afollestad) */
@Suppress("RemoveRedundantQualifierName")
internal fun DayOfWeek.andTheRest(): List<DayOfWeek> {
  return mutableListOf<DayOfWeek>().apply {
    for (value in rawValue..SATURDAY.rawValue) {
      add(value.asDayOfWeek())
    }
    for (value in SUNDAY.rawValue until rawValue) {
      add(value.asDayOfWeek())
    }
  }
}

/** @author Aidan Follestad (@afollestad) */
internal fun DayOfWeek.nextDayOfWeek(): DayOfWeek {
  return when (this) {
    SUNDAY -> MONDAY
    MONDAY -> TUESDAY
    TUESDAY -> WEDNESDAY
    WEDNESDAY -> THURSDAY
    THURSDAY -> FRIDAY
    FRIDAY -> SATURDAY
    SATURDAY -> SUNDAY
  }
}
