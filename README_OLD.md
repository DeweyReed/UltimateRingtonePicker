<div align="center">
  <img src="https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/ic_launcher-web.webp?raw=true" height="128" />
</div>

<h1 align="center">UltimateMusicPicker</h1>

<div align="center">
  <strong>Pick ringtone, notification, alarm sound and music files from external storage with an Activity or a dialog</strong>
</div>
</br>
<div align="center">
    <a href="https://android-arsenal.com/details/1/7141">
        <img src="https://img.shields.io/badge/Android%20Arsenal-UltimateMusicPicker-green.svg?style=flat"/>
    </a>
    <a href="https://travis-ci.org/DeweyReed/UltimateMusicPicker">
        <img src="https://travis-ci.org/DeweyReed/UltimateMusicPicker.svg?branch=master"/>
    </a>
    <a href="https://jitpack.io/#DeweyReed/UltimateMusicPicker">
        <img src="https://jitpack.io/v/DeweyReed/UltimateMusicPicker.svg"/>
    </a>
    <a href="https://android-arsenal.com/api?level=14">
        <img src="https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat" border="0" alt="API">
    </a>
    </br>
    <a href="https://github.com/DeweyReed/UltimateMusicPicker/blob/master/README_ZH.md">
        <img src="https://img.shields.io/badge/Translation-%E4%B8%AD%E6%96%87-red.svg">
    </a>
</div>
</br>

## Overview

- Separates music to alarm sound, notification sound, ringtone sound and external music files.
- Provides interface to specify default item
- Provides interface to add custom music items
- Automatically remembers which external music files users picked
- Music Preview
- Available as an Activity and a Dialog
- Dark theme support
- Permission are handled internally
- AndroidX support

## Screenshot

||||
|:-:|:-:|:-:|
|![Activity](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/activity.webp?raw=true)|![Dialog](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/dialog.webp?raw=true)|![Dark](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/dark.webp?raw=true)|

# Table of Contents

1. [Sample APK](https://github.com/DeweyReed/UltimateMusicPicker/releases)
1. [Gradle Dependency](#gradle-dependency)
1. [Usage](#usage)
1. [Advanced Usage](#advanced-usage)
    1. [Custom Activity](#custom-activity)
    1. [Dark Theme](#dark-theme)
1. [Todo List](#todo-list)
1. [Migrate from 1.X](#migrate-from-1x)
1. [License](#license)

## Gradle Dependency

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```Groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```Groovy
dependencies {
    implementation 'com.github.DeweyReed:UltimateMusicPicker:2.0.6'
}
```

## Usage

1. If you wish to pick external music files, add `READ_EXTERNAL_STORAGE` permission to the `Manifest.xml`.

    `<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />`

1. If you wish to use picker dialog, implement `MusicPickerListener` in the activity or fragment to get pick result.

    ```Kotlin
    interface MusicPickerListener {
        fun onMusicPick(uri: Uri, title: String)
        fun onPickCanceled()
    }
    ```

1. If you wish to use predefined activity to pick music, add this to the `Manifest.xml`:

    `<activity android:name="xyz.aprildown.ultimatemusicpicker.MusicPickerActivity" />`

    and receive the pick result in the `onActivityResult`:

    ```Kotlin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra(UltimateMusicPicker.EXTRA_SELECTED_TITLE)
            val uri = data?.getParcelableExtra<Uri>(UltimateMusicPicker.EXTRA_SELECTED_URI)
            if (title != null && uri != null) {
                onMusicPick(uri, title)
            } else {
                onPickCanceled()
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }
    ```

1. Let's pick some something.

    ```Kotlin
    UltimateMusicPicker()
        // Picker activity action bar title or dialog title
        .windowTitle("UltimateMusicPicker")

        // Add a extra default item
        .defaultUri(uri)
        // Add a default item and change the default item name("Default" is used otherwise)
        .defaultTitleAndUri("My default name", uri)

        // There's a "silent" item by default, use this line to remove it.
        .removeSilent()

        // Select this uri
        .selectUri(uri)

        // Add some other music items(from R.raw or app's asset)
        .additional("Myself Music", uri)
        .additional("Another Music", uri)

        // Music preview stream type(AudioManager.STREAM_MUSIC is used by default)
        .streamType(AudioManager.STREAM_ALARM)

        // Show different kinds of system ringtones. Calling order determines their display order.
        .ringtone()
        .notification()
        .alarm()
        // Show music files from external storage. Requires READ_EXTERNAL_STORAGE permission.
        .music()

        // Show a picker dialog
        .goWithDialog(supportFragmentManager)
        // Or show a picker activity
        //.goWithActivity(this, 0, MusicPickerActivity::class.java)
    ```

    **When you launch dialog picker in an fragment, remember to use `childFragmentManager` instead of `fragmentManager` to make sure the child can find his/her parents.**

## Advanced Usage

The picker view is a `Fragment` so it can be easily used in an Activity and a dialog.

### Custom Activity

Simply copy and paste [`MusicPickerActivity`](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/library/src/main/java/xyz/aprildown/ringtone/MusicPickerActivity.kt) or [`MusicPickerDialog`](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/library/src/main/java/xyz/aprildown/ringtone/MusicPickerDialog.kt) and create your own. You may notice it's just a wrapper for `MusicPickerFragment` and it can be used in many places(like in a `ViewPager`?)

What's more, there are two methods in the `UltimateMusicPicker` class to help you.

```Kotlin
/**
  * Create a setting [Parcelable]. Useful when customize how to start activity
  */
fun buildParcelable(): Parcelable

/**
  * Put a setting [Parcelable] into a [Intent]. Useful when customize how to start activity
  */
fun putSettingIntoIntent(intent: Intent): Intent
```

### Dark Theme

This library supports dark theme with a naive way. It works fine when I use `AppCompatDelegate.setDefaultNightMode` to toggle night theme. If this is not enough, open a issue or send a PR.

## Todo List

- Use `READ_CONTENT` to select without permission

## Migrate from 1.X

2.0.0 renames package name from `xyz.aprildown.ringtone.UltimateMusicPicker` to `xyz.aprildown.ultimatemusicpicker.UltimateMusicPicker` to make it more meaningful.

So after cleaning up imports, everything should work.

## License

[MIT License](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/LICENSE)