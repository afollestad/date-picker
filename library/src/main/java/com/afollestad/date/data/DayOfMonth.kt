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

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.data.snapshot.MonthSnapshot

/** The date of an empty date, a placeholder in the graph. */
internal const val NO_DATE: Int = -1

/** @author Aidan Follestad (@afollestad) */
internal sealed class MonthItem {

  /** @author Aidan Follestad (@afollestad) */
  internal data class WeekHeader(
    val dayOfWeek: DayOfWeek
  ) : MonthItem()

  /** @author Aidan Follestad (@afollestad) */
  internal data class DayOfMonth(
    val dayOfWeek: DayOfWeek,
    val month: MonthSnapshot,
    val date: Int = NO_DATE,
    val isSelected: Boolean = false,
    val isToday: Boolean = false
  ) : MonthItem()
}

/** @author Aidan Follestad (@afollestad) */
internal fun List<MonthItem>?.applyDiffTo(
  withNewDays: List<MonthItem>?,
  adapter: RecyclerView.Adapter<*>
) {
  if (this == null || withNewDays == null) {
    adapter.notifyDataSetChanged()
  } else {
    val diffResult = DiffUtil.calculateDiff(
        MonthItemCallback(
            oldItems = this,
            newItems = withNewDays
        )
    )
    diffResult.dispatchUpdatesTo(adapter)
  }
}
