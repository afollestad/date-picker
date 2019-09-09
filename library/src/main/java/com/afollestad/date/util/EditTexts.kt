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

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText

/** @author Aidan Follestad (@afollestad) */
internal fun EditText.showKeyboard() {
  post {
    requestFocus()
    val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, SHOW_IMPLICIT)
  }
}

/** @author Aidan Follestad (@afollestad) */
internal fun EditText.hideKeyboard() {
  val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
  imm.hideSoftInputFromWindow(windowToken, 0)
}

/** @author Aidan Follestad (@afollestad) */
internal fun EditText.onTextChanged(
  debounceMs: Long = 150,
  requiredLength: Int? = null,
  block: (input: CharSequence) -> Unit
) {
  val textWatcher = DebouncedTextWatcher(
      debounceMs = debounceMs,
      requiredLength = requiredLength,
      block = block
  )
  addTextChangedListener(textWatcher)
  addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
    override fun onViewDetachedFromWindow(view: View) {
      removeTextChangedListener(textWatcher)
      textWatcher.dispose()
    }

    override fun onViewAttachedToWindow(view: View) = Unit
  })
}

/** @author Aidan Follestad (@afollestad) */
private class DebouncedTextWatcher(
  private val debounceMs: Long = 0,
  private val requiredLength: Int? = null,
  private val block: (input: CharSequence) -> Unit
) : TextWatcher {
  private val handler = Handler()
  private var scheduledEvent: Runnable? = null

  override fun afterTextChanged(p: Editable) = Unit

  override fun beforeTextChanged(
    p0: CharSequence,
    start: Int,
    count: Int,
    after: Int
  ) = Unit

  override fun onTextChanged(
    text: CharSequence,
    start: Int,
    before: Int,
    count: Int
  ) {
    if (requiredLength != null && text.length < requiredLength) return
    scheduledEvent = Runnable { block(text) }
    handler.postDelayed(scheduledEvent!!, debounceMs)
  }

  fun dispose() {
    scheduledEvent?.let { handler.removeCallbacks(it) }
    scheduledEvent = null
  }
}
