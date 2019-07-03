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
package com.afollestad.date.data

import com.afollestad.date.data.SelectedDate.Companion.CURRENT_IS_LOW
import com.afollestad.date.data.SelectionMode.RANGE
import com.afollestad.date.data.SelectionMode.SINGLE
import com.afollestad.date.data.snapshot.snapshot
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class SelectedDateTest {
  private val now = GregorianCalendar(1995, Calendar.JULY, 28)
  private val anotherDate = GregorianCalendar(2019, Calendar.AUGUST, 7)
  private val selectedDate = SelectedDate()

  @Test fun `single selection mode`() {
    selectedDate.mode = SINGLE
    assertThat(selectedDate.current).isEqualTo(CURRENT_IS_LOW)

    val nowSnapshot = now.snapshot()
    selectedDate.set(nowSnapshot)
    assertThat(selectedDate.get()).isEqualTo(nowSnapshot)
    assertThat(selectedDate.getCalendar()!!.snapshot())
        .isEqualTo(nowSnapshot)
    assertThat(selectedDate.lowSnapshot).isEqualTo(nowSnapshot)
    assertThat(selectedDate.highSnapshot).isNull()

    val anotherDateSnapshot = anotherDate.snapshot()
    selectedDate.set(anotherDateSnapshot)
    assertThat(selectedDate.get()).isEqualTo(anotherDateSnapshot)
    assertThat(selectedDate.getCalendar()!!.snapshot())
        .isEqualTo(anotherDateSnapshot)
    assertThat(selectedDate.lowSnapshot).isEqualTo(anotherDateSnapshot)
    assertThat(selectedDate.highSnapshot).isNull()

    assertThat(selectedDate.getRange()).isNull()
  }

  @Test fun `range selection mode`() {
    selectedDate.mode = RANGE
    assertThat(selectedDate.current).isEqualTo(CURRENT_IS_LOW)

    val nowSnapshot = now.snapshot()
    selectedDate.set(nowSnapshot)
    assertThat(selectedDate.get()).isNull()
    assertThat(selectedDate.getCalendar()).isNull()
    assertThat(selectedDate.lowSnapshot).isEqualTo(nowSnapshot)
    assertThat(selectedDate.highSnapshot).isNull()

    val anotherDateSnapshot = anotherDate.snapshot()
    selectedDate.set(anotherDateSnapshot)
    assertThat(selectedDate.get()).isEqualTo(nowSnapshot)
    assertThat(selectedDate.getCalendar()!!.snapshot())
        .isEqualTo(nowSnapshot)
    assertThat(selectedDate.lowSnapshot).isEqualTo(nowSnapshot)
    assertThat(selectedDate.highSnapshot).isEqualTo(anotherDateSnapshot)

    assertThat(selectedDate.getRange()).isEqualTo(
        Pair(nowSnapshot, anotherDateSnapshot)
    )
  }
}
