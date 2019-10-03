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

import androidx.annotation.CheckResult
import com.afollestad.date.runners.Mode
import com.afollestad.date.runners.Mode.CALENDAR
import com.afollestad.date.runners.Mode.INPUT_EDIT
import com.afollestad.date.runners.Mode.YEAR_LIST
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** @author Aidan Follestad (@afollestad) */
internal fun ObservableValue<Mode>.toggleMode(wantedMode: Mode = YEAR_LIST) {
  when (get()) {
    CALENDAR -> set(wantedMode)
    INPUT_EDIT -> set(CALENDAR)
    else -> set(CALENDAR)
  }
}

/** @author Aidan Follestad (@afollestad) */
internal typealias Observer<T> = (value: T) -> Unit

/** @author Aidan Follestad (@afollestad) */
internal class ObservableValue<T : Any>(initialValue: T) : ReadWriteProperty<Any, T> {
  private var currentValue: T = initialValue
  private val observers = mutableSetOf<Observer<T>>()

  fun on(observer: Observer<T>): ObservableValue<T> {
    observers.add(observer)
    return this
  }

  @Synchronized fun set(value: T) {
    currentValue = value
    observers.forEach { it(value) }
  }

  @Synchronized @CheckResult
  fun get(): T = currentValue

  override fun getValue(
    thisRef: Any,
    property: KProperty<*>
  ): T = currentValue

  override fun setValue(
    thisRef: Any,
    property: KProperty<*>,
    value: T
  ) = set(value)
}
