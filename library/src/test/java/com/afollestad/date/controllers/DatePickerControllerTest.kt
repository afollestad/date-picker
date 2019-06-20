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

import com.afollestad.date.internal.DayOfWeek
import com.afollestad.date.internal.MonthGraph
import com.afollestad.date.internal.Week
import com.afollestad.date.month
import com.afollestad.date.snapshot.DateSnapshot
import com.afollestad.date.snapshot.MonthSnapshot
import com.afollestad.date.snapshot.asCalendar
import com.afollestad.date.snapshot.snapshot
import com.afollestad.date.snapshot.snapshotMonth
import com.afollestad.date.year
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

/** @author Aidan Follestad (@afollestad) */
class DatePickerControllerTest {
  private val now = GregorianCalendar(1995, Calendar.JULY, 28)
  private val vibrator = mock<VibratorController>()
  private val minMaxController = mock<MinMaxController> {
    // TODO test false for these and their effects
    on { canGoBack(any()) } doReturn true
    on { canGoForward(any()) } doReturn true
  }
  private val renderHeaders = mock<(Calendar, DateSnapshot) -> Unit>()
  private val renderDaysOfWeek = mock<(List<DayOfWeek>) -> Unit>()
  private val renderWeeks = mock<(List<Week>) -> Unit>()
  private val goBackVisibility = mock<(Boolean) -> Unit>()
  private val goForwardVisibility = mock<(Boolean) -> Unit>()
  private val switchToMonthMode = mock<() -> Unit>()
  private val listener = mock<(Calendar) -> Unit>()

  private val controller = DatePickerController(
      vibrator,
      minMaxController,
      renderHeaders,
      renderDaysOfWeek,
      renderWeeks,
      goBackVisibility,
      goForwardVisibility,
      switchToMonthMode,
      getNow = { now }
  ).apply {
    addDateChangedListener(listener)
  }

  @Test fun `maybeInit - did not already init`() {
    val selectedDate = DateSnapshot(Calendar.JULY, 28, 1995)
    controller.didInit = false
    controller.maybeInit()

    assertThat(controller.didInit).isTrue()
    assertThat(controller.viewingMonth).isEqualTo(MonthSnapshot(Calendar.JULY, 1995))
    assertThat(controller.monthGraph).isNotNull()
    assertThat(controller.selectedDate).isEqualTo(selectedDate)

    assertListenerGotDate(selectedDate)
  }

  @Test fun `maybeInit - did already init`() {
    controller.didInit = true
    controller.maybeInit()

    assertThat(controller.viewingMonth).isNull()
    assertThat(controller.monthGraph).isNull()
    assertThat(controller.selectedDate).isNull()

    verify(listener, never()).invoke(any())
  }

  @Test fun `maybeInit - now is before min date`() {
    val minDate = DateSnapshot(Calendar.AUGUST, 7, 2016)
    controller.didInit = false
    whenever(minMaxController.isOutOfMinRange(isA())).doReturn(true)
    whenever(minMaxController.getMinDate()).doReturn(minDate.asCalendar())
    controller.maybeInit()

    assertThat(controller.didInit).isTrue()
    assertThat(controller.viewingMonth).isEqualTo(MonthSnapshot(Calendar.AUGUST, 2016))
    assertThat(controller.monthGraph).isNotNull()
    assertThat(controller.selectedDate).isEqualTo(minDate)

    assertListenerGotDate(minDate)
  }

  @Test fun `maybeInit - now is after max date`() {
    val maxDate = DateSnapshot(Calendar.JUNE, 2, 1990)
    controller.didInit = false
    whenever(minMaxController.isOutOfMaxRange(isA())).doReturn(true)
    whenever(minMaxController.getMaxDate()).doReturn(maxDate.asCalendar())
    controller.maybeInit()

    assertThat(controller.didInit).isTrue()
    assertThat(controller.viewingMonth).isEqualTo(MonthSnapshot(Calendar.JUNE, 1990))
    assertThat(controller.monthGraph).isNotNull()
    assertThat(controller.selectedDate).isEqualTo(maxDate)

    assertListenerGotDate(maxDate)
  }

  @Test fun previousMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.previousMonth()

