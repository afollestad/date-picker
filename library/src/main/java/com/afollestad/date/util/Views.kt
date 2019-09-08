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
package com.afollestad.date.util

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.LayoutRes

/** @author Aidan Follestad (@afollestad) */
internal fun View.show() {
  if (visibility == VISIBLE) return
  visibility = VISIBLE
}

/** @author Aidan Follestad (@afollestad) */
internal fun View.hide() {
  if (visibility == GONE) return
  visibility = GONE
}

/** @author Aidan Follestad (@afollestad) */
internal fun View.conceal() {
  if (visibility == INVISIBLE) return
  visibility = INVISIBLE
}

/** @author Aidan Follestad (@afollestad) */
internal fun View.showOrHide(show: Boolean) = if (show) show() else hide()

internal fun View.showOrConceal(show: Boolean) = if (show) show() else conceal()

/** @author Aidan Follestad (@afollestad) */
internal fun View.isVisible(): Boolean = visibility == VISIBLE

/** @author Aidan Follestad (@afollestad) */
internal inline fun <reified T> ViewGroup.inflate(@LayoutRes res: Int): T {
  return LayoutInflater.from(context).inflate(res, this, false) as T
}

/** @author Aidan Follestad (@afollestad) */
internal fun View.updatePadding(
  left: Int = paddingLeft,
  top: Int = paddingTop,
  right: Int = paddingRight,
  bottom: Int = paddingBottom
) {
  setPadding(left, top, right, bottom)
}

/** @author Aidan Follestad (@afollestad) */
internal fun View.placeAt(
  top: Int,
  left: Int = 0,
  rightOffset: Int = measuredWidth,
  bottomOffset: Int = measuredHeight
) {
//  if (isGone()) return
  layout(
      /* left=   */left,
      /* top=    */top,
      /* right=  */left + rightOffset,
      /* bottom= */top + bottomOffset
  )
}
