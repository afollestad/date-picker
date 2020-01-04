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

import com.afollestad.date.data.DateFormatter
import com.afollestad.date.data.MonthGraph
import com.afollestad.date.data.MonthItem
import com.afollestad.date.data.snapshot.DateSnapshot
import com.afollestad.date.data.snapshot.MonthSnapshot
import com.afollestad.date.data.snapshot.snapshot
import com.afollestad.date.data.snapshot.snapshotMonth
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import java.util.Calendar
import java.util.GregorianCalendar
import org.junit.Test

/** @author Aidan Follestad (@afollestad) */
class DatePickerControllerTest {
  private val now = GregorianCalendar(1995, Calendar.JULY, 28)
  private val vibrator = mock<VibratorController>()
  private val renderHeaders = mock<(MonthSnapshot, DateSnapshot, Boolean) -> Unit>()
  private val renderMonthItems = mock<(List<MonthItem>) -> Unit>()
  private val switchToMonthMode = mock<() -> Unit>()
  private val listener = mock<(Calendar, Calendar) -> Unit>()
  private val dateFormatter = DateFormatter()

  private val controller = DatePickerController(
      vibrator,
      renderHeaders,
      renderMonthItems,
      switchToMonthMode,
      dateFormatter = dateFormatter,
      getNow = { now }
  ).apply { addDateChangedListener(listener) }

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

  @Test fun previousMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.previousMonth()

    val previousMonth = MonthSnapshot(Calendar.JUNE, 1995)
    assertSetCurrentMonth(previousMonth)
    assertRender(previousMonth, now, false)
    verify(vibrator).vibrateForSelection()
    verify(switchToMonthMode).invoke()
  }

  @Test fun nextMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.nextMonth()

    val nextMonth = MonthSnapshot(Calendar.AUGUST, 1995)
    assertSetCurrentMonth(nextMonth)
    assertRender(nextMonth, now, false)
    verify(vibrator).vibrateForSelection()
    verify(switchToMonthMode).invoke()
  }

  @Test fun setMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.setMonth(Calendar.DECEMBER)

    val expectedMonth = MonthSnapshot(Calendar.DECEMBER, 1995)
    assertSetCurrentMonth(expectedMonth)
    assertRender(expectedMonth, now, false)
    verify(vibrator).vibrateForSelection()
    verify(switchToMonthMode).invoke()
  }

  @Test fun `setMonth - thenYear - thenDayOfMonth`() {
    controller.didInit = false
    controller.maybeInit()
    assertThat(controller.didInit).isTrue()

    controller.setMonth(Calendar.AUGUST)
    controller.setYear(2017)
    controller.setDayOfMonth(7)

    val expectedSelectedDate = DateSnapshot(Calendar.AUGUST, 7, 2017)
    val expectedSelectedCalendar = expectedSelectedDate.asCalendar()
    val expectedOldDate = expectedSelectedDate.copy(day = 28)
    val expectedMonthSnapshot = MonthSnapshot(Calendar.AUGUST, 2017)

    assertThat(controller.selectedDate!!).isEqualTo(expectedSelectedDate)
    assertSetCurrentMonth(expectedMonthSnapshot)
    assertListenerGotDate(expectedOldDate, expectedSelectedDate)
    assertRender(expectedMonthSnapshot, expectedSelectedCalendar, false)
  }

  @Test fun setFullDate() {
    controller.didInit = false
    controller.selectedDate = null

    val expectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    val expectedCalendar = expectedDate.asCalendar()
    val expectedMonthSnapshot = MonthSnapshot(Calendar.JANUARY, 1995)

    controller.setFullDate(expectedCalendar)

    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(now.snapshot(), expectedDate)
    assertSetCurrentMonth(expectedMonthSnapshot)
    assertRender(expectedMonthSnapshot, expectedCalendar, false)
  }

  @Test fun `setFullDate - manual`() {
    controller.didInit = false
    controller.selectedDate = null
    controller.setFullDate(1995, Calendar.JANUARY, 11)

    val expectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    val expectedCalendar = expectedDate.asCalendar()
    val expectedMonthSnapshot = MonthSnapshot(Calendar.JANUARY, 1995)

    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertListenerGotDate(now.snapshot(), expectedDate)
    assertSetCurrentMonth(expectedMonthSnapshot)
    assertRender(expectedMonthSnapshot, expectedCalendar, false)
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
    assertRender(now.snapshotMonth(), expectedCalendar, false)
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
    assertRender(MonthSnapshot(Calendar.AUGUST, 2016), expectedCalendar, false)

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
    currentMonth: MonthSnapshot,
    selectedDate: Calendar,
    fromUserEditInput: Boolean
  ) {
    val monthSnapshotCaptor = argumentCaptor<MonthSnapshot>()
    val selectedDateCaptor = argumentCaptor<DateSnapshot>()
    verify(renderHeaders, atLeast(1)).invoke(
        monthSnapshotCaptor.capture(), selectedDateCaptor.capture(), eq(fromUserEditInput)
    )

    val captured1 = monthSnapshotCaptor.lastValue
    assertWithMessage("Viewing months should be equal.")
        .that(captured1)
        .isEqualTo(currentMonth)

    val selectedCaptured = selectedDateCaptor.lastValue
    assertWithMessage("Selected dates should be equal.")
        .that(selectedCaptured.asCalendar().snapshotMonth())
        .isEqualTo(selectedDate.snapshotMonth())

    verify(renderMonthItems, atLeast(1)).invoke(isA())
  }

  private fun assertSetCurrentMonth(month: MonthSnapshot) {
    assertThat(controller.viewingMonth).isEqualTo(MonthSnapshot(month.month, month.year))
    assertThat(controller.monthGraph!!.calendar.snapshotMonth()).isEqualTo(month)
  }

  private fun assertListenerGotDate(
    oldDate: DateSnapshot,
    newDate: DateSnapshot
  ) {
    val oldCaptor = argumentCaptor<Calendar>()
    val newCaptor = argumentCaptor<Calendar>()
    verify(listener, atLeast(1)).invoke(oldCaptor.capture(), newCaptor.capture())

    val capturedOld = oldCaptor.lastValue
    assertWithMessage("Didn't get matching old date in emission.")
        .that(capturedOld.snapshot())
        .isEqualTo(oldDate)

    val capturedNew = newCaptor.lastValue
    assertWithMessage("Didn't get matching new date in emission.")
        .that(capturedNew.snapshot())
        .isEqualTo(newDate)
  }
}
