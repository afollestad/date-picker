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

import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE

/** @author Aidan Follestad (@afollestad) */
internal fun View.show() {
  visibility = VISIBLE
}

/** @author Aidan Follestad (@afollestad) */
internal fun View.hide() {
  visibility = GONE
}

/** @author Aidan Follestad (@afollestad) */
internal fun View.conceal() {
  visibility = INVISIBLE
}

/** @author Aidan Follestad (@afollestad) */
internal fun List<View>.concealAll() = forEach { it.conceal() }

/** @author Aidan Follestad (@afollestad) */
internal fun List<View>.showAll() = forEach { it.show() }

/** @author Aidan Follestad (@afollestad) */
internal fun View.showOrHide(show: Boolean) = if (show) show() else hide()

internal fun View.showOrConceal(show: Boolean) = if (show) show() else conceal()

/** @author Aidan Follestad (@afollestad) */
internal fun View.isVisible(): Boolean = visibility == VISIBLE

/** @author Aidan Follestad (@afollestad) */
internal fun View.isConcealed(): Boolean = visibility == INVISIBLE
