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
import com.afollestad.date.dayOfMonth
import com.afollestad.date.dayOfWeek
import com.afollestad.date.decrementMonth
import com.afollestad.date.incrementMonth
import com.afollestad.date.month
import com.afollestad.date.totalDaysInMonth
import com.afollestad.date.year
import com.google.common.truth.Truth.assertThat
import java.util.Calendar
import java.util.GregorianCalendar
import org.junit.Before
import org.junit.Test

class CalendarsTest {
  private lateinit var calendar: Calendar

  @Before fun setup() {
    calendar = GregorianCalendar(
        2019,
        Calendar.JULY,
        28
    )
  }

  @Test fun year() {
    assertThat(calendar.year).isEqualTo(2019)
  }

  @Test fun month() {
    assertThat(calendar.month).isEqualTo(Calendar.JULY)
  }

  @Test fun dayOfMonth() {
    assertThat(calendar.dayOfMonth).isEqualTo(28)
  }

  @Test fun totalDaysInMonth() {
    assertThat(calendar.totalDaysInMonth).isEqualTo(31)
  }

  @Test fun incrementMonth() {
    val result = calendar.incrementMonth()
    assertThat(result).isNotSameInstanceAs(calendar)
    assertThat(result.dayOfMonth).isEqualTo(1)
    assertThat(result.month).isEqualTo(Calendar.AUGUST)
    assertThat(result.year).isEqualTo(2019)
  }

  @Test fun decrementMonth() {
    val result = calendar.decrementMonth()
    assertThat(result).isNotSameInstanceAs(calendar)
    assertThat(result.dayOfMonth).isEqualTo(30)
    assertThat(result.month).isEqualTo(Calendar.JUNE)
    assertThat(result.year).isEqualTo(2019)
  }

  @Test fun dayOfWeek() {
    assertThat(calendar.dayOfWeek).isEqualTo(DayOfWeek.SUNDAY)
  }
}
