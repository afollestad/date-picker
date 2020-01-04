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

import com.afollestad.date.data.snapshot.DateSnapshot
import com.google.common.truth.Truth.assertThat
import java.util.Calendar
import org.junit.Test

class DateSnapshotTest {
  private val date = DateSnapshot(Calendar.JULY, 28, 1995)

  @Test fun `compare - same`() {
    assertThat(date == DateSnapshot(Calendar.JULY, 28, 1995)).isTrue()
    assertThat(date.compareTo(DateSnapshot(Calendar.JULY, 28, 1995))).isEqualTo(0)
  }

  @Test fun `compare - less than`() {
    // Day
    assertThat(date < DateSnapshot(Calendar.JULY, 29, 1995)).isTrue()
    // Month
    assertThat(date < DateSnapshot(Calendar.AUGUST, 28, 1995)).isTrue()
    // Year
    assertThat(date < DateSnapshot(Calendar.JULY, 28, 1996)).isTrue()
  }

  @Test fun `compare - greater than`() {
    // Day
    assertThat(date > DateSnapshot(Calendar.JULY, 27, 1995)).isTrue()
    // Month
    assertThat(date > DateSnapshot(Calendar.JUNE, 28, 1995)).isTrue()
    // Year
    assertThat(date > DateSnapshot(Calendar.JULY, 28, 1994)).isTrue()
  }
}
