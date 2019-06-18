# Date Picker

[ ![Bintray](https://api.bintray.com/packages/drummer-aidan/maven/date-picker/images/download.svg) ](https://bintray.com/drummer-aidan/maven/date-picker/_latestVersion)
[![Build Status](https://travis-ci.org/afollestad/date-picker.svg)](https://travis-ci.org/afollestad/date-picker)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

### Gradle Dependency

```gradle
dependencies {
  ...
  implementation 'com.afollestad:date-picker:0.1.0'
}
```

### Usage

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
    month = 6,
    selectedDate = 17
)

datePicker.setDate(Calendar.getInstance())
```

### Styling

You can configure basic theme properties from your layout:

```xml
<com.afollestad.date.DatePicker
    xmlns:app="http://schemas.android.com/apk/res-auto"
    ...
    app:date_picker_selection_color="?colorAccent"
    app:date_picker_header_background_color="?colorAccent"
    app:date_picker_selection_vibrates="true"
    />
```

*(Note that in order for date_picker_selection_vibrates=true to have an effect, your app needs to 
declare the `VIBRATE` permission in its manifest.)*