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

import androidx.recyclerview.widget.DiffUtil
import com.afollestad.date.internal.MonthItem.DayOfMonth
import com.afollestad.date.internal.MonthItem.WeekHeader

/** @author Aidan Follestad (@afollestad) */
internal class MonthItemCallback(
  private val oldItems: List<MonthItem>,
  private val newItems: List<MonthItem>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(
    oldItemPosition: Int,
    newItemPosition: Int
  ): Boolean {
    val oldItem: MonthItem = oldItems[oldItemPosition]
    val newItem: MonthItem = newItems[newItemPosition]
    return when {
      oldItem is WeekHeader && newItem is WeekHeader -> {
        // Same week day?
        oldItem.dayOfWeek == newItem.dayOfWeek
      }
      oldItem is DayOfMonth && newItem is DayOfMonth -> {
        // Same date of same month?
        oldItem.month == newItem.month && oldItem.date == newItem.date
      }
      else -> false
    }
  }

  override fun areContentsTheSame(
    oldItemPosition: Int,
    newItemPosition: Int
  ): Boolean {
    val oldItem = oldItems[oldItemPosition]
    val newItem = newItems[newItemPosition]
    return when {
      oldItem is WeekHeader && newItem is WeekHeader -> {
        // Same week day?
        oldItem.dayOfWeek == newItem.dayOfWeek
      }
      oldItem is DayOfMonth && newItem is DayOfMonth -> {
        // Same date of same month AND selected state is the same?
        oldItem.month == newItem.month &&
            oldItem.date == newItem.date &&
            oldItem.isSelected == newItem.isSelected
      }
      else -> false
    }
  }

  override fun getOldListSize(): Int = oldItems.size

  override fun getNewListSize(): Int = newItems.size
}
