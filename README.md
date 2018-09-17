<div align="center">
  <img src="https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/ic_launcher-web.png?raw=true" height="128" />
</div>

<h1 align="center">UltimateMusicPicker</h1>

<div align="center">
  <strong>Pick ringtone, notification, alarm sound and music files from external storage with an Activity or a dialog</strong>
</div>
</br>
<div align="center">
    <a href="https://travis-ci.org/DeweyReed/UltimateMusicPicker">
        <img src="https://travis-ci.org/DeweyReed/UltimateMusicPicker.svg?branch=master"/>
    </a>
    <a href="https://jitpack.io/#DeweyReed/UltimateMusicPicker">
        <img src="https://jitpack.io/v/DeweyReed/UltimateMusicPicker.svg"/>
    </a>
    </br>
    <a href="https://github.com/DeweyReed/UltimateMusicPicker/blob/master/README_ZH.md">
        <img src="https://img.shields.io/badge/Translation-%E4%B8%AD%E6%96%87-red.svg">
    </a>
</div>
</br>

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

[![The Newest Version](https://jitpack.io/v/DeweyReed/UltimateMusicPicker.svg)](https://jitpack.io/#DeweyReed/UltimateMusicPicker)

```Groovy
dependencies {
    implementation "com.github.DeweyReed:UltimateMusicPicker:${version}"
}
```

## Usage

```Kotlin
UltimateMusicPicker()
    // Picker activity action bar title or dialog title
    .windowTitle("UltimateMusicPicker")

    // Add a extra default item
    .defaultUri(uri)
    // Add default and change the default item name("Default" is used otherwise)
    .defaultTitleAndUri("My default name", uri)

    // Use this to remove the "Silent" item
    .removeSilent()

    // Select this uri
    .selectUri(uri)

    // Add some other music items(from R.raw or app's asset)
    .additional("Myself Music", uri)
    .additional("Another Music", uri)

    // Music preview stream type
    .streamType(AudioManager.STREAM_MUSIC)

    // Show device ringtones sound
    .ringtone()
    // Show device notification sound
    .notification()
    // Show device alarm sound
    .alarm()
    // Show music files from external storage.
    // Remember to add READ_EXTERNAL_STORAGE permission to your `Manifest.xml`.
    .music()

    // Show a picker dialog
    // Remember to implement MusicPickerListener for the calling activity or fragment
    .goWithDialog(supportFragmentManager)
    // Or show a picker activity. Check below for more.
    //.goWithActivity(this, 0, MusicPickerActivity::class.java)
```

Things to remember:

- No permission is needed(`RingtoneManager` is used internally) unless you use `.music()` to select music files in the external storage.

    If so, add `READ_EXTERNAL_STORAGE` permission to your app's `Manifest.xml` and the library will handle permission request properlly.
- When use `.goWithDialog(supportFragmentManager)`, the calling class should implement `MusicPickerListener` to get pick result.
- When use `.goWithActivity(this, 0, MusicPickerActivity::class.java)`(the thrid parameter is an Activity implementing `MusicPickerListener`), add `MusicPickerActivity`(already defined in the library) in your `Manifest.xml`.

    Then in the activity's `onActivityResult`,

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

## License

[MIT License](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/LICENSE)