    val previousMonth = MonthSnapshot(Calendar.JUNE, 1995)
    assertSetCurrentMonth(previousMonth)
    assertRender(previousMonth.asCalendar(1), now.snapshot())
    verify(vibrator).vibrateForSelection()
  }

  @Test fun nextMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.nextMonth()

    val nextMonth = MonthSnapshot(Calendar.AUGUST, 1995)
    assertSetCurrentMonth(nextMonth)
    assertRender(nextMonth.asCalendar(1), now.snapshot())
    verify(vibrator).vibrateForSelection()
  }

  @Test fun setFullDate() {
    controller.didInit = false
    controller.selectedDate = null

    val expectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    controller.setFullDate(expectedDate.asCalendar())

    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(expectedDate)
    assertSetCurrentMonth(MonthSnapshot(Calendar.JANUARY, 1995))
    assertRender(expectedDate.asCalendar(), expectedDate)
  }

  @Test fun `setFullDate - manual`() {
    controller.didInit = false
    controller.selectedDate = null
    controller.setFullDate(1995, Calendar.JANUARY, 11)

    val expectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(expectedDate)
    assertSetCurrentMonth(MonthSnapshot(Calendar.JANUARY, 1995))
    assertRender(expectedDate.asCalendar(), expectedDate)
  }

  @Test fun `setDayOfMonth - did not already init`() {
    controller.didInit = false
    controller.viewingMonth = null
    controller.selectedDate = null
    controller.setDayOfMonth(4)

    val expectedDate = now.snapshot()
        .copy(day = 4)

    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(expectedDate)
    assertSetCurrentMonth(now.snapshotMonth())
    assertRender(expectedDate.asCalendar(), expectedDate)
  }

  @Test fun `setDayOfMonth - did already init`() {
    val expectedDate = DateSnapshot(Calendar.AUGUST, 4, 2016)
    val expectedCalendar = expectedDate.asCalendar()

    controller.didInit = true
    controller.viewingMonth = MonthSnapshot(Calendar.AUGUST, 2016)
    controller.monthGraph = MonthGraph(expectedCalendar)
    controller.selectedDate = null

    controller.setDayOfMonth(4)
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(expectedDate)
    assertRender(expectedCalendar, expectedDate)

    verify(vibrator).vibrateForSelection()
  }

  @Test fun setYear() {
    val selectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    controller.selectedDate = selectedDate
    controller.setYear(2018)
    assertListenerGotDate(selectedDate.copy(year = 2018))
    verify(switchToMonthMode, times(1)).invoke()
  }

  private fun assertRender(
    calendar: Calendar,
    selectedDate: DateSnapshot,
    canGoBack: Boolean = true,
    canGoForward: Boolean = true
  ) {
    val calendarCaptor = argumentCaptor<Calendar>()
    verify(renderHeaders).invoke(calendarCaptor.capture(), eq(selectedDate))
    val captured = calendarCaptor.lastValue
    assertWithMessage("Calendars should have an  equal year.")
        .that(calendar.year)
        .isEqualTo(captured.year)
    assertWithMessage("Calendars should have an equal month.")
        .that(calendar.month)
        .isEqualTo(captured.month)

    verify(renderWeeks).invoke(isA())
    verify(goBackVisibility).invoke(canGoBack)
    verify(goForwardVisibility).invoke(canGoForward)
  }

  private fun assertSetCurrentMonth(month: MonthSnapshot) {
    assertThat(controller.viewingMonth).isEqualTo(MonthSnapshot(month.month, month.year))
    assertThat(controller.monthGraph!!.calendar.month).isEqualTo(month.month)
    assertThat(controller.monthGraph!!.calendar.year).isEqualTo(month.year)
    verify(renderDaysOfWeek).invoke(
        listOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
    )
  }

  private fun assertListenerGotDate(date: DateSnapshot) {
    val captor = argumentCaptor<Calendar>()
    verify(listener).invoke(captor.capture())
    val captured = captor.lastValue
    assertWithMessage("Didn't get matching year in emission.")
        .that(date.year)
        .isEqualTo(captured.year)
    assertWithMessage("Didn't get matching month in emission.")
        .that(date.month)
        .isEqualTo(captured.month)
    assertWithMessage("Didn't get matching day in emission.")
        .that(date.month)
        .isEqualTo(captured.month)
  }
}
