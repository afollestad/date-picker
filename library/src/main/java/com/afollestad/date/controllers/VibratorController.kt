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
@file:Suppress("DEPRECATION")

package com.afollestad.date.controllers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Vibrator
import androidx.core.content.ContextCompat

/** @author Aidan Follestad (@afollestad) */
class VibratorController(private val context: Context) {
  private val vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator

  @SuppressLint("MissingPermission")
  fun vibrateForSelection() {
    if (hasPermission()) {
      vibrator.vibrate(VIBRATION_DURATION)
    }
  }

  private fun hasPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.VIBRATE
    ) == PERMISSION_GRANTED
  }

  private companion object {
    const val VIBRATION_DURATION: Long = 15L
  }
}
