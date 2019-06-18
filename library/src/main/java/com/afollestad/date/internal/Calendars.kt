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

import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal var Calendar.year: Int
  get() = get(Calendar.YEAR)
  set(value) {
    set(Calendar.YEAR, value)
  }

/** @author Aidan Follestad (@afollestad) */
internal var Calendar.month: Int
  get() = get(Calendar.MONTH)
  set(value) {
    set(Calendar.MONTH, value)
  }

/** @author Aidan Follestad (@afollestad) */
internal var Calendar.dayOfMonth: Int
  get() = get(Calendar.DAY_OF_MONTH)
  set(value) {
    set(Calendar.DAY_OF_MONTH, value)
  }

/** @author Aidan Follestad (@afollestad) */
internal val Calendar.dayOfWeek: DayOfWeek
  get() = get(Calendar.DAY_OF_WEEK).asDayOfWeek()

/** @author Aidan Follestad (@afollestad) */
internal val Calendar.totalDaysInMonth: Int
  get() = getActualMaximum(Calendar.DAY_OF_MONTH)

/** @author Aidan Follestad (@afollestad) */
internal fun Calendar.incrementMonth() {
  add(Calendar.MONTH, 1)
  set(Calendar.DAY_OF_MONTH, 1)
}

/** @author Aidan Follestad (@afollestad) */
internal fun Calendar.decrementMonth() {
  add(Calendar.MONTH, -1)
  set(Calendar.DAY_OF_MONTH, 1)
}
