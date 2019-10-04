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

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.R
import com.afollestad.date.util.Util.createRoundedRectangleSelector
import com.afollestad.date.util.inflate
import com.afollestad.date.util.onClickDebounced
import com.afollestad.date.year
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal class YearViewHolder(
  itemView: ViewGroup,
  private val adapter: YearAdapter
) : ViewHolder(itemView) {
  val textView = itemView.getChildAt(0) as TextView

  init {
    itemView.onClickDebounced {
      adapter.onRowClicked(adapterPosition)
    }
  }
}

/** @author Aidan Follestad (@afollestad) */
internal class YearAdapter(
  private val config: DatePickerConfig,
  private val onSelection: (year: Int) -> Unit
) : RecyclerView.Adapter<YearViewHolder>() {

  var selectedYear: Int? = null
    set(value) {
      val lastSelectedYear = field
      field = value
      if (lastSelectedYear != null) {
        notifyItemChanged(lastSelectedYear.asPosition())
      }
      if (value != null) {
        notifyItemChanged(value.asPosition())
      }
    }
  private val yearRange: Pair<Int, Int> = with(Calendar.getInstance().year) {
    Pair(this - 100, this + 100)
  }

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = position.asYear().toLong()

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): YearViewHolder {
    val context = parent.context
    val view = parent.inflate<ViewGroup>(R.layout.year_list_row)
    return YearViewHolder(view, this).apply {
      textView.background = createRoundedRectangleSelector(context, config.selectionColor)
    }
  }

  override fun getItemCount(): Int = yearRange.second - yearRange.first

  override fun onBindViewHolder(
    holder: YearViewHolder,
    position: Int
  ) {
    val currentYear = position.asYear()
    val isSelected = currentYear == selectedYear

    holder.itemView.isSelected = isSelected
    holder.textView.text = currentYear.toString()
    holder.textView.typeface = config.normalFont
  }

  fun getSelectedPosition(): Int? = selectedYear?.asPosition()

  internal fun onRowClicked(position: Int) {
    this.selectedYear = position.asYear()
        .also { onSelection(it) }
  }

  /**
   * Gets the index that receiver, representing a year, should exist at in the list. If the base
   * year was 1900, and the given year was 2000, the position would be 99 (since position 1 is index 0).
   */
  private fun Int.asPosition(): Int {
    val base = yearRange.first
    return this - base - 1
  }

  /**
   * Gets the year that the receiver, representing a position, is equal to. This is
   * basically the inverse of [asPosition].
   */
  private fun Int.asYear(): Int {
    val base = yearRange.first
    return this + 1 + base
  }
}
