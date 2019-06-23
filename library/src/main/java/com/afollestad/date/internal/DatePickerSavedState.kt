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

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.view.View.BaseSavedState
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal class DatePickerSavedState : BaseSavedState {
  var selectedDate: Calendar? = null

  constructor(source: Parcel) : super(source) {
    selectedDate = source.readSerializable() as? Calendar
  }

  constructor(
    selectedDate: Calendar?,
    superState: Parcelable?
  ) : super(superState) {
    this.selectedDate = selectedDate
  }

  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    super.writeToParcel(parcel, flags)
    parcel.writeSerializable(selectedDate)
  }

  override fun describeContents(): Int = 0

  companion object CREATOR : Creator<DatePickerSavedState> {
    override fun createFromParcel(parcel: Parcel): DatePickerSavedState {
      return DatePickerSavedState(parcel)
    }

    override fun newArray(size: Int): Array<DatePickerSavedState?> {
      return arrayOfNulls(size)
    }
  }
}
