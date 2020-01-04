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

import com.afollestad.date.data.DayOfWeek.FRIDAY
import com.afollestad.date.data.DayOfWeek.MONDAY
import com.afollestad.date.data.DayOfWeek.SATURDAY
import com.afollestad.date.data.DayOfWeek.SUNDAY
import com.afollestad.date.data.DayOfWeek.THURSDAY
import com.afollestad.date.data.DayOfWeek.TUESDAY
import com.afollestad.date.data.DayOfWeek.WEDNESDAY
import com.afollestad.date.data.MonthGraph
import com.afollestad.date.data.MonthItem.DayOfMonth
import com.afollestad.date.data.MonthItem.WeekHeader
import com.afollestad.date.data.NO_DATE
import com.afollestad.date.data.snapshot.snapshot
import com.afollestad.date.data.snapshot.snapshotMonth
import com.google.common.truth.Truth.assertThat
import java.util.Calendar
import java.util.Locale
import org.junit.Test

class MonthGraphTest {

  @Test fun january_2019() {
    val calendar = Calendar.getInstance(Locale.US)
        .apply {
          set(Calendar.YEAR, 2019)
          set(Calendar.MONTH, Calendar.JANUARY)
          set(Calendar.DAY_OF_MONTH, 5)
        }
    val selectedDate = calendar.snapshot()
    val month = calendar.snapshotMonth()

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(TUESDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(31)

    val items = graph.getMonthItems(selectedDate)

    assertThat(items).isEqualTo(
        listOf(
            WeekHeader(SUNDAY),
            WeekHeader(MONDAY),
            WeekHeader(TUESDAY),
            WeekHeader(WEDNESDAY),
            WeekHeader(THURSDAY),
            WeekHeader(FRIDAY),
            WeekHeader(SATURDAY),
            DayOfMonth(SUNDAY, month, NO_DATE),
            DayOfMonth(MONDAY, month, NO_DATE),
            DayOfMonth(TUESDAY, month, 1),
            DayOfMonth(WEDNESDAY, month, 2),
            DayOfMonth(THURSDAY, month, 3),
            DayOfMonth(FRIDAY, month, 4),
            DayOfMonth(SATURDAY, month, 5, isSelected = true),
            DayOfMonth(SUNDAY, month, 6),
            DayOfMonth(MONDAY, month, 7),
            DayOfMonth(TUESDAY, month, 8),
            DayOfMonth(WEDNESDAY, month, 9),
            DayOfMonth(THURSDAY, month, 10),
            DayOfMonth(FRIDAY, month, 11),
            DayOfMonth(SATURDAY, month, 12),
            DayOfMonth(SUNDAY, month, 13),
            DayOfMonth(MONDAY, month, 14),
            DayOfMonth(TUESDAY, month, 15),
            DayOfMonth(WEDNESDAY, month, 16),
            DayOfMonth(THURSDAY, month, 17),
            DayOfMonth(FRIDAY, month, 18),
            DayOfMonth(SATURDAY, month, 19),
            DayOfMonth(SUNDAY, month, 20),
            DayOfMonth(MONDAY, month, 21),
            DayOfMonth(TUESDAY, month, 22),
            DayOfMonth(WEDNESDAY, month, 23),
            DayOfMonth(THURSDAY, month, 24),
            DayOfMonth(FRIDAY, month, 25),
            DayOfMonth(SATURDAY, month, 26),
            DayOfMonth(SUNDAY, month, 27),
            DayOfMonth(MONDAY, month, 28),
            DayOfMonth(TUESDAY, month, 29),
            DayOfMonth(WEDNESDAY, month, 30),
            DayOfMonth(THURSDAY, month, 31),
            DayOfMonth(FRIDAY, month, NO_DATE),
            DayOfMonth(SATURDAY, month, NO_DATE),
            DayOfMonth(SUNDAY, month, NO_DATE),
            DayOfMonth(MONDAY, month, NO_DATE),
            DayOfMonth(TUESDAY, month, NO_DATE),
            DayOfMonth(WEDNESDAY, month, NO_DATE),
            DayOfMonth(THURSDAY, month, NO_DATE),
            DayOfMonth(FRIDAY, month, NO_DATE),
            DayOfMonth(SATURDAY, month, NO_DATE)
        )
    )
  }

  @Test fun february_2019_french_locale() {
    val calendar = Calendar.getInstance(Locale.FRANCE)
        .apply {
          set(Calendar.YEAR, 2019)
          set(Calendar.MONTH, Calendar.FEBRUARY)
          set(Calendar.DAY_OF_MONTH, 1)
        }
    val selectedDate = calendar.snapshot()
    val month = calendar.snapshotMonth()

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(FRIDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(28)

    val items = graph.getMonthItems(selectedDate)

    assertThat(items).isEqualTo(
        listOf(
            WeekHeader(MONDAY),
            WeekHeader(TUESDAY),
            WeekHeader(WEDNESDAY),
            WeekHeader(THURSDAY),
            WeekHeader(FRIDAY),
            WeekHeader(SATURDAY),
            WeekHeader(SUNDAY),
            DayOfMonth(MONDAY, month),
            DayOfMonth(TUESDAY, month),
            DayOfMonth(WEDNESDAY, month),
            DayOfMonth(THURSDAY, month),
            DayOfMonth(FRIDAY, month, 1, isSelected = true),
            DayOfMonth(SATURDAY, month, 2),
            DayOfMonth(SUNDAY, month, 3),
            DayOfMonth(MONDAY, month, 4),
            DayOfMonth(TUESDAY, month, 5),
            DayOfMonth(WEDNESDAY, month, 6),
            DayOfMonth(THURSDAY, month, 7),
            DayOfMonth(FRIDAY, month, 8),
            DayOfMonth(SATURDAY, month, 9),
            DayOfMonth(SUNDAY, month, 10),
            DayOfMonth(MONDAY, month, 11),
            DayOfMonth(TUESDAY, month, 12),
            DayOfMonth(WEDNESDAY, month, 13),
            DayOfMonth(THURSDAY, month, 14),
            DayOfMonth(FRIDAY, month, 15),
            DayOfMonth(SATURDAY, month, 16),
            DayOfMonth(SUNDAY, month, 17),
            DayOfMonth(MONDAY, month, 18),
            DayOfMonth(TUESDAY, month, 19),
            DayOfMonth(WEDNESDAY, month, 20),
            DayOfMonth(THURSDAY, month, 21),
            DayOfMonth(FRIDAY, month, 22),
            DayOfMonth(SATURDAY, month, 23),
            DayOfMonth(SUNDAY, month, 24),
            DayOfMonth(MONDAY, month, 25),
            DayOfMonth(TUESDAY, month, 26),
            DayOfMonth(WEDNESDAY, month, 27),
            DayOfMonth(THURSDAY, month, 28),
            DayOfMonth(FRIDAY, month),
            DayOfMonth(SATURDAY, month),
            DayOfMonth(SUNDAY, month),
            DayOfMonth(MONDAY, month),
            DayOfMonth(TUESDAY, month),
            DayOfMonth(WEDNESDAY, month),
            DayOfMonth(THURSDAY, month),
            DayOfMonth(FRIDAY, month),
            DayOfMonth(SATURDAY, month),
            DayOfMonth(SUNDAY, month)
        )
    )
  }

  @Test fun june_2019() {
    val calendar = Calendar.getInstance(Locale.US)
        .apply {
          set(Calendar.YEAR, 2019)
          set(Calendar.MONTH, Calendar.JUNE)
          set(Calendar.DAY_OF_MONTH, 8)
        }
    val selectedDate = calendar.snapshot()
    val month = calendar.snapshotMonth()

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(SATURDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(30)

    val items = graph.getMonthItems(selectedDate)

    assertThat(items).isEqualTo(
        listOf(
            WeekHeader(SUNDAY),
            WeekHeader(MONDAY),
            WeekHeader(TUESDAY),
            WeekHeader(WEDNESDAY),
            WeekHeader(THURSDAY),
            WeekHeader(FRIDAY),
            WeekHeader(SATURDAY),
            DayOfMonth(SUNDAY, month, NO_DATE),
            DayOfMonth(MONDAY, month, NO_DATE),
            DayOfMonth(TUESDAY, month, NO_DATE),
            DayOfMonth(WEDNESDAY, month, NO_DATE),
            DayOfMonth(THURSDAY, month, NO_DATE),
            DayOfMonth(FRIDAY, month, NO_DATE),
            DayOfMonth(SATURDAY, month, 1),
            DayOfMonth(SUNDAY, month, 2),
            DayOfMonth(MONDAY, month, 3),
            DayOfMonth(TUESDAY, month, 4),
            DayOfMonth(WEDNESDAY, month, 5),
            DayOfMonth(THURSDAY, month, 6),
            DayOfMonth(FRIDAY, month, 7),
            DayOfMonth(SATURDAY, month, 8, isSelected = true),
            DayOfMonth(SUNDAY, month, 9),
            DayOfMonth(MONDAY, month, 10),
            DayOfMonth(TUESDAY, month, 11),
            DayOfMonth(WEDNESDAY, month, 12),
            DayOfMonth(THURSDAY, month, 13),
            DayOfMonth(FRIDAY, month, 14),
            DayOfMonth(SATURDAY, month, 15),
            DayOfMonth(SUNDAY, month, 16),
            DayOfMonth(MONDAY, month, 17),
            DayOfMonth(TUESDAY, month, 18),
            DayOfMonth(WEDNESDAY, month, 19),
            DayOfMonth(THURSDAY, month, 20),
            DayOfMonth(FRIDAY, month, 21),
            DayOfMonth(SATURDAY, month, 22),
            DayOfMonth(SUNDAY, month, 23),
            DayOfMonth(MONDAY, month, 24),
            DayOfMonth(TUESDAY, month, 25),
            DayOfMonth(WEDNESDAY, month, 26),
            DayOfMonth(THURSDAY, month, 27),
            DayOfMonth(FRIDAY, month, 28),
            DayOfMonth(SATURDAY, month, 29),
            DayOfMonth(SUNDAY, month, 30),
            DayOfMonth(MONDAY, month, NO_DATE),
            DayOfMonth(TUESDAY, month, NO_DATE),
            DayOfMonth(WEDNESDAY, month, NO_DATE),
            DayOfMonth(THURSDAY, month, NO_DATE),
            DayOfMonth(FRIDAY, month, NO_DATE),
            DayOfMonth(SATURDAY, month, NO_DATE)
        )
    )
  }

  @Test fun july_1995() {
    val calendar = Calendar.getInstance(Locale.US)
        .apply {
          set(Calendar.YEAR, 1995)
          set(Calendar.MONTH, Calendar.JULY)
          set(Calendar.DAY_OF_MONTH, 28)
        }
    val selectedDate = calendar.snapshot()
    val month = calendar.snapshotMonth()

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(SATURDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(31)

    val items = graph.getMonthItems(selectedDate)

    assertThat(items).isEqualTo(
        listOf(
            WeekHeader(SUNDAY),
            WeekHeader(MONDAY),
            WeekHeader(TUESDAY),
            WeekHeader(WEDNESDAY),
            WeekHeader(THURSDAY),
            WeekHeader(FRIDAY),
            WeekHeader(SATURDAY),
            DayOfMonth(SUNDAY, month, NO_DATE),
            DayOfMonth(MONDAY, month, NO_DATE),
            DayOfMonth(TUESDAY, month, NO_DATE),
            DayOfMonth(WEDNESDAY, month, NO_DATE),
            DayOfMonth(THURSDAY, month, NO_DATE),
            DayOfMonth(FRIDAY, month, NO_DATE),
            DayOfMonth(SATURDAY, month, 1),
            DayOfMonth(SUNDAY, month, 2),
            DayOfMonth(MONDAY, month, 3),
            DayOfMonth(TUESDAY, month, 4),
            DayOfMonth(WEDNESDAY, month, 5),
            DayOfMonth(THURSDAY, month, 6),
            DayOfMonth(FRIDAY, month, 7),
            DayOfMonth(SATURDAY, month, 8),
            DayOfMonth(SUNDAY, month, 9),
            DayOfMonth(MONDAY, month, 10),
            DayOfMonth(TUESDAY, month, 11),
            DayOfMonth(WEDNESDAY, month, 12),
            DayOfMonth(THURSDAY, month, 13),
            DayOfMonth(FRIDAY, month, 14),
            DayOfMonth(SATURDAY, month, 15),
            DayOfMonth(SUNDAY, month, 16),
            DayOfMonth(MONDAY, month, 17),
            DayOfMonth(TUESDAY, month, 18),
            DayOfMonth(WEDNESDAY, month, 19),
            DayOfMonth(THURSDAY, month, 20),
            DayOfMonth(FRIDAY, month, 21),
            DayOfMonth(SATURDAY, month, 22),
            DayOfMonth(SUNDAY, month, 23),
            DayOfMonth(MONDAY, month, 24),
            DayOfMonth(TUESDAY, month, 25),
            DayOfMonth(WEDNESDAY, month, 26),
            DayOfMonth(THURSDAY, month, 27),
            DayOfMonth(FRIDAY, month, 28, isSelected = true),
            DayOfMonth(SATURDAY, month, 29),
            DayOfMonth(SUNDAY, month, 30),
            DayOfMonth(MONDAY, month, 31),
            DayOfMonth(TUESDAY, month, NO_DATE),
            DayOfMonth(WEDNESDAY, month, NO_DATE),
            DayOfMonth(THURSDAY, month, NO_DATE),
            DayOfMonth(FRIDAY, month, NO_DATE),
            DayOfMonth(SATURDAY, month, NO_DATE)
        )
    )
  }
}
