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
import androidx.annotation.IntRange
import androidx.annotation.VisibleForTesting
import com.afollestad.date.OnDateChanged
import com.afollestad.date.dayOfMonth
import com.afollestad.date.decrementMonth
import com.afollestad.date.incrementMonth
import com.afollestad.date.internal.DayOfMonth
import com.afollestad.date.internal.DayOfWeek
import com.afollestad.date.internal.MonthGraph
import com.afollestad.date.month
import com.afollestad.date.snapshot.DateSnapshot
import com.afollestad.date.snapshot.MonthSnapshot
import com.afollestad.date.snapshot.asCalendar
import com.afollestad.date.snapshot.snapshot
import com.afollestad.date.snapshot.snapshotMonth
import com.afollestad.date.year
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal class DatePickerController(
  private val vibrator: VibratorController,
  private val minMaxController: MinMaxController,
  private val renderHeaders: (Calendar, Calendar) -> Unit,
  private val renderDaysOfWeek: (List<DayOfWeek>) -> Unit,
  private val renderDaysOfMonth: (List<DayOfMonth>) -> Unit,
  private val goBackVisibility: (visible: Boolean) -> Unit,
  private val goForwardVisibility: (visible: Boolean) -> Unit,
  private val switchToDaysOfMonthMode: () -> Unit,
  private val getNow: () -> Calendar = { Calendar.getInstance() }
) {
  @VisibleForTesting var didInit: Boolean = false
  private val dateChangedListeners: MutableList<OnDateChanged> = mutableListOf()

  @VisibleForTesting var viewingMonth: MonthSnapshot? = null
  @VisibleForTesting var monthGraph: MonthGraph? = null
  @VisibleForTesting var selectedDate: DateSnapshot? = null
    set(value) {
      field = value
      selectedDateCalendar = value?.asCalendar()
    }
  private var selectedDateCalendar: Calendar? = null

  fun maybeInit() {
    if (!didInit) {
      var now = getNow()
      val nowSnapshot = now.snapshot()
      if (minMaxController.isOutOfMaxRange(nowSnapshot)) {
        now = minMaxController.getMaxDate()!!
      } else if (minMaxController.isOutOfMinRange(nowSnapshot)) {
        now = minMaxController.getMinDate()!!
      }
      setFullDate(now, notifyListeners = false)
    }
  }

  fun previousMonth() {
    switchToDaysOfMonthMode()
    val calendar = viewingMonth!!.asCalendar(1)
        .decrementMonth()
    updateCurrentMonth(calendar)
    render(calendar)
    vibrator.vibrateForSelection()
  }

  fun nextMonth() {
    switchToDaysOfMonthMode()
    val calendar = viewingMonth!!.asCalendar(1)
        .incrementMonth()
    updateCurrentMonth(calendar)
    render(calendar)
    vibrator.vibrateForSelection()
  }

  fun setMonth(month: Int) {
    switchToDaysOfMonthMode()
    val calendar = viewingMonth!!.asCalendar(1)
        .apply { this.month = month }
    updateCurrentMonth(calendar)
    render(calendar)
    vibrator.vibrateForSelection()
  }

  fun setFullDate(
    calendar: Calendar,
    notifyListeners: Boolean = true
  ) {
    val oldSelected: Calendar = currentSelectedOrNow()
    this.didInit = true
    this.selectedDate = calendar.snapshot()
    if (notifyListeners) {
      notifyListeners(oldSelected) { calendar.clone() as Calendar }
    }
    updateCurrentMonth(calendar)
    render(calendar)
  }

  fun setFullDate(
    @IntRange(from = 1, to = Long.MAX_VALUE) year: Int? = null,
    month: Int,
    @IntRange(from = 1, to = 31) selectedDate: Int? = null,
    notifyListeners: Boolean = true
  ) = setFullDate(getNow().apply {
    if (year != null) {
      this.year = year
    }
    this.month = month
    if (selectedDate != null) {
      this.dayOfMonth = selectedDate
    }
  }, notifyListeners = notifyListeners)

  @CheckResult fun getFullDate(): Calendar? = selectedDateCalendar

  fun setDayOfMonth(day: Int) {
    if (!didInit) {
      setFullDate(getNow().apply {
        dayOfMonth = day
      })
      return
    }

    val oldSelected: Calendar = currentSelectedOrNow()
    val calendar = viewingMonth!!.asCalendar(day)
    selectedDate = calendar.snapshot()
    vibrator.vibrateForSelection()
    notifyListeners(oldSelected) { calendar }
    render(calendar)
  }

  fun setYear(year: Int) {
    setFullDate(
        month = selectedDate!!.month,
        year = year,
        selectedDate = selectedDate!!.day
    )
    switchToDaysOfMonthMode()
  }

  fun addDateChangedListener(listener: OnDateChanged) {
    dateChangedListeners.add(listener)
  }

  fun clearDateChangedListeners() {
    dateChangedListeners.clear()
  }

  private fun updateCurrentMonth(calendar: Calendar) {
    viewingMonth = calendar.snapshotMonth()
    monthGraph = MonthGraph(calendar)
    renderDaysOfWeek(monthGraph!!.orderedWeekDays)
  }

  private fun render(calendar: Calendar) {
    renderHeaders(calendar, selectedDateCalendar!!)
    renderDaysOfMonth(monthGraph!!.getDaysOfMonth(selectedDate!!))
    goBackVisibility(minMaxController.canGoBack(calendar))
    goForwardVisibility(minMaxController.canGoForward(calendar))
  }

  private fun notifyListeners(
    old: Calendar,
    block: () -> Calendar
  ) {
    if (dateChangedListeners.isNotEmpty()) {
      dateChangedListeners.forEach { it(old, block()) }
    }
  }

  private fun currentSelectedOrNow(): Calendar = selectedDateCalendar ?: getNow()
}
