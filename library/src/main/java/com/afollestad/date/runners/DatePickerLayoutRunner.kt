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
package com.afollestad.date.runners

import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.View.MeasureSpec
import android.view.View.MeasureSpec.AT_MOST
import android.view.View.MeasureSpec.EXACTLY
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.date.R
import com.afollestad.date.adapters.MonthItemAdapter
import com.afollestad.date.adapters.YearAdapter
import com.afollestad.date.controllers.VibratorController
import com.afollestad.date.data.DateFormatter
import com.afollestad.date.data.snapshot.DateSnapshot
import com.afollestad.date.data.snapshot.MonthSnapshot
import com.afollestad.date.data.snapshot.asCalendar
import com.afollestad.date.runners.DatePickerLayoutRunner.Mode.CALENDAR
import com.afollestad.date.runners.DatePickerLayoutRunner.Mode.INPUT_EDIT
import com.afollestad.date.runners.DatePickerLayoutRunner.Mode.YEAR_LIST
import com.afollestad.date.runners.DatePickerLayoutRunner.Orientation.PORTRAIT
import com.afollestad.date.util.TypefaceHelper
import com.afollestad.date.util.Util.createCircularSelector
import com.afollestad.date.util.attachTopDivider
import com.afollestad.date.util.color
import com.afollestad.date.util.dimenPx
import com.afollestad.date.util.drawable
import com.afollestad.date.util.font
import com.afollestad.date.util.hideKeyboard
import com.afollestad.date.util.invalidateTopDividerNow
import com.afollestad.date.util.onClickDebounced
import com.afollestad.date.util.onTextChanged
import com.afollestad.date.util.placeAt
import com.afollestad.date.util.resolveColor
import com.afollestad.date.util.setCompoundDrawablesCompat
import com.afollestad.date.util.showKeyboard
import com.afollestad.date.util.showOrConceal
import com.afollestad.date.util.showOrHide
import com.afollestad.date.util.string
import com.afollestad.date.util.updatePadding
import com.google.android.material.textfield.TextInputLayout

