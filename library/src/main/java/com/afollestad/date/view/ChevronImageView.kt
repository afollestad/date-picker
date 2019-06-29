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
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.afollestad.date.util.waitForLayout

/** @author Aidan Follestad (@afollestad) */
internal class ChevronImageView(
  context: Context,
  attrs: AttributeSet?
) : AppCompatImageView(context, attrs) {
  private var targetWidth: Int? = null

  fun attach(view: View) {
    view.waitForLayout { width ->
      targetWidth = width / DAYS_IN_WEEK
      requestLayout()
    }
  }

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    if (targetWidth != null) {
      setMeasuredDimension(targetWidth!!, targetWidth!!)
      return
    }
    val widthAndHeight = MeasureSpec.getSize(widthMeasureSpec)
    setMeasuredDimension(widthAndHeight, widthAndHeight)
  }

  private companion object {
    const val DAYS_IN_WEEK = 7
  }
}
