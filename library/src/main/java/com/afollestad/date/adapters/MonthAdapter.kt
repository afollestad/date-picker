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

import android.graphics.Typeface
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.afollestad.date.R
import com.afollestad.date.data.DateFormatter
import com.afollestad.date.month
import com.afollestad.date.util.Util.createTextSelector
import com.afollestad.date.util.inflate
import com.afollestad.date.util.onClickDebounced
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal class MonthViewHolder(
  itemView: View,
  private val adapter: MonthAdapter
) : ViewHolder(itemView) {
  val textView = itemView as TextView

  init {
    itemView.onClickDebounced {
      adapter.onRowClicked(adapterPosition)
    }
  }
}

/** @author Aidan Follestad (@afollestad) */
internal class MonthAdapter(
  @ColorInt private val selectionColor: Int,
  private val normalFont: Typeface,
  private val mediumFont: Typeface,
  private val dateFormatter: DateFormatter,
  private val onSelection: (month: Int) -> Unit
) : RecyclerView.Adapter<MonthViewHolder>() {

  var selectedMonth: Int? = null
    set(value) {
      val lastSelectedMonth = field
      field = value
      if (lastSelectedMonth != null) {
        notifyItemChanged(lastSelectedMonth)
      }
      if (value != null) {
        notifyItemChanged(value)
      }
    }
  private val calendar = Calendar.getInstance()

  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long = position.toLong()

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): MonthViewHolder {
    val context = parent.context
    val view = parent.inflate(R.layout.year_list_row)
    return MonthViewHolder(view, this)
        .apply {
          textView.setTextColor(
              createTextSelector(context, selectionColor, overColoredBackground = false)
          )
        }
  }

  override fun getItemCount(): Int = calendar.getActualMaximum(Calendar.MONTH) + 1

  override fun onBindViewHolder(
    holder: MonthViewHolder,
    position: Int
  ) {
    val isSelected = position == selectedMonth
    val res = holder.itemView.context.resources

    holder.textView.text = position.nameOfMonth()
    holder.textView.isSelected = isSelected
    holder.textView.setTextSize(
        COMPLEX_UNIT_PX,
        res.getDimension(
            if (isSelected) {
              R.dimen.year_month_list_text_size_selected
            } else {
              R.dimen.year_month_list_text_size
            }
        )
    )
    holder.textView.typeface = if (isSelected) {
      mediumFont
    } else {
      normalFont
    }
  }

  internal fun onRowClicked(position: Int) {
    this.selectedMonth = position.also { onSelection(it) }
  }

  private fun Int.nameOfMonth(): String {
    calendar.month = this
    return dateFormatter.month(calendar)
  }
}
