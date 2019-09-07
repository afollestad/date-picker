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
package com.afollestad.date.view

import android.content.Context
import android.util.AttributeSet
import android.view.View.MeasureSpec.EXACTLY
import android.widget.FrameLayout
import android.widget.TextView
import com.afollestad.date.R
import com.afollestad.date.util.dimenPx
import com.afollestad.date.util.getFloat

/** @author Aidan Follestad (@afollestad) */
internal class DayOfMonthRootView(
  context: Context,
  attrs: AttributeSet?
) : FrameLayout(context, attrs) {
  private val ratio: Float = context.getFloat(R.dimen.day_of_month_height_ratio)
  private val circleInset: Int = context.dimenPx(R.dimen.day_of_month_circle_inset)
  private lateinit var textView: TextView

  override fun onFinishInflate() {
    super.onFinishInflate()
    textView = getChildAt(0) as TextView
  }

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = (width * ratio).toInt()
    setMeasuredDimension(width, height)

    val circleSize = height - (circleInset * 2)
    textView.measure(
        MeasureSpec.makeMeasureSpec(circleSize, EXACTLY),
        MeasureSpec.makeMeasureSpec(circleSize, EXACTLY)
    )
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    textView.isEnabled = enabled
  }
}
