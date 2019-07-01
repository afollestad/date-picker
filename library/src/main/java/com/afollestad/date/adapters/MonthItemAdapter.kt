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
package com.afollestad.date.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.afollestad.date.R
import com.afollestad.date.data.MonthItem
import com.afollestad.date.data.MonthItem.DayOfMonth
import com.afollestad.date.data.MonthItem.WeekHeader
import com.afollestad.date.data.applyDiffTo
import com.afollestad.date.renderers.MonthItemRenderer
import com.afollestad.date.util.inflate

/** @author Aidan Follestad (@afollestad) */
internal class MonthItemViewHolder(
  itemView: View
) : ViewHolder(itemView) {
  val textView: TextView = itemView.findViewById(R.id.textView)
}

/** @author Aidan Follestad (@afollestad) */
internal class MonthItemAdapter(
  private val itemRenderer: MonthItemRenderer,
  private val onSelection: (day: DayOfMonth) -> Unit
) : RecyclerView.Adapter<MonthItemViewHolder>() {

  var items: List<MonthItem>? = null
    set(value) {
      val oldDays = field
      field = value
      oldDays.applyDiffTo(value, this)
    }

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getItemViewType(position: Int): Int {
    return if (items?.get(position) is WeekHeader) {
      R.layout.month_grid_header
    } else {
      R.layout.month_grid_item
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): MonthItemViewHolder {
    return MonthItemViewHolder(parent.inflate(viewType))
  }

  override fun getItemCount(): Int = items?.size ?: 0

  override fun onBindViewHolder(
    holder: MonthItemViewHolder,
    position: Int
  ) {
    val item = items?.get(position) ?: error("Impossible!")
    itemRenderer.render(item, holder.itemView, holder.textView, onSelection)
  }
}
