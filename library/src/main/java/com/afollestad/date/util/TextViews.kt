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

import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.TextView
import androidx.annotation.Px

/** @author Aidan Follestad (@afollestad) */
internal fun TextView.setCompoundDrawablesCompat(
  start: Drawable? = null,
  top: Drawable? = null,
  end: Drawable? = null,
  bottom: Drawable? = null
) {
  if (Build.VERSION.SDK_INT >= 17) {
    setCompoundDrawablesRelative(
        start?.bound(start.intrinsicWidth),
        top?.bound(top.intrinsicWidth),
        end?.bound(end.intrinsicWidth),
        bottom?.bound(bottom.intrinsicWidth)
    )
  } else {
    setCompoundDrawables(
        start?.bound(start.intrinsicWidth),
        top?.bound(top.intrinsicWidth),
        end?.bound(end.intrinsicWidth),
        bottom?.bound(bottom.intrinsicWidth)
    )
  }
}

/** @author Aidan Follestad (@afollestad) */
private fun Drawable.bound(@Px size: Int): Drawable {
  return apply { setBounds(0, 0, size, size) }
}
