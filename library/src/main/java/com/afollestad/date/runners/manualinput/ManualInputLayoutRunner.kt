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
package com.afollestad.date.runners.manualinput

import android.content.Context
import android.text.InputFilter.LengthFilter
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.View.MeasureSpec.getSize
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.R
import com.afollestad.date.data.snapshot.DateSnapshot
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.runners.Mode.INPUT_EDIT
import com.afollestad.date.runners.Mode.YEAR_LIST
import com.afollestad.date.runners.base.Bounds
import com.afollestad.date.runners.base.LayoutRunner
import com.afollestad.date.runners.base.Size
import com.afollestad.date.util.dimenPx
import com.afollestad.date.util.hideKeyboard
import com.afollestad.date.util.onTextChanged
import com.afollestad.date.util.placeAt
import com.afollestad.date.util.showKeyboard
import com.afollestad.date.util.showOrHide
import com.google.android.material.textfield.TextInputLayout

/** @author Aidan Follestad (@afollestad) */
internal class ManualInputLayoutRunner(
  context: Context,
  config: DatePickerConfig,
  root: ViewGroup,
  private val onDateInput: (CharSequence) -> Unit
) : LayoutRunner(context, config) {

  private val editModeInput: TextInputLayout = root.findViewById(R.id.edit_mode_input)
  private val inputMarginSides: Int = context.dimenPx(R.dimen.edit_mode_input_margin_sides)
  private val inputMarginTop: Int = context.dimenPx(R.dimen.edit_mode_input_margin_top)

  init {
    setupInput()
    config.currentMode.on { mode ->
      when (mode) {
        CALENDAR -> {
          editModeInput.showOrHide(false)
          editModeInput.editText?.hideKeyboard()
        }
        YEAR_LIST -> {
          editModeInput.showOrHide(false)
          editModeInput.editText?.hideKeyboard()
        }
        INPUT_EDIT -> {
          editModeInput.showOrHide(true)
          editModeInput.editText?.showKeyboard()
        }
      }
    }
  }

  fun setCurrentDate(selectedDate: DateSnapshot) {
    val inputDateString = selectedDate.asCalendar()
        .let { config.dateFormatter.inputDate(it) }
    editModeInput.editText?.setText(inputDateString)
    editModeInput.editText?.setSelection(inputDateString.length)
  }

  private fun setupInput() {
    editModeInput.hint = config.manualInputLabel
    editModeInput.editText?.apply {
      val pattern = config.dateFormatter.dateInputFormatter.toLocalizedPattern()
      hint = pattern
      filters += LengthFilter(pattern.length)
      onTextChanged(requiredLength = pattern.length) { onDateInput(it) }
    }
  }

  override fun measure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int,
    totalHeightSoFar: Int
  ): Size {
    val parentWidth: Int = getSize(widthMeasureSpec)
    val totalMargin = (inputMarginSides * 2)
    val inputWidth = (getNonHeadersWidth(parentWidth) - totalMargin)

    editModeInput.measure(
        makeMeasureSpec(inputWidth, EXACTLY),
        makeMeasureSpec(0, UNSPECIFIED)
    )

    return size.apply {
      width = inputWidth
      height = (inputMarginTop + editModeInput.measuredHeight)
    }
  }

  override fun layout(
    top: Int,
    left: Int,
    right: Int,
    parentWidth: Int
  ): Bounds {
    editModeInput.placeAt(
        top = top + inputMarginTop,
        left = left + inputMarginSides
    )

    return bounds.apply {
      this.top = top
      this.left = left
      this.right = right
      this.bottom = top + editModeInput.bottom
    }
  }
}
