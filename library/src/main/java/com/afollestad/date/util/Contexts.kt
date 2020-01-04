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
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

/** @author Aidan Follestad (@afollestad) */
@CheckResult internal fun Context.resolveColor(
  @AttrRes attr: Int,
  fallback: (() -> Int)? = null
): Int {
  val a = theme.obtainStyledAttributes(intArrayOf(attr))
  try {
    val result = a.getColor(0, 0)
    if (result == 0 && fallback != null) {
      return fallback()
    }
    return result
  } finally {
    a.recycle()
  }
}

/** @author Aidan Follestad (@afollestad) */
@CheckResult @Px internal fun Context.dimenPx(@DimenRes dimen: Int): Int {
  return resources.getDimensionPixelSize(dimen)
}

/** @author Aidan Follestad (@afollestad) */
@CheckResult internal fun Context.drawable(
  @DrawableRes drawable: Int,
  @ColorInt tint: Int? = null
): Drawable {
  val result = ContextCompat.getDrawable(this, drawable)!!
  if (tint != null) {
    return DrawableCompat.wrap(result)
        .apply { DrawableCompat.setTint(this, tint) }
  }
  return result
}

/** @author Aidan Follestad (@afollestad) */
@CheckResult internal fun Context.getFloat(@DimenRes dimen: Int): Float {
  return TypedValue().apply { resources.getValue(dimen, this, true) }
      .float
}
