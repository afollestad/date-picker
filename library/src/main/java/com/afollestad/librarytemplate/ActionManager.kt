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
package com.afollestad.librarytemplate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * @author Aidan Follestad (@afollestad)
 */
class ActionManager(
  private val mainContext: CoroutineContext,
  private val ioContext: CoroutineContext
) {
  private var job: Job? = Job(mainContext[Job])

  /**
   * Executes [action] on the coroutine scope and sends the result to [onDone].
   */
  fun <T> doSomething(
    action: () -> T,
    onDone: (result: T) -> Unit
  ) {
    check(job != null) { "Manager already disposed." }
    val scope = CoroutineScope(ioContext + job!!)
    scope.launch {
      delay(500L)
      val result = action()
      delay(500L)
      withContext(mainContext) {
        onDone(result)
      }
    }
  }

  /** Cancels the manager's job, cancelling all children as well. */
  fun dispose() {
    job?.cancel()
  }
}
