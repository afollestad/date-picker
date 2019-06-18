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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Vibrator
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

/** @author Aidan Follestad (@afollestad) */
internal fun Context.resolveColor(
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
internal fun Context.hasVibratePermission(): Boolean {
  return ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.VIBRATE
  ) == PERMISSION_GRANTED
}

/** @author Aidan Follestad (@afollestad) */
internal fun Context.vibrator(): Vibrator {
  return getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
}
