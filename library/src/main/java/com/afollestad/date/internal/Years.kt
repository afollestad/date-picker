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

/**
 * If the year (receiver) was 2019, we get 1900 and 2100 in the resulting pair.
 *
 * @author Aidan Follestad (@afollestad)
 */
internal fun Int.visibleYearRange(): Pair<Int, Int> {
  val middle = (this / 100f).toInt() * 100
  return Pair(middle - 100, middle + 100)
}