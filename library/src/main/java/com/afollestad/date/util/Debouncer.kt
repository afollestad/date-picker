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

import android.view.View

/** @author Aidan Follestad (@afollestad) */
internal object Debouncer {
  @Volatile private var enabled: Boolean = true
  private val enableAgain = Runnable { enabled = true }

  fun canPerform(view: View): Boolean {
    if (enabled) {
      enabled = false
      view.post(enableAgain)
      return true
    }
    return false
  }
}

/** @author Aidan Follestad (@afollestad) */
internal fun <T : View> T.onClickDebounced(click: (view: T) -> Unit): T {
  setOnClickListener {
    if (Debouncer.canPerform(it)) {
      @Suppress("UNCHECKED_CAST")
      click(it as T)
    }
  }
  return this
}
