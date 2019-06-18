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

import android.graphics.Typeface
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.afollestad.date.R
import com.afollestad.date.internal.Util.createTextSelector
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal class YearViewHolder(
  itemView: View,
  private val adapter: YearAdapter
) : ViewHolder(itemView) {
  val textView = itemView as TextView

  init {
    itemView.onClickDebounced {
      adapter.onRowClicked(adapterPosition)
    }
  }
}

/** @author Aidan Follestad (@afollestad) */
internal class YearAdapter(
  @ColorInt private val selectionColor: Int,
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
  private val yearRange: Pair<Int, Int> = Calendar.getInstance()
      .year
      .visibleYearRange()

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = position.asYear().toLong()

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): YearViewHolder {
    val context = parent.context
    val view = LayoutInflater.from(context)
        .inflate(R.layout.year_list_row, parent, false)
    return YearViewHolder(view, this).apply {
      textView.setTextColor(
          createTextSelector(context, selectionColor, overColoredBackground = false)
      )
    }
  }

  override fun getItemCount(): Int = yearRange.second - yearRange.first

  override fun onBindViewHolder(
    holder: YearViewHolder,
    position: Int
  ) {
    val currentYear = position.asYear()
    val isSelected = currentYear == selectedYear
    val res = holder.itemView.context.resources

    holder.textView.text = currentYear.toString()
    holder.textView.isSelected = isSelected
    holder.textView.setTextSize(
        COMPLEX_UNIT_PX,
        res.getDimension(
            if (isSelected) {
              R.dimen.year_list_text_size_selected
            } else {
              R.dimen.year_list_text_size
            }
        )
    )
    holder.textView.typeface = if (isSelected) {
      Typeface.DEFAULT_BOLD
    } else {
      Typeface.SANS_SERIF
    }
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
