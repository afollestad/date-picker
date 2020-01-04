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

import com.afollestad.date.data.DayOfWeek
import com.afollestad.date.data.andTheRest
import com.afollestad.date.data.asDayOfWeek
import com.afollestad.date.data.nextDayOfWeek
import com.google.common.truth.Truth.assertThat
import java.util.Calendar
import org.junit.Test

class DayOfWeekTest {

  @Test fun forValue() {
    assertThat(Calendar.SUNDAY.asDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY)
    assertThat(Calendar.MONDAY.asDayOfWeek()).isEqualTo(DayOfWeek.MONDAY)
    assertThat(Calendar.TUESDAY.asDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY)
    assertThat(Calendar.WEDNESDAY.asDayOfWeek()).isEqualTo(
        DayOfWeek.WEDNESDAY
    )
    assertThat(Calendar.THURSDAY.asDayOfWeek()).isEqualTo(
        DayOfWeek.THURSDAY
    )
    assertThat(Calendar.FRIDAY.asDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY)
    assertThat(Calendar.SATURDAY.asDayOfWeek()).isEqualTo(
        DayOfWeek.SATURDAY
    )
  }

  @Test fun restOfDays() {
    assertThat(DayOfWeek.MONDAY.andTheRest()).isEqualTo(
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        )
    )
    assertThat(DayOfWeek.SUNDAY.andTheRest()).isEqualTo(
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
    assertThat(DayOfWeek.SATURDAY.andTheRest()).isEqualTo(
        listOf(
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    )
    assertThat(DayOfWeek.WEDNESDAY.andTheRest()).isEqualTo(
        listOf(
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY
        )
    )
  }

  @Test fun nextDayOfWeek() {
    assertThat(DayOfWeek.SUNDAY.nextDayOfWeek()).isEqualTo(
        DayOfWeek.MONDAY)
    assertThat(DayOfWeek.MONDAY.nextDayOfWeek()).isEqualTo(
        DayOfWeek.TUESDAY)
    assertThat(DayOfWeek.TUESDAY.nextDayOfWeek()).isEqualTo(
        DayOfWeek.WEDNESDAY)
    assertThat(DayOfWeek.WEDNESDAY.nextDayOfWeek()).isEqualTo(
        DayOfWeek.THURSDAY)
    assertThat(DayOfWeek.THURSDAY.nextDayOfWeek()).isEqualTo(
        DayOfWeek.FRIDAY)
    assertThat(DayOfWeek.FRIDAY.nextDayOfWeek()).isEqualTo(
        DayOfWeek.SATURDAY)
    assertThat(DayOfWeek.SATURDAY.nextDayOfWeek()).isEqualTo(
        DayOfWeek.SUNDAY)
  }
}
