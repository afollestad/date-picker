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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

class DateSnapshotTest {
  private val now = DateSnapshot(Calendar.JULY, 28, 2019)

  @Test fun before_true() {
    // month
    assertThat(now.isBefore(DateSnapshot(Calendar.AUGUST, 28, 2019))).isTrue()
    // day
    assertThat(now.isBefore(DateSnapshot(Calendar.JULY, 29, 2019))).isTrue()
    // year
    assertThat(now.isBefore(DateSnapshot(Calendar.JULY, 28, 2020))).isTrue()
  }

  @Test fun before_false() {
    // month
    assertThat(now.isBefore(DateSnapshot(Calendar.JUNE, 28, 2019))).isFalse()
    // day
    assertThat(now.isBefore(DateSnapshot(Calendar.JULY, 27, 2019))).isFalse()
    // year
    assertThat(now.isBefore(DateSnapshot(Calendar.JULY, 28, 2018))).isFalse()
  }

  @Test fun after_true() {
    // month
    assertThat(now.isAfter(DateSnapshot(Calendar.JUNE, 28, 2019))).isTrue()
    // day
    assertThat(now.isAfter(DateSnapshot(Calendar.JULY, 27, 2019))).isTrue()
    // year
    assertThat(now.isAfter(DateSnapshot(Calendar.JULY, 28, 2018))).isTrue()
  }

  @Test fun after_false() {
    // month
    assertThat(now.isAfter(DateSnapshot(Calendar.AUGUST, 28, 2019))).isFalse()
    // day
    assertThat(now.isAfter(DateSnapshot(Calendar.JULY, 29, 2019))).isFalse()
    // year
    assertThat(now.isAfter(DateSnapshot(Calendar.JULY, 28, 2020))).isFalse()
  }
}
