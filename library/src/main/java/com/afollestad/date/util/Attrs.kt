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

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.core.content.res.ResourcesCompat

/** @author Aidan Follestad (@afollestad) */
@ColorInt internal fun TypedArray.color(
  @StyleableRes attr: Int,
  fallback: () -> Int
): Int {
  val colorValue = getColor(attr, 0)
  return if (colorValue == 0) fallback() else colorValue
}

/** @author Aidan Follestad (@afollestad) */
internal fun TypedArray.font(
  context: Context,
  @StyleableRes attr: Int,
  fallback: () -> Typeface
): Typeface {
  val resId = getResourceId(attr, 0)
  return if (resId == 0) {
    fallback()
  } else {
    ResourcesCompat.getFont(context, resId) ?: fallback()
  }
}

/** @author Aidan Follestad (@afollestad) */
internal fun TypedArray.string(
  context: Context,
  @StyleableRes attr: Int,
  fallback: () -> String
): String {
  val resId = getResourceId(attr, 0)
  return if (resId == 0) {
    fallback()
  } else {
    context.resources.getString(resId)
  }
}
