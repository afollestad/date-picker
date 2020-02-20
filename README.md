# Date Picker

[ ![Bintray](https://api.bintray.com/packages/drummer-aidan/maven/date-picker/images/download.svg) ](https://bintray.com/drummer-aidan/maven/date-picker/_latestVersion)
[![Android CI](https://github.com/afollestad/date-picker/workflows/Android%20CI/badge.svg)](https://github.com/afollestad/date-picker/actions?query=workflow%3A%22Android+CI%22)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6d7ae4fee74247aa9f9a45946c9e2289)](https://www.codacy.com/app/drummeraidan_50/date-picker?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=afollestad/date-picker&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

<img src="https://raw.githubusercontent.com/afollestad/date-picker/master/art/showcase2.jpg" width="500" />

---

### Gradle Dependency

```gradle
dependencies {
  ...
  implementation 'com.afollestad:date-picker:0.6.1'
}
```

---

### Why?

Android includes a stock `DatePicker` in its framework, however this widget is very stubborn. It 
does not adapt to different view widths, making it difficult to use in modern UI. This library 
solves for that by creating a custom implementation, written completely in Kotlin.

---

### Basics

It's simple, just add a `DatePicker` to your layout (with the fully qualified package name):

```xml
<com.afollestad.date.DatePicker
    android:id="@+id/datePicker"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    />
```

There are a few basic getters and setters:

```kotlin
val datePicker: DatePicker = // ...

val selectedDate: Calendar? = datePicker.getDate()

datePicker.setDate(
    year = 2019,
    month = Calendar.JUNE,
    selectedDate = 17
)
datePicker.setDate(Calendar.getInstance())
```

---

### Styling

You can configure basic theme properties from your layout:

```xml
<com.afollestad.date.DatePicker
    xmlns:app="http://schemas.android.com/apk/res-auto"
    ...
    app:date_picker_selection_color="?colorAccent"
    app:date_picker_header_background_color="?colorAccent"
    app:date_picker_medium_font="@font/some_medium_font"
    app:date_picker_normal_font="@font/some_normal_font"
    app:date_picker_disabled_background_color="@color/disabled_color"
    app:date_picker_selection_vibrates="true"
    app:date_picker_calendar_horizontal_padding="4dp"
    />
```

*(Note that in order for date_picker_selection_vibrates=true to have an effect, your app needs to 
declare the `VIBRATE` permission in its manifest.)*

---

### Callback

```kotlin
val datePicker: DatePicker = // ...

datePicker.addOnDateChanged { previousDate, newDate->
  // this library provides convenience extensions to Calendar like month, year, and dayOfMonth too.
}

// Removes all callbacks you've added previously with addOnDateChanged(...) 
datePicker.clearOnDateChanged()
```

---

### Min and Max Dates

<img src="https://raw.githubusercontent.com/afollestad/date-picker/master/art/min_max_date.png" width="250" />

```kotlin
val datePicker: DatePicker = // ...

val minDate = datePicker.getMinDate()
datePicker.setMinDate(
  year = 2019,
  month = Calendar.JUNE,
  date = 17
)
datePicker.setMinDate(Calendar.getInstance())

val maxDate = datePicker.getMaxDate()
datePicker.setMaxDate(
  year = 2019,
  month = Calendar.JUNE,
  date = 20
)
datePicker.setMaxDate(Calendar.getInstance())
```
