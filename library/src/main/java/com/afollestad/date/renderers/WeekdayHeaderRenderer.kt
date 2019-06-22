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
package com.afollestad.date.renderers

import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import com.afollestad.date.internal.DayOfWeek

// TODO write unit tests
/** @author Aidan Follestad (@afollestad) */
internal class WeekdayHeaderRenderer(
  private val normalFont: Typeface
) {

  @VisibleForTesting fun render(
    dayOfWeek: DayOfWeek,
    view: TextView
  ) {
    view.typeface = normalFont
    view.text = dayOfWeek.name.first()
        .toString()
  }

  fun renderAll(
    daysOfWeek: List<DayOfWeek>,
    views: List<TextView>
  ) {
    require(daysOfWeek.size == views.size) {
      "Days of week size (${daysOfWeek.size}) should equal views size (${views.size})."
    }
    daysOfWeek.forEachIndexed { index, dayOfWeek ->
      render(dayOfWeek, views[index])
    }
  }
}
