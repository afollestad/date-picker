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
package com.afollestad.date.runners.base

import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.annotation.CheckResult
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.R
import com.afollestad.date.runners.base.Orientation.PORTRAIT

/** @author Aidan Follestad (@afollestad) */
internal abstract class LayoutRunner(
  context: Context,
  protected val config: DatePickerConfig
) {
  protected val orientation = Orientation.get(context)
  protected val bounds: Bounds = Bounds()
  protected val size: Size = Size()

  private val headersWidthFactor: Int =
    context.resources.getInteger(R.integer.headers_width_factor)

  @CheckResult abstract fun measure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
    totalHeightSoFar: Int
  ): Size

  @CheckResult abstract fun layout(
    top: Int,
    left: Int,
    right: Int,
    parentWidth: Int
  ): Bounds

  @CheckResult fun getHeadersWidth(parentWidth: Int): Int {
    return (parentWidth / headersWidthFactor)
  }

  @CheckResult fun getNonHeadersWidth(parentWidth: Int): Int {
    return if (orientation == PORTRAIT) {
      parentWidth
    } else {
      parentWidth - getHeadersWidth(parentWidth)
    }
  }

  @CheckResult fun getRecyclerViewWidth(parentWidth: Int): Int {
    val nonHeadersWidth = getNonHeadersWidth(parentWidth)
    return (nonHeadersWidth - (config.horizontalPadding * 2))
  }

  companion object
}

/** @author Aidan Follestad (@afollestad) */
internal enum class Orientation {
  PORTRAIT,
  LANDSCAPE;

  companion object {
    fun get(context: Context): Orientation {
      return if (context.resources.configuration.orientation == ORIENTATION_PORTRAIT) {
        PORTRAIT
      } else {
        LANDSCAPE
      }
    }
  }
}

/** @author Aidan Follestad (@afollestad) */
internal data class Size(
  var width: Int = -1,
  var height: Int = -1
)

/** @author Aidan Follestad (@afollestad) */
internal data class Bounds(
  var top: Int = 0,
  var left: Int = 0,
  var right: Int = -1,
  var bottom: Int = -1
)
