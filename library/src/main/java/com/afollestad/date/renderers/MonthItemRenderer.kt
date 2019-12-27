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
package com.afollestad.date.renderers

import android.view.Gravity.CENTER
import android.view.View
import android.widget.TextView
import com.afollestad.date.DatePickerConfig
import com.afollestad.date.data.DayOfWeek
import com.afollestad.date.data.MonthItem
import com.afollestad.date.data.MonthItem.DayOfMonth
import com.afollestad.date.data.MonthItem.WeekHeader
import com.afollestad.date.data.NO_DATE
import com.afollestad.date.dayOfWeek
import com.afollestad.date.util.CalendarUtils
import com.afollestad.date.util.LunarCalendarUtils
import com.afollestad.date.util.Util.createCircularSelector
import com.afollestad.date.util.Util.createTextSelector
import com.afollestad.date.util.onClickDebounced
import com.afollestad.date.util.resolveColor
import java.util.Calendar

/** @author Aidan Follestad (@afollestad) */
internal class MonthItemRenderer(private val config: DatePickerConfig) {
  private val calendar = Calendar.getInstance()

  fun render(
    item: MonthItem,
    rootView: View,
    textView: TextView,
    onSelection: (DayOfMonth) -> Unit
  ) {
    when (item) {
      is WeekHeader -> renderWeekHeader(item.dayOfWeek, textView)
      is DayOfMonth -> renderDayOfMonth(item, rootView, textView, onSelection)
    }
  }

  private fun renderWeekHeader(
    dayOfWeek: DayOfWeek,
    textView: TextView
  ) {
    textView.apply {
      setTextColor(context.resolveColor(android.R.attr.textColorSecondary))
      calendar.dayOfWeek = dayOfWeek
      text = config.dateFormatter.weekdayAbbreviation(calendar)
      typeface = config.normalFont
    }
  }

  private fun renderDayOfMonth(
    dayOfMonth: DayOfMonth,
    rootView: View,
    textView: TextView,
    onSelection: (DayOfMonth) -> Unit
  ) {
    rootView.background = null
    textView.apply {
      setTextColor(createTextSelector(context, config.selectionColor))
        if (config.enableChineseCalendar){
            text = solarDate2LunarDateString(dayOfMonth)
        }else{
            text = dayOfMonth.date.positiveOrEmptyAsString()
        }

      typeface = config.normalFont
      gravity = CENTER
      background = null
      setOnClickListener(null)
    }

    if (dayOfMonth.date == NO_DATE) {
      rootView.isEnabled = false
      textView.isSelected = false
      textView.isActivated = false
      return
    }

    rootView.isEnabled = textView.text.toString()
        .isNotEmpty()
    textView.apply {
      isSelected = dayOfMonth.isSelected
      isActivated = dayOfMonth.isToday
      background = createCircularSelector(
          context = context,
          selectedColor = config.selectionColor,
          todayStrokeColor = config.todayStrokeColor
      )
      onClickDebounced { onSelection(dayOfMonth) }
    }
  }

  private fun Int.positiveOrEmptyAsString(): String {
    return if (this < 1) "" else toString()
  }

    private fun solarDate2LunarDateString(dayOfMonth: DayOfMonth):String{
        var dayInMonthStr = dayOfMonth.date.positiveOrEmptyAsString()
        //Log.d("pmm", dayOfMonth.toString())
        val year = dayOfMonth.month.year
        val month = dayOfMonth.month.month+1
        val day = dayOfMonth.date
        if (dayInMonthStr.isNotBlank()) {
            val solar = LunarCalendarUtils.Solar(year, month, day)
            val lunar = LunarCalendarUtils.solarToLunar(solar)
            var lunarStr = ""
            val holidayOfLunar = LunarCalendarUtils.getLunarHoliday(
                lunar.lunarYear,
                lunar.lunarMonth,
                lunar.lunarDay
            )

            //Log.d("pmm", "农历：${lunar.toString()} 农历假期：${holidayOfLunar}")
            val holidayOfSolar = CalendarUtils.getHolidayFromSolar(
                solar.solarYear,
                solar.solarMonth-1,
                solar.solarDay
            )
            //Log.d("pmm", "公历历：${solar.toString()} 公历假期：${holidayOfSolar}")
            if (holidayOfLunar.isNotBlank()) {
                lunarStr = holidayOfLunar
            } else if (holidayOfSolar.isNotBlank()) {
                lunarStr = holidayOfSolar
            } else {
                if (lunar.lunarDay==1){
                    val lunarFirstDayStr =LunarCalendarUtils.getLunarFirstDayString(lunar.lunarMonth,lunar.isLeap)
                    lunarStr = lunarFirstDayStr
                }else {
                    lunarStr = LunarCalendarUtils.getLunarDayString(lunar.lunarDay)
                }
            }


            dayInMonthStr += "\n${lunarStr}"
        }
        return dayInMonthStr
    }
}
