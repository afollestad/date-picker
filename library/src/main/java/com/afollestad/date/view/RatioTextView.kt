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
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.AppCompatTextView
import com.afollestad.date.R
import com.afollestad.date.internal.getFloat

/** @author Aidan Follestad (@afollestad) */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class RatioTextView(
  context: Context,
  attrs: AttributeSet?
) : AppCompatTextView(context, attrs) {
  private val ratio: Float = context.getFloat(R.dimen.day_of_month_height_ratio)
  private var originalBackground: Drawable? = null
  private var lastInsetPadding: Int? = null

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = (width * ratio).toInt()
    setMeasuredDimension(width, height)
    invalidateBackground()
  }

  override fun setBackground(background: Drawable?) {
    originalBackground = background
    super.setBackground(originalBackground)
    invalidateBackground()
  }

  private fun invalidateBackground() {
    if (measuredWidth == 0) return
    val insetPadding = (measuredWidth - measuredHeight) / 2
    if (lastInsetPadding == insetPadding) return
    val currentBg: Drawable = originalBackground ?: return
    super.setBackground(InsetDrawable(currentBg, insetPadding, 0, insetPadding, 0))
    lastInsetPadding = insetPadding
  }
}
