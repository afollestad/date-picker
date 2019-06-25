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
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
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
    on { canGoBack(any()) } doReturn true
    on { canGoForward(any()) } doReturn true
  }
  private val renderHeaders = mock<(Calendar, Calendar) -> Unit>()
  private val renderDaysOfWeek = mock<(List<DayOfWeek>) -> Unit>()
  private val renderDaysOfMonth = mock<(List<DayOfMonth>) -> Unit>()
  private val goBackVisibility = mock<(Boolean) -> Unit>()
  private val goForwardVisibility = mock<(Boolean) -> Unit>()
  private val switchToMonthMode = mock<() -> Unit>()
  private val listener = mock<(Calendar, Calendar) -> Unit>()

  private val controller = DatePickerController(
      vibrator,
      minMaxController,
      renderHeaders,
      renderDaysOfWeek,
      renderDaysOfMonth,
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

    verify(listener, never()).invoke(any(), any())
  }

  @Test fun `maybeInit - did already init`() {
    controller.didInit = true
    controller.maybeInit()

    assertThat(controller.viewingMonth).isNull()
    assertThat(controller.monthGraph).isNull()
    assertThat(controller.selectedDate).isNull()

    verify(listener, never()).invoke(any(), any())
  }

  @Test fun `maybeInit - now is before min date`() {
    val minDate = DateSnapshot(Calendar.AUGUST, 7, 2016)
    val minDateCalendar: Calendar? = minDate.asCalendar()
    controller.didInit = false
    whenever(minMaxController.isOutOfMinRange(isA())).doReturn(true)
    whenever(minMaxController.getMinDate()).doReturn(minDateCalendar)
    controller.maybeInit()

    assertThat(controller.didInit).isTrue()
    assertThat(controller.viewingMonth).isEqualTo(MonthSnapshot(Calendar.AUGUST, 2016))
    assertThat(controller.monthGraph).isNotNull()
    assertThat(controller.selectedDate).isEqualTo(minDate)

    verify(listener, never()).invoke(any(), any())
  }

  @Test fun `maybeInit - now is after max date`() {
    val maxDate = DateSnapshot(Calendar.JUNE, 2, 1990)
    val maxDateCalendar: Calendar? = maxDate.asCalendar()
    controller.didInit = false
    whenever(minMaxController.isOutOfMaxRange(isA())).doReturn(true)
    whenever(minMaxController.getMaxDate()).doReturn(maxDateCalendar)
    controller.maybeInit()

    assertThat(controller.didInit).isTrue()
    assertThat(controller.viewingMonth).isEqualTo(MonthSnapshot(Calendar.JUNE, 1990))
    assertThat(controller.monthGraph).isNotNull()
    assertThat(controller.selectedDate).isEqualTo(maxDate)

    verify(listener, never()).invoke(any(), any())
  }

  @Test fun previousMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.previousMonth()

    val previousMonth = MonthSnapshot(Calendar.JUNE, 1995)
    assertSetCurrentMonth(previousMonth)
    assertRender(previousMonth.asCalendar(1), now)
    verify(vibrator).vibrateForSelection()
    verify(switchToMonthMode).invoke()
  }

  @Test fun nextMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.nextMonth()

    val nextMonth = MonthSnapshot(Calendar.AUGUST, 1995)
    assertSetCurrentMonth(nextMonth)
    assertRender(nextMonth.asCalendar(1), now)
    verify(vibrator).vibrateForSelection()
    verify(switchToMonthMode).invoke()
  }

  @Test fun setMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.setMonth(Calendar.DECEMBER)

    val expectedMonth = MonthSnapshot(Calendar.DECEMBER, 1995)
    assertSetCurrentMonth(expectedMonth)
    assertRender(expectedMonth.asCalendar(1), now)
    verify(vibrator).vibrateForSelection()
    verify(switchToMonthMode).invoke()
  }

  @Test fun setFullDate() {
    controller.didInit = false
    controller.selectedDate = null

    val expectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    val expectedCalendar = expectedDate.asCalendar()
    controller.setFullDate(expectedCalendar)

    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(now.snapshot(), expectedDate)
    assertSetCurrentMonth(MonthSnapshot(Calendar.JANUARY, 1995))
    assertRender(expectedCalendar, expectedCalendar)
  }

  @Test fun `setFullDate - manual`() {
    controller.didInit = false
    controller.selectedDate = null
    controller.setFullDate(1995, Calendar.JANUARY, 11)

    val expectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    val expectedCalendar = expectedDate.asCalendar()

    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(now.snapshot(), expectedDate)
    assertSetCurrentMonth(MonthSnapshot(Calendar.JANUARY, 1995))
    assertRender(expectedCalendar, expectedCalendar)
  }

  @Test fun `setDayOfMonth - did not already init`() {
    controller.didInit = false
    controller.viewingMonth = null
    controller.selectedDate = null
    controller.setDayOfMonth(4)

    val expectedDate = now.snapshot()
        .copy(day = 4)
    val expectedCalendar = expectedDate.asCalendar()

    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(now.snapshot(), expectedDate)
    assertSetCurrentMonth(now.snapshotMonth())
    assertRender(expectedCalendar, expectedCalendar)
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
    assertListenerGotDate(now.snapshot(), expectedDate)
    assertRender(expectedCalendar, expectedCalendar)

    verify(vibrator).vibrateForSelection()
  }

  @Test fun setYear() {
    val selectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    controller.selectedDate = selectedDate
    controller.setYear(2018)
    assertListenerGotDate(selectedDate, selectedDate.copy(year = 2018))
    verify(switchToMonthMode, times(1)).invoke()
  }

  private fun assertRender(
    calendar: Calendar,
    selectedDate: Calendar,
    canGoBack: Boolean = true,
    canGoForward: Boolean = true
  ) {
    val calendarCaptor = argumentCaptor<Calendar>()
    val selectedDateCaptor = argumentCaptor<Calendar>()
    verify(renderHeaders).invoke(calendarCaptor.capture(), selectedDateCaptor.capture())

    val captured1 = calendarCaptor.lastValue
    assertWithMessage("Viewing months should be equal.")
        .that(calendar.snapshotMonth())
        .isEqualTo(captured1.snapshotMonth())

    val selectedCaptured = selectedDateCaptor.lastValue
    assertWithMessage("Selected dates should be equal.")
        .that(selectedDate.snapshotMonth())
        .isEqualTo(selectedCaptured.snapshotMonth())

    verify(renderDaysOfMonth).invoke(isA())
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

  private fun assertListenerGotDate(
    oldDate: DateSnapshot,
    newDate: DateSnapshot
  ) {
    val oldCaptor = argumentCaptor<Calendar>()
    val newCaptor = argumentCaptor<Calendar>()

    verify(listener).invoke(oldCaptor.capture(), newCaptor.capture())

    val capturedOld = oldCaptor.allValues.single()
    assertWithMessage("Didn't get matching old year in emission.")
        .that(oldDate.year)
        .isEqualTo(capturedOld.year)
    assertWithMessage("Didn't get matching old month in emission.")
        .that(oldDate.month)
        .isEqualTo(capturedOld.month)
    assertWithMessage("Didn't get matching old day in emission.")
        .that(oldDate.month)
        .isEqualTo(capturedOld.month)

    val capturedNew = newCaptor.allValues.single()
    assertWithMessage("Didn't get matching new year in emission.")
        .that(newDate.year)
        .isEqualTo(capturedNew.year)
    assertWithMessage("Didn't get matching new month in emission.")
        .that(newDate.month)
        .isEqualTo(capturedNew.month)
    assertWithMessage("Didn't get matching new day in emission.")
        .that(newDate.month)
        .isEqualTo(capturedNew.month)
  }
}