// TODO write unit tests
/** @author Aidan Follestad (@afollestad) */
internal class DatePickerLayoutRunner(
  private val context: Context,
  typedArray: TypedArray,
  root: ViewGroup,
  private val vibrator: VibratorController,
  private val dateFormatter: DateFormatter,
  private val onDateInput: (CharSequence) -> Unit
) {
  val selectionColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_selection_color) {
      context.resolveColor(R.attr.colorAccent)
    }
  private val headerBackgroundColor: Int =
    typedArray.color(R.styleable.DatePicker_date_picker_header_background_color) {
      context.resolveColor(R.attr.colorAccent)
    }
  private val normalFont: Typeface =
    typedArray.font(context, R.styleable.DatePicker_date_picker_normal_font) {
      TypefaceHelper.create("sans-serif")
    }
  private val mediumFont: Typeface =
    typedArray.font(context, R.styleable.DatePicker_date_picker_medium_font) {
      TypefaceHelper.create("sans-serif-medium")
    }
  private val calendarHorizontalPadding: Int =
    typedArray.getDimensionPixelSize(
        R.styleable.DatePicker_date_picker_calendar_horizontal_padding, 0
    )
  private val pickerTitle: String =
    typedArray.string(context, R.styleable.DatePicker_date_picker_title) {
      context.getString(R.string.select_date)
    }
  private val manualInputLabel: String =
    typedArray.string(context, R.styleable.DatePicker_date_picker_manual_input_label) {
      context.getString(R.string.enter_date)
    }

  private val pickerTitleView: TextView = root.findViewById(R.id.picker_title)
  private val selectedDateView: TextView = root.findViewById(R.id.current_date)
  private val editModeToggleView: ImageView = root.findViewById(R.id.edit_mode_toggle)
  private val editModeInput: TextInputLayout = root.findViewById(R.id.edit_mode_input)

  private val goPreviousMonthView: ImageView = root.findViewById(R.id.left_chevron)
  private val visibleMonthView: TextView = root.findViewById(R.id.current_month)
  private val goNextMonthView: ImageView = root.findViewById(R.id.right_chevron)

  private val listsDividerView: View = root.findViewById(R.id.year_month_list_divider)
  private val daysRecyclerView: RecyclerView = root.findViewById(R.id.day_list)
  private val yearsRecyclerView: RecyclerView = root.findViewById(R.id.year_list)

  private val currentMonthTopMargin: Int =
    context.dimenPx(R.dimen.current_month_top_margin)
  private val chevronsTopMargin: Int =
    context.dimenPx(R.dimen.chevrons_top_margin)
  private val currentMonthHeight: Int =
    context.dimenPx(R.dimen.current_month_header_height)
  private val dividerHeight: Int =
    context.dimenPx(R.dimen.divider_height)
  private val headersWithFactor: Int =
    context.resources.getInteger(R.integer.headers_width_factor)

  private val size = Size(0, 0)
  private val orientation = Orientation.get(context)
  private var lastMode: Mode? = null

  init {
    setupHeaderViews()
    setupNavigationViews()
    setupListViews()
    typedArray.getInt(R.styleable.DatePicker_date_picker_default_mode, CALENDAR.rawValue)
        .let { Mode.fromRawValue(it) }
        .let { if (it != CALENDAR) setMode(it) }
  }

  @CheckResult fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ): Size {
    val parentWidth: Int = MeasureSpec.getSize(widthMeasureSpec)
    val parentHeight: Int = MeasureSpec.getSize(heightMeasureSpec)

    // First header views
    val headersWidth: Int = (parentWidth / headersWithFactor)
    if (!pickerTitleView.text.isNullOrBlank()) {
      pickerTitleView.measure(
          makeMeasureSpec(headersWidth, EXACTLY),
          makeMeasureSpec(0, UNSPECIFIED)
      )
    }
    selectedDateView.measure(
        makeMeasureSpec(headersWidth, EXACTLY),
        if (parentHeight <= 0 || orientation == PORTRAIT) {
          makeMeasureSpec(0, UNSPECIFIED)
        } else {
          makeMeasureSpec(parentHeight - pickerTitleView.measuredHeight, EXACTLY)
        }
    )
    context.dimenPx(R.dimen.edit_mode_toggle_size)
        .let {
          editModeToggleView.measure(
              makeMeasureSpec(it, EXACTLY),
              makeMeasureSpec(it, EXACTLY)
          )
        }
    context.dimenPx(R.dimen.edit_mode_input_margin_sides)
        .let {
          editModeInput.measure(
              makeMeasureSpec(headersWidth - (it * 2), EXACTLY),
              makeMeasureSpec(0, UNSPECIFIED)
          )
        }

    // And the current month
    val nonHeadersWidth: Int = if (orientation == PORTRAIT) {
      parentWidth
    } else {
      parentWidth - headersWidth
    }
    visibleMonthView.measure(
        makeMeasureSpec(nonHeadersWidth, AT_MOST),
        makeMeasureSpec(currentMonthHeight, EXACTLY)
    )

    // Then the divider
    listsDividerView.measure(
        makeMeasureSpec(nonHeadersWidth, EXACTLY),
        makeMeasureSpec(dividerHeight, EXACTLY)
    )

    // Then the calendar recycler view
    val heightSoFar = if (orientation == PORTRAIT) {
      pickerTitleView.measuredHeight +
          selectedDateView.measuredHeight +
          visibleMonthView.measuredHeight +
          listsDividerView.measuredHeight
    } else {
      visibleMonthView.measuredHeight +
          listsDividerView.measuredHeight
    }
    val recyclerViewsWidth: Int = (nonHeadersWidth - (calendarHorizontalPadding * 2))
    daysRecyclerView.measure(
        makeMeasureSpec(recyclerViewsWidth, EXACTLY),
        if (parentHeight > 0) {
          makeMeasureSpec(parentHeight - heightSoFar, AT_MOST)
        } else {
          makeMeasureSpec(0, UNSPECIFIED)
        }
    )

    // Then the go back / go forward chevrons
    val chevronWidthAndHeight: Int = (recyclerViewsWidth / DAYS_IN_WEEK)
    goPreviousMonthView.measure(
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY),
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY)
    )
    goNextMonthView.measure(
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY),
        makeMeasureSpec(chevronWidthAndHeight, EXACTLY)
    )

    // Then the year and month recycler views
    yearsRecyclerView.measure(
        makeMeasureSpec(daysRecyclerView.measuredWidth, EXACTLY),
        makeMeasureSpec(daysRecyclerView.measuredHeight, EXACTLY)
    )

    // Calculate a total
    return size.apply {
      width = parentWidth
      // Vertical margins are included here
      height = (heightSoFar +
          daysRecyclerView.measuredHeight +
          chevronsTopMargin +
          currentMonthTopMargin)
    }
  }

  fun onLayout(
    left: Int,
    top: Int
  ) {
    // First header views
    if (!pickerTitleView.text.isNullOrBlank()) {
      pickerTitleView.placeAt(top = top)
      selectedDateView.placeAt(top = pickerTitleView.bottom)
    } else {
      selectedDateView.placeAt(top = top)
    }
    val nonHeaderLeft = (if (orientation == PORTRAIT) left else selectedDateView.right)

    // And the current month
    val visibleMonthViewTop = if (orientation == PORTRAIT) {
      selectedDateView.bottom + currentMonthTopMargin
    } else {
      currentMonthTopMargin
    }
    visibleMonthView.placeAt(top = visibleMonthViewTop)

    // Then the divider
    listsDividerView.placeAt(
        top = visibleMonthView.bottom,
        left = nonHeaderLeft
    )

    // Then the calendar recycler view
    val daysRecyclerViewLeft = (nonHeaderLeft + calendarHorizontalPadding)
    daysRecyclerView.placeAt(
        left = daysRecyclerViewLeft,
        top = listsDividerView.bottom
    )

    // Then the go back / go forward chevrons
    val chevronsMiddleY = (visibleMonthView.bottom - (visibleMonthView.measuredHeight / 2))
    val chevronsTop =
      ((chevronsMiddleY - (goPreviousMonthView.measuredHeight / 2)) + chevronsTopMargin)
    val nextChevronLeft = (daysRecyclerView.right -
        goNextMonthView.measuredWidth -
        calendarHorizontalPadding)

    goNextMonthView.placeAt(
        left = nextChevronLeft,
        top = chevronsTop
    )
    goPreviousMonthView.placeAt(
        left = (nextChevronLeft - goNextMonthView.measuredWidth),
        top = chevronsTop
    )

    // This goes way down here cause we want to layout the chevrons first
    val editToggleLeft = nextChevronLeft +
        ((goPreviousMonthView.measuredWidth - editModeToggleView.measuredWidth) / 2)
    editModeToggleView.placeAt(
        top = selectedDateView.top + (editModeToggleView.measuredHeight / 2),
        left = editToggleLeft
    )

    // Then the edit mode input field
    val inputMarginTop = context.dimenPx(R.dimen.edit_mode_input_margin_top)
    val inputMarginSides = context.dimenPx(R.dimen.edit_mode_input_margin_sides)
    editModeInput.placeAt(
        top = selectedDateView.bottom + inputMarginTop,
        left = inputMarginSides
    )

    // Then the year and month recycler views
    yearsRecyclerView.layout(
        /* left   = */daysRecyclerView.left,
        /* top    = */daysRecyclerView.top,
        /* right  = */daysRecyclerView.right,
        /* bottom = */daysRecyclerView.bottom
    )
  }

  fun setAdapters(
    monthItemAdapter: MonthItemAdapter,
    yearAdapter: YearAdapter
  ) {
    daysRecyclerView.adapter = monthItemAdapter
    yearsRecyclerView.adapter = yearAdapter
  }

  fun showOrHideGoPrevious(show: Boolean) {
    if (INPUT_EDIT == lastMode) return
    goPreviousMonthView.showOrConceal(show)
  }

  fun showOrHideGoNext(show: Boolean) {
    if (INPUT_EDIT == lastMode) return
    goNextMonthView.showOrConceal(show)
  }

  fun setHeadersContent(
    currentMonth: MonthSnapshot,
    selectedDate: DateSnapshot,
    fromUserEditInput: Boolean
  ) {
    visibleMonthView.text = dateFormatter.monthAndYear(currentMonth.asCalendar(1))
    selectedDate.asCalendar()
        .let {
          selectedDateView.text = dateFormatter.date(it)
          val inputDateString = dateFormatter.inputDate(it)
          if (!fromUserEditInput) {
            editModeInput.editText?.setText(inputDateString)
            editModeInput.editText?.setSelection(inputDateString.length)
          }
        }
  }

  fun scrollToYearPosition(pos: Int) = yearsRecyclerView.scrollToPosition(pos - 2)

  fun onNavigate(
    onGoToPrevious: () -> Unit,
    onGoToNext: () -> Unit
  ) {
    goPreviousMonthView.onClickDebounced { onGoToPrevious() }
    goNextMonthView.onClickDebounced { onGoToNext() }
  }

  private fun setupHeaderViews() {
    pickerTitleView.apply {
      background = ColorDrawable(headerBackgroundColor)
      typeface = normalFont
      text = pickerTitle
    }
    selectedDateView.apply {
      isSelected = true
      background = ColorDrawable(headerBackgroundColor)
      typeface = normalFont
      onClickDebounced { setMode(CALENDAR) }
    }
    editModeToggleView.apply {
      background = createCircularSelector(
          context, context.resolveColor(android.R.attr.textColorPrimaryInverse)
      )
      onClickDebounced { toggleMode(INPUT_EDIT) }
    }
    editModeInput.hint = manualInputLabel
    editModeInput.editText?.apply {
      hint = dateFormatter.dateInputFormatter.toLocalizedPattern()
      onTextChanged { onDateInput(it) }
    }
  }

  private fun setupNavigationViews() {
    goPreviousMonthView.background = createCircularSelector(context, selectionColor)
    visibleMonthView.apply {
      typeface = mediumFont
      onClickDebounced { toggleMode() }
    }
    invalidateCurrentMonthChevron()
    goNextMonthView.background = createCircularSelector(context, selectionColor)
  }

  private fun setupListViews() {
    daysRecyclerView.apply {
      layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.day_grid_span))
      attachTopDivider(listsDividerView)
      updatePadding(
          left = calendarHorizontalPadding,
          right = calendarHorizontalPadding
      )
    }
    yearsRecyclerView.apply {
      layoutManager = GridLayoutManager(context, resources.getInteger(R.integer.year_grid_span))
      attachTopDivider(listsDividerView)
    }
  }

  fun setMode(mode: Mode) {
    invalidateCurrentMonthChevron()

    when (mode) {
      CALENDAR -> {
        daysRecyclerView.invalidateTopDividerNow(listsDividerView)
        daysRecyclerView.showOrConceal(true)
        yearsRecyclerView.showOrConceal(false)
        editModeInput.showOrHide(false)
        visibleMonthView.showOrHide(true)
        goPreviousMonthView.showOrHide(true)
        goNextMonthView.showOrHide(true)
        editModeInput.editText?.hideKeyboard()
      }
      YEAR_LIST -> {
        yearsRecyclerView.invalidateTopDividerNow(listsDividerView)
        daysRecyclerView.showOrConceal(false)
        yearsRecyclerView.showOrConceal(true)
        editModeInput.showOrHide(false)
        visibleMonthView.showOrHide(true)
        goPreviousMonthView.showOrHide(true)
        goNextMonthView.showOrHide(true)
        editModeInput.editText?.hideKeyboard()
      }
      INPUT_EDIT -> {
        daysRecyclerView.showOrHide(false)
        yearsRecyclerView.showOrHide(false)
        editModeInput.showOrHide(true)
        visibleMonthView.showOrHide(false)
        goPreviousMonthView.showOrHide(false)
        goNextMonthView.showOrHide(false)
        editModeInput.editText?.showKeyboard()
      }
    }

    pickerTitleView.isSelected = mode == YEAR_LIST
    selectedDateView.isSelected = mode == CALENDAR
    editModeToggleView.setImageResource(
        if (mode == CALENDAR) {
          R.drawable.ic_edit
        } else {
          R.drawable.ic_calendar
        }
    )
    vibrator.vibrateForSelection()
    lastMode = mode
  }

  private fun toggleMode(wantedMode: Mode = YEAR_LIST) {
    when (lastMode) {
      null, CALENDAR -> setMode(wantedMode)
      INPUT_EDIT -> setMode(CALENDAR)
      else -> setMode(CALENDAR)
    }
  }

  private fun invalidateCurrentMonthChevron() {
    val expanded = lastMode == YEAR_LIST
    val currentMonthChevronDrawable = context.drawable(
        if (expanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down,
        context.resolveColor(android.R.attr.textColorSecondary)
    )
    visibleMonthView.setCompoundDrawablesCompat(end = currentMonthChevronDrawable)
  }

  enum class Mode(val rawValue: Int) {
    CALENDAR(1),
    YEAR_LIST(2),
    INPUT_EDIT(3);

    companion object {
      fun fromRawValue(value: Int): Mode {
        return values().single { it.rawValue == value }
      }
    }
  }

  enum class Orientation {
    PORTRAIT,
    LANDSCAPE;

    companion object {
      fun get(context: Context): Orientation {
        return if (context.resources.configuration.orientation == ORIENTATION_PORTRAIT) {
          PORTRAIT
        } else {
          LANDSCAPE
        }
      }
    }
  }

  data class Size(
    var width: Int,
    var height: Int
  )

  companion object {
    @CheckResult fun inflateInto(
      context: Context,
      typedArray: TypedArray,
      container: ViewGroup,
      dateFormatter: DateFormatter,
      onDateInput: (CharSequence) -> Unit
    ): DatePickerLayoutRunner {
      View.inflate(context, R.layout.date_picker, container)
      val vibrator = VibratorController(context, typedArray)
      return DatePickerLayoutRunner(
          context, typedArray, container, vibrator, dateFormatter, onDateInput
      )
    }

    private const val DAYS_IN_WEEK = 7
  }
}
