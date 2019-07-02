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
import androidx.annotation.VisibleForTesting
import com.afollestad.date.data.SelectionMode.SINGLE
import com.afollestad.date.data.snapshot.DateSnapshot
import org.jetbrains.annotations.TestOnly
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
enum class SelectionMode {
  SINGLE,
  RANGE
}

/** @author Aidan Follestad (@afollestad) */
internal class SelectedDate {
  var mode: SelectionMode = SINGLE
  var current: Int = CURRENT_IS_LOW
    set(value) {
      check(mode != SINGLE) { "Only use range functions in RANGE selection mode." }
      require(current == CURRENT_IS_LOW || current == CURRENT_IS_HIGH) {
        "Invalid 'current' value: $current"
      }
      field = value
    }

  @VisibleForTesting var lowSnapshot: DateSnapshot? = null
  private var lowCalendar: Calendar? = null
  @VisibleForTesting var highSnapshot: DateSnapshot? = null
  private var highCalendar: Calendar? = null

  fun set(snapshot: DateSnapshot?) {
    if (mode == SINGLE) {
      lowSnapshot = snapshot?.also { lowCalendar = it.asCalendar() }
      return
    }
    when (current) {
      CURRENT_IS_LOW -> {
        this.lowSnapshot = snapshot?.also { lowCalendar = it.asCalendar() }
        this.current = CURRENT_IS_HIGH
      }
      CURRENT_IS_HIGH -> {
        this.highSnapshot = snapshot?.also { highCalendar = it.asCalendar() }
        this.current = CURRENT_IS_LOW
      }
      else -> error("Invalid current value: $current")
    }
  }

  @CheckResult fun get(): DateSnapshot? {
    if (mode == SINGLE) {
      return lowSnapshot
    }
    return when (current) {
      CURRENT_IS_LOW -> lowSnapshot
      CURRENT_IS_HIGH -> highSnapshot
      else -> error("Invalid current value: $current")
    }
  }

  @CheckResult fun getCalendar(): Calendar? {
    if (mode == SINGLE) {
      return lowCalendar
    }
    return when (current) {
      CURRENT_IS_LOW -> lowCalendar
      CURRENT_IS_HIGH -> highCalendar
      else -> error("Invalid current value: $current")
    }
  }

  @CheckResult fun getRange(): Pair<DateSnapshot, DateSnapshot>? {
    if (mode == SINGLE) {
      return null
    }
    return Pair(
        lowSnapshot ?: return null,
        highSnapshot ?: return null
    )
  }

  @TestOnly fun clear() {
    lowSnapshot = null
    lowCalendar = null
    highSnapshot = null
    highCalendar = null
  }

  override fun equals(other: Any?): Boolean {
    val otherDate = other as? SelectedDate ?: return false
    return mode == otherDate.mode &&
        lowSnapshot == otherDate.lowSnapshot &&
        highSnapshot == otherDate.highSnapshot &&
        current == otherDate.current
  }

  override fun hashCode(): Int {
    var result = mode.hashCode()
    result = 31 * result + (lowSnapshot?.hashCode() ?: 0)
    result = 31 * result + (highSnapshot?.hashCode() ?: 0)
    result = 31 * result + current
    return result
  }

  companion object {
    const val CURRENT_IS_LOW: Int = 1
    const val CURRENT_IS_HIGH: Int = 2
  }
}
