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

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(TUESDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(31)

    val weeks = graph.getWeeks()
    assertThat(weeks.size).isEqualTo(6)

    assertThat(weeks[0].dates).isEqualTo(
        listOf(
            Date(SUNDAY, NO_DATE, false),
            Date(MONDAY, NO_DATE, false),
            Date(TUESDAY, 1, false),
            Date(WEDNESDAY, 2, false),
            Date(THURSDAY, 3, false),
            Date(FRIDAY, 4, false),
            Date(SATURDAY, 5, false)
        )
    )
    assertThat(weeks[1].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 6, false),
            Date(MONDAY, 7, false),
            Date(TUESDAY, 8, false),
            Date(WEDNESDAY, 9, false),
            Date(THURSDAY, 10, false),
            Date(FRIDAY, 11, false),
            Date(SATURDAY, 12, false)
        )
    )
    assertThat(weeks[2].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 13, false),
            Date(MONDAY, 14, false),
            Date(TUESDAY, 15, false),
            Date(WEDNESDAY, 16, false),
            Date(THURSDAY, 17, false),
            Date(FRIDAY, 18, false),
            Date(SATURDAY, 19, false)
        )
    )
    assertThat(weeks[3].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 20, false),
            Date(MONDAY, 21, false),
            Date(TUESDAY, 22, false),
            Date(WEDNESDAY, 23, false),
            Date(THURSDAY, 24, false),
            Date(FRIDAY, 25, false),
            Date(SATURDAY, 26, false)
        )
    )
    assertThat(weeks[4].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 27, false),
            Date(MONDAY, 28, false),
            Date(TUESDAY, 29, false),
            Date(WEDNESDAY, 30, false),
            Date(THURSDAY, 31, true),
            Date(FRIDAY, NO_DATE, false),
            Date(SATURDAY, NO_DATE, false)
        )
    )
    assertThat(weeks[5].dates).isEqualTo(
        listOf(
            Date(SUNDAY, NO_DATE, false),
            Date(MONDAY, NO_DATE, false),
            Date(TUESDAY, NO_DATE, false),
            Date(WEDNESDAY, NO_DATE, false),
            Date(THURSDAY, NO_DATE, false),
            Date(FRIDAY, NO_DATE, false),
            Date(SATURDAY, NO_DATE, false)
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

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(FRIDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(28)

    val weeks = graph.getWeeks()
    assertThat(weeks.size).isEqualTo(6)

    assertThat(weeks[0].dates).isEqualTo(
        listOf(
            Date(MONDAY, NO_DATE, false),
            Date(TUESDAY, NO_DATE, false),
            Date(WEDNESDAY, NO_DATE, false),
            Date(THURSDAY, NO_DATE, false),
            Date(FRIDAY, 1, false),
            Date(SATURDAY, 2, false),
            Date(SUNDAY, 3, false)
        )
    )
    assertThat(weeks[1].dates).isEqualTo(
        listOf(
            Date(MONDAY, 4, false),
            Date(TUESDAY, 5, false),
            Date(WEDNESDAY, 6, false),
            Date(THURSDAY, 7, false),
            Date(FRIDAY, 8, false),
            Date(SATURDAY, 9, false),
            Date(SUNDAY, 10, false)
        )
    )
    assertThat(weeks[2].dates).isEqualTo(
        listOf(
            Date(MONDAY, 11, false),
            Date(TUESDAY, 12, false),
            Date(WEDNESDAY, 13, false),
            Date(THURSDAY, 14, false),
            Date(FRIDAY, 15, false),
            Date(SATURDAY, 16, false),
            Date(SUNDAY, 17, false)
        )
    )
    assertThat(weeks[3].dates).isEqualTo(
        listOf(
            Date(MONDAY, 18, false),
            Date(TUESDAY, 19, false),
            Date(WEDNESDAY, 20, false),
            Date(THURSDAY, 21, false),
            Date(FRIDAY, 22, false),
            Date(SATURDAY, 23, false),
            Date(SUNDAY, 24, false)
        )
    )
    assertThat(weeks[4].dates).isEqualTo(
        listOf(
            Date(MONDAY, 25, false),
            Date(TUESDAY, 26, false),
            Date(WEDNESDAY, 27, false),
            Date(THURSDAY, 28, true),
            Date(FRIDAY, NO_DATE, false),
            Date(SATURDAY, NO_DATE, false),
            Date(SUNDAY, NO_DATE, false)
        )
    )
    assertThat(weeks[5].dates).isEqualTo(
        listOf(
            Date(MONDAY, NO_DATE, false),
            Date(TUESDAY, NO_DATE, false),
            Date(WEDNESDAY, NO_DATE, false),
            Date(THURSDAY, NO_DATE, false),
            Date(FRIDAY, NO_DATE, false),
            Date(SATURDAY, NO_DATE, false),
            Date(SUNDAY, NO_DATE, false)
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

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(SATURDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(30)

    val weeks = graph.getWeeks()
    assertThat(weeks.size).isEqualTo(6)

    assertThat(weeks[0].dates).isEqualTo(
        listOf(
            Date(SUNDAY, NO_DATE, false),
            Date(MONDAY, NO_DATE, false),
            Date(TUESDAY, NO_DATE, false),
            Date(WEDNESDAY, NO_DATE, false),
            Date(THURSDAY, NO_DATE, false),
            Date(FRIDAY, NO_DATE, false),
            Date(SATURDAY, 1, false)
        )
    )
    assertThat(weeks[1].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 2, false),
            Date(MONDAY, 3, false),
            Date(TUESDAY, 4, false),
            Date(WEDNESDAY, 5, false),
            Date(THURSDAY, 6, false),
            Date(FRIDAY, 7, false),
            Date(SATURDAY, 8, false)
        )
    )
    assertThat(weeks[2].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 9, false),
            Date(MONDAY, 10, false),
            Date(TUESDAY, 11, false),
            Date(WEDNESDAY, 12, false),
            Date(THURSDAY, 13, false),
            Date(FRIDAY, 14, false),
            Date(SATURDAY, 15, false)
        )
    )
    assertThat(weeks[3].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 16, false),
            Date(MONDAY, 17, false),
            Date(TUESDAY, 18, false),
            Date(WEDNESDAY, 19, false),
            Date(THURSDAY, 20, false),
            Date(FRIDAY, 21, false),
            Date(SATURDAY, 22, false)
        )
    )
    assertThat(weeks[4].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 23, false),
            Date(MONDAY, 24, false),
            Date(TUESDAY, 25, false),
            Date(WEDNESDAY, 26, false),
            Date(THURSDAY, 27, false),
            Date(FRIDAY, 28, false),
            Date(SATURDAY, 29, false)
        )
    )
    assertThat(weeks[5].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 30, true),
            Date(MONDAY, NO_DATE, false),
            Date(TUESDAY, NO_DATE, false),
            Date(WEDNESDAY, NO_DATE, false),
            Date(THURSDAY, NO_DATE, false),
            Date(FRIDAY, NO_DATE, false),
            Date(SATURDAY, NO_DATE, false)
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

    val graph = MonthGraph(calendar)
    assertThat(graph.firstWeekDayInMonth).isEqualTo(SATURDAY)
    assertThat(graph.orderedWeekDays).isEqualTo(
        listOf(SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY)
    )
    assertThat(graph.daysInMonth).isEqualTo(31)

    val weeks = graph.getWeeks()
    assertThat(weeks.size).isEqualTo(6)

    assertThat(weeks[0].dates).isEqualTo(
        listOf(
            Date(SUNDAY, NO_DATE, false),
            Date(MONDAY, NO_DATE, false),
            Date(TUESDAY, NO_DATE, false),
            Date(WEDNESDAY, NO_DATE, false),
            Date(THURSDAY, NO_DATE, false),
            Date(FRIDAY, NO_DATE, false),
            Date(SATURDAY, 1, false)
        )
    )
    assertThat(weeks[1].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 2, false),
            Date(MONDAY, 3, false),
            Date(TUESDAY, 4, false),
            Date(WEDNESDAY, 5, false),
            Date(THURSDAY, 6, false),
            Date(FRIDAY, 7, false),
            Date(SATURDAY, 8, false)
        )
    )
    assertThat(weeks[2].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 9, false),
            Date(MONDAY, 10, false),
            Date(TUESDAY, 11, false),
            Date(WEDNESDAY, 12, false),
            Date(THURSDAY, 13, false),
            Date(FRIDAY, 14, false),
            Date(SATURDAY, 15, false)
        )
    )
    assertThat(weeks[3].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 16, false),
            Date(MONDAY, 17, false),
            Date(TUESDAY, 18, false),
            Date(WEDNESDAY, 19, false),
            Date(THURSDAY, 20, false),
            Date(FRIDAY, 21, false),
            Date(SATURDAY, 22, false)
        )
    )
    assertThat(weeks[4].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 23, false),
            Date(MONDAY, 24, false),
            Date(TUESDAY, 25, false),
            Date(WEDNESDAY, 26, false),
            Date(THURSDAY, 27, false),
            Date(FRIDAY, 28, false),
            Date(SATURDAY, 29, false)
        )
    )
    assertThat(weeks[5].dates).isEqualTo(
        listOf(
            Date(SUNDAY, 30, false),
            Date(MONDAY, 31, true),
            Date(TUESDAY, NO_DATE, false),
            Date(WEDNESDAY, NO_DATE, false),
            Date(THURSDAY, NO_DATE, false),
            Date(FRIDAY, NO_DATE, false),
            Date(SATURDAY, NO_DATE, false)
        )
    )
  }
}
