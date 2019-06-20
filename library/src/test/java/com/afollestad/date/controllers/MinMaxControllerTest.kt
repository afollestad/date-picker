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

import com.afollestad.date.assertException
import com.afollestad.date.snapshot.DateSnapshot
import com.afollestad.date.snapshot.snapshot
import com.afollestad.date.totalDaysInMonth
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

class MinMaxControllerTest {
  private val now = DateSnapshot(Calendar.JULY, 28, 2019)
  private val controller = MinMaxController()

  @Test fun setMinDate() {
    val snapshot = DateSnapshot(Calendar.JULY, 28, 1995)
    controller.setMinDate(snapshot.asCalendar())
    assertThat(controller.getMinDate()!!.snapshot()).isEqualTo(snapshot)
  }

  @Test fun `setMinDate - manually`() {
    val snapshot = DateSnapshot(Calendar.JULY, 28, 1995)
    controller.setMinDate(snapshot.year, snapshot.month, snapshot.day)
    assertThat(controller.getMinDate()!!.snapshot()).isEqualTo(snapshot)
  }

  @Test fun `setMinDate - with date that is greater than or equal to max date`() {
    controller.setMaxDate(1995, Calendar.JULY, 28)
    assertException(IllegalStateException::class) {
      controller.setMinDate(1995, Calendar.JULY, 29)
    }
  }

  @Test fun setMaxDate() {
    val snapshot = DateSnapshot(Calendar.JULY, 28, 1995)
    controller.setMaxDate(snapshot.asCalendar())
    assertThat(controller.getMaxDate()!!.snapshot()).isEqualTo(snapshot)
  }

  @Test fun `setMaxDate - manually`() {
    val snapshot = DateSnapshot(Calendar.JULY, 28, 1995)
    controller.setMaxDate(snapshot.year, snapshot.month, snapshot.day)
    assertThat(controller.getMaxDate()!!.snapshot()).isEqualTo(snapshot)
  }

  @Test fun `setMaxDate - with date that is less than or equal to min date`() {
    controller.setMinDate(1995, Calendar.JULY, 28)
    assertException(IllegalStateException::class) {
      controller.setMaxDate(1995, Calendar.JULY, 27)
    }
  }

  @Test fun `isOutOfMinRange - true`() {
    controller.minDate = now
    // month
    assertThat(controller.isOutOfMinRange(DateSnapshot(Calendar.JUNE, 28, 2019))).isTrue()
    // day
    assertThat(controller.isOutOfMinRange(DateSnapshot(Calendar.JULY, 27, 2019))).isTrue()
    // year
    assertThat(controller.isOutOfMinRange(DateSnapshot(Calendar.JULY, 28, 2018))).isTrue()
  }

  @Test fun `isOutOfMinRange - false`() {
    controller.minDate = now
    // month
    assertThat(controller.isOutOfMinRange(DateSnapshot(Calendar.AUGUST, 28, 2019))).isFalse()
    // day
    assertThat(controller.isOutOfMinRange(DateSnapshot(Calendar.JULY, 29, 2019))).isFalse()
    // year
    assertThat(controller.isOutOfMinRange(DateSnapshot(Calendar.JULY, 28, 2020))).isFalse()
  }

  @Test fun getOutOfMinRangeBackgroundRes() {
    controller.minDate = now
    // First day of month
    controller.getOutOfMinRangeBackgroundRes(now.copy(day = 1))
    // Day before today
    controller.getOutOfMinRangeBackgroundRes(now.copy(day = now.day - 1))
    // Different month
    controller.getOutOfMinRangeBackgroundRes(now.copy(month = now.month - 1))
    // Else
    controller.getOutOfMinRangeBackgroundRes(now.copy(day = 26, year = 2000))
  }

  @Test fun `isOutOfMaxRange - true`() {
    controller.maxDate = now
    // month
    assertThat(controller.isOutOfMaxRange(DateSnapshot(Calendar.AUGUST, 28, 2019))).isTrue()
    // day
    assertThat(controller.isOutOfMaxRange(DateSnapshot(Calendar.JULY, 29, 2019))).isTrue()
    // year
    assertThat(controller.isOutOfMaxRange(DateSnapshot(Calendar.JULY, 28, 2020))).isTrue()
  }

  @Test fun `isOutOfMaxRange - false`() {
    controller.maxDate = now
    // month
    assertThat(controller.isOutOfMaxRange(DateSnapshot(Calendar.JUNE, 28, 2019))).isFalse()
    // day
    assertThat(controller.isOutOfMaxRange(DateSnapshot(Calendar.JULY, 27, 2019))).isFalse()
    // year
    assertThat(controller.isOutOfMaxRange(DateSnapshot(Calendar.JULY, 28, 2018))).isFalse()
  }

  @Test fun getOutOfMaxRangeBackgroundRes() {
    controller.maxDate = now
    // Last day of month
    controller.getOutOfMaxRangeBackgroundRes(now.copy(day = now.asCalendar().totalDaysInMonth))
    // Day after today
    controller.getOutOfMaxRangeBackgroundRes(now.copy(day = now.day + 1))
    // Different month
    controller.getOutOfMaxRangeBackgroundRes(now.copy(month = now.month + 1))
    // Else
    controller.getOutOfMaxRangeBackgroundRes(now.copy(day = 26, year = 2000))
  }
}
