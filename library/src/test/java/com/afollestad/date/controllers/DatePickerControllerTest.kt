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
    on { canGoBack(any()) } doReturn true
    on { canGoForward(any()) } doReturn true
  }
  private val renderHeaders = mock<(MonthSnapshot, Calendar) -> Unit>()
  private val renderMonthItems = mock<(List<MonthItem>) -> Unit>()
  private val goBackVisibility = mock<(Boolean) -> Unit>()
  private val goForwardVisibility = mock<(Boolean) -> Unit>()
  private val switchToMonthMode = mock<() -> Unit>()
  private val listener = mock<(Calendar, Calendar) -> Unit>()

  private val controller = DatePickerController(
      vibrator,
      minMaxController,
      renderHeaders,
      renderMonthItems,
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
    assertRender(previousMonth, now)
    verify(vibrator).vibrateForSelection()
    verify(switchToMonthMode).invoke()
  }

  @Test fun nextMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.nextMonth()

    val nextMonth = MonthSnapshot(Calendar.AUGUST, 1995)
    assertSetCurrentMonth(nextMonth)
    assertRender(nextMonth, now)
    verify(vibrator).vibrateForSelection()
    verify(switchToMonthMode).invoke()
  }

  @Test fun setMonth() {
    controller.viewingMonth = now.snapshotMonth()
    controller.selectedDate = now.snapshot()
    controller.setMonth(Calendar.DECEMBER)

    val expectedMonth = MonthSnapshot(Calendar.DECEMBER, 1995)
    assertSetCurrentMonth(expectedMonth)
    assertRender(expectedMonth, now)
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
    assertRender(expectedMonthSnapshot, expectedSelectedCalendar)
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
    assertRender(expectedMonthSnapshot, expectedCalendar)
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
    assertRender(expectedMonthSnapshot, expectedCalendar)
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
    assertRender(now.snapshotMonth(), expectedCalendar)
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
    assertRender(MonthSnapshot(Calendar.AUGUST, 2016), expectedCalendar)

    verify(vibrator).vibrateForSelection()
  }

  @Test fun setYear() {
    val selectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    controller.selectedDate = selectedDate
    controller.setYear(2018)
    assertListenerGotDate(selectedDate, selectedDate.copy(year = 2018))
    verify(switchToMonthMode, times(1)).invoke()
  }

  @Test fun `getFullDate - is null if out of range`() {
    val snapshot = now.snapshot()
    controller.selectedDate = snapshot
    assertThat(controller.getFullDate()!!.snapshot()).isEqualTo(snapshot)

    whenever(minMaxController.isOutOfMinRange(eq(snapshot)))
        .doReturn(true)
    whenever(minMaxController.isOutOfMaxRange(eq(snapshot)))
        .doReturn(true)
    assertThat(controller.getFullDate()).isNull()
  }

  @Test fun `out of range dates do not go through listeners`() {
    controller.didInit = false
    controller.selectedDate = null

    val expectedDate = DateSnapshot(Calendar.JANUARY, 11, 1995)
    val expectedCalendar = expectedDate.asCalendar()
    val expectedCurrentMonth = MonthSnapshot(Calendar.JANUARY, 1995)

    whenever(minMaxController.isOutOfMinRange(eq(expectedDate)))
        .doReturn(true)
    whenever(minMaxController.isOutOfMaxRange(eq(expectedDate)))
        .doReturn(true)

    controller.setFullDate(expectedCalendar)

    assertThat(controller.didInit).isTrue()
    assertThat(controller.selectedDate!!).isEqualTo(expectedDate)
    assertSetCurrentMonth(expectedCurrentMonth)
    assertRender(expectedCurrentMonth, expectedCalendar)

    verify(listener, never()).invoke(any(), any())
  }

  private fun assertRender(
    currentMonth: MonthSnapshot,
    selectedDate: Calendar,
    canGoBack: Boolean = true,
    canGoForward: Boolean = true
  ) {
    val monthSnapshotCaptor = argumentCaptor<MonthSnapshot>()
    val selectedDateCaptor = argumentCaptor<Calendar>()
    verify(renderHeaders, atLeast(1)).invoke(monthSnapshotCaptor.capture(), selectedDateCaptor.capture())

    val captured1 = monthSnapshotCaptor.lastValue
    assertWithMessage("Viewing months should be equal.")
        .that(captured1)
        .isEqualTo(currentMonth)

    val selectedCaptured = selectedDateCaptor.lastValue
    assertWithMessage("Selected dates should be equal.")
        .that(selectedCaptured.snapshotMonth())
        .isEqualTo(selectedDate.snapshotMonth())

    verify(renderMonthItems, atLeast(1)).invoke(isA())
    verify(goBackVisibility, atLeast(1)).invoke(canGoBack)
    verify(goForwardVisibility, atLeast(1)).invoke(canGoForward)
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
