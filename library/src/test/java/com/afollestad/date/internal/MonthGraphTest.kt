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

import com.afollestad.date.internal.DayOfWeek.FRIDAY
import com.afollestad.date.internal.DayOfWeek.MONDAY
import com.afollestad.date.internal.DayOfWeek.SATURDAY
import com.afollestad.date.internal.DayOfWeek.SUNDAY
import com.afollestad.date.internal.DayOfWeek.THURSDAY
import com.afollestad.date.internal.DayOfWeek.TUESDAY
import com.afollestad.date.internal.DayOfWeek.WEDNESDAY
import com.afollestad.date.snapshot.snapshot
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class MonthGraphTest {

  @Test fun january_2019() {
    val calendar = Calendar.getInstance(Locale.US)
        .apply {
          set(Calendar.YEAR, 2019)
          set(Calendar.MONTH, Calendar.JANUARY)
          set(Calendar.DAY_OF_MONTH, 5)
        }
    val selectedDate = calendar.snapshot()

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(TUESDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(31)

    val weeks = graph.getWeeks(selectedDate)
    assertThat(weeks.size).isEqualTo(6)

    assertThat(weeks[0].dates).isEqualTo(
        listOf(
            Date(SUNDAY, NO_DATE),
            Date(MONDAY, NO_DATE),
            Date(TUESDAY, 1),
            Date(WEDNESDAY, 2),
            Date(THURSDAY, 3),
            Date(FRIDAY, 4),
            Date(SATURDAY, 5, isSelected = true)
        )
    )
    assertThat(weeks[1].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 6),
            Date(MONDAY, 7),
            Date(TUESDAY, 8),
            Date(WEDNESDAY, 9),
            Date(THURSDAY, 10),
            Date(FRIDAY, 11),
            Date(SATURDAY, 12)
        )
    )
    assertThat(weeks[2].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 13),
            Date(MONDAY, 14),
            Date(TUESDAY, 15),
            Date(WEDNESDAY, 16),
            Date(THURSDAY, 17),
            Date(FRIDAY, 18),
            Date(SATURDAY, 19)
        )
    )
    assertThat(weeks[3].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 20),
            Date(MONDAY, 21),
            Date(TUESDAY, 22),
            Date(WEDNESDAY, 23),
            Date(THURSDAY, 24),
            Date(FRIDAY, 25),
            Date(SATURDAY, 26)
        )
    )
    assertThat(weeks[4].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 27),
            Date(MONDAY, 28),
            Date(TUESDAY, 29),
            Date(WEDNESDAY, 30),
            Date(THURSDAY, 31, lastOfMonth = true),
            Date(FRIDAY, NO_DATE),
            Date(SATURDAY, NO_DATE)
        )
    )
    assertThat(weeks[5].dates).isEqualTo(
        listOf(
            Date(SUNDAY, NO_DATE),
            Date(MONDAY, NO_DATE),
            Date(TUESDAY, NO_DATE),
            Date(WEDNESDAY, NO_DATE),
            Date(THURSDAY, NO_DATE),
            Date(FRIDAY, NO_DATE),
            Date(SATURDAY, NO_DATE)
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

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(FRIDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(28)

    val weeks = graph.getWeeks(selectedDate)
    assertThat(weeks.size).isEqualTo(6)

    assertThat(weeks[0].dates).isEqualTo(
        listOf(
            Date(MONDAY, NO_DATE),
            Date(TUESDAY, NO_DATE),
            Date(WEDNESDAY, NO_DATE),
            Date(THURSDAY, NO_DATE),
            Date(FRIDAY, 1, isSelected = true),
            Date(SATURDAY, 2),
            Date(SUNDAY, 3)
        )
    )
    assertThat(weeks[1].dates).isEqualTo(
        listOf(
            Date(MONDAY, 4),
            Date(TUESDAY, 5),
            Date(WEDNESDAY, 6),
            Date(THURSDAY, 7),
            Date(FRIDAY, 8),
            Date(SATURDAY, 9),
            Date(SUNDAY, 10)
        )
    )
    assertThat(weeks[2].dates).isEqualTo(
        listOf(
            Date(MONDAY, 11),
            Date(TUESDAY, 12),
            Date(WEDNESDAY, 13),
            Date(THURSDAY, 14),
            Date(FRIDAY, 15),
            Date(SATURDAY, 16),
            Date(SUNDAY, 17)
        )
    )
    assertThat(weeks[3].dates).isEqualTo(
        listOf(
            Date(MONDAY, 18),
            Date(TUESDAY, 19),
            Date(WEDNESDAY, 20),
            Date(THURSDAY, 21),
            Date(FRIDAY, 22),
            Date(SATURDAY, 23),
            Date(SUNDAY, 24)
        )
    )
    assertThat(weeks[4].dates).isEqualTo(
        listOf(
            Date(MONDAY, 25),
            Date(TUESDAY, 26),
            Date(WEDNESDAY, 27),
            Date(THURSDAY, 28, lastOfMonth = true),
            Date(FRIDAY, NO_DATE),
            Date(SATURDAY, NO_DATE),
            Date(SUNDAY, NO_DATE)
        )
    )
    assertThat(weeks[5].dates).isEqualTo(
        listOf(
            Date(MONDAY, NO_DATE),
            Date(TUESDAY, NO_DATE),
            Date(WEDNESDAY, NO_DATE),
            Date(THURSDAY, NO_DATE),
            Date(FRIDAY, NO_DATE),
            Date(SATURDAY, NO_DATE),
            Date(SUNDAY, NO_DATE)
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

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(SATURDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(30)

    val weeks = graph.getWeeks(selectedDate)
    assertThat(weeks.size).isEqualTo(6)

    assertThat(weeks[0].dates).isEqualTo(
        listOf(
            Date(SUNDAY, NO_DATE),
            Date(MONDAY, NO_DATE),
            Date(TUESDAY, NO_DATE),
            Date(WEDNESDAY, NO_DATE),
            Date(THURSDAY, NO_DATE),
            Date(FRIDAY, NO_DATE),
            Date(SATURDAY, 1)
        )
    )
    assertThat(weeks[1].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 2),
            Date(MONDAY, 3),
            Date(TUESDAY, 4),
            Date(WEDNESDAY, 5),
            Date(THURSDAY, 6),
            Date(FRIDAY, 7),
            Date(SATURDAY, 8, isSelected = true)
        )
    )
    assertThat(weeks[2].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 9),
            Date(MONDAY, 10),
            Date(TUESDAY, 11),
            Date(WEDNESDAY, 12),
            Date(THURSDAY, 13),
            Date(FRIDAY, 14),
            Date(SATURDAY, 15)
        )
    )
    assertThat(weeks[3].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 16),
            Date(MONDAY, 17),
            Date(TUESDAY, 18),
            Date(WEDNESDAY, 19),
            Date(THURSDAY, 20),
            Date(FRIDAY, 21),
            Date(SATURDAY, 22)
        )
    )
    assertThat(weeks[4].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 23),
            Date(MONDAY, 24),
            Date(TUESDAY, 25),
            Date(WEDNESDAY, 26),
            Date(THURSDAY, 27),
            Date(FRIDAY, 28),
            Date(SATURDAY, 29)
        )
    )
    assertThat(weeks[5].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 30, lastOfMonth = true),
            Date(MONDAY, NO_DATE),
            Date(TUESDAY, NO_DATE),
            Date(WEDNESDAY, NO_DATE),
            Date(THURSDAY, NO_DATE),
            Date(FRIDAY, NO_DATE),
            Date(SATURDAY, NO_DATE)
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

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(SATURDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(31)

    val weeks = graph.getWeeks(selectedDate)
    assertThat(weeks.size).isEqualTo(6)

    assertThat(weeks[0].dates).isEqualTo(
        listOf(
            Date(SUNDAY, NO_DATE),
            Date(MONDAY, NO_DATE),
            Date(TUESDAY, NO_DATE),
            Date(WEDNESDAY, NO_DATE),
            Date(THURSDAY, NO_DATE),
            Date(FRIDAY, NO_DATE),
            Date(SATURDAY, 1)
        )
    )
    assertThat(weeks[1].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 2),
            Date(MONDAY, 3),
            Date(TUESDAY, 4),
            Date(WEDNESDAY, 5),
            Date(THURSDAY, 6),
            Date(FRIDAY, 7),
            Date(SATURDAY, 8)
        )
    )
    assertThat(weeks[2].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 9),
            Date(MONDAY, 10),
            Date(TUESDAY, 11),
            Date(WEDNESDAY, 12),
            Date(THURSDAY, 13),
            Date(FRIDAY, 14),
            Date(SATURDAY, 15)
        )
    )
    assertThat(weeks[3].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 16),
            Date(MONDAY, 17),
            Date(TUESDAY, 18),
            Date(WEDNESDAY, 19),
            Date(THURSDAY, 20),
            Date(FRIDAY, 21),
            Date(SATURDAY, 22)
        )
    )
    assertThat(weeks[4].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 23),
            Date(MONDAY, 24),
            Date(TUESDAY, 25),
            Date(WEDNESDAY, 26),
            Date(THURSDAY, 27),
            Date(FRIDAY, 28, isSelected = true),
            Date(SATURDAY, 29)
        )
    )
    assertThat(weeks[5].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 30),
            Date(MONDAY, 31, lastOfMonth = true),
            Date(TUESDAY, NO_DATE),
            Date(WEDNESDAY, NO_DATE),
            Date(THURSDAY, NO_DATE),
            Date(FRIDAY, NO_DATE),
            Date(SATURDAY, NO_DATE)
        )
    )
  }
}
