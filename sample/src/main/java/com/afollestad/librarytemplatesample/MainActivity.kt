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
package com.afollestad.librarytemplatesample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.afollestad.librarytemplate.ActionManager
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {
  lateinit var textView: TextView

  private var actionManager: ActionManager? = null
  private var index: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    textView = findViewById(R.id.textView)
    actionManager = ActionManager(
        mainContext = Dispatchers.Main,
        ioContext = Dispatchers.IO
    )

    findViewById<Button>(R.id.buttonView).onClickDebounced {
      actionManager?.doSomething(
          action = { ++index },
          onDone = { result ->
            val text = "${textView.text} $result"
            textView.text = text
          }
      )
    }
  }

  override fun onDestroy() {
    actionManager?.dispose()
    actionManager = null
    super.onDestroy()
  }
}
