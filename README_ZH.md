<div align="center">
  <img src="https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/ic_launcher-web.webp?raw=true" height="128" />
</div>

<h1 align="center">UltimateMusicPicker</h1>

<div align="center">
  <strong>通过Activity或对话框选择铃声、通知音、闹钟音或外部存储的音乐文件</strong>
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
</div>
</br>

## 概览

- 将音乐文件分为闹钟音、通知音、铃声和外部音乐文件
- 可以设置默认条目
- 可以添加自定义音乐条目
- 自动保存用户选择过的外部音乐文件
- 音乐预览
- 既可以使用Activity也可以使用对话框
- 暗色主题的支持
- 内部已负责了权限的获取
- AndroidX支持

## 截图

||||
|:-:|:-:|:-:|
|![Activity](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/activity.webp?raw=true)|![对话框](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/dialog.webp?raw=true)|![暗色对话框](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/dark.webp?raw=true)|

# 目录

1. [示例APK](https://github.com/DeweyReed/UltimateMusicPicker/releases)
1. [Gradle依赖](#gradle依赖)
1. [使用方法](#使用方法)
1. [高级用法](#高级用法)
    1. [自定义Activity](#自定义Activity)
    1. [暗色主题](#暗色主题)
1. [计划清单](#计划清单)
1. [从1.X升级](#从1.X升级)
1. [License](#license)

## Gradle依赖

步骤 1. 添加JitPack仓库到build文件

在根目录build.gradle的仓库列表下添加:

```Groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

步骤 2. 添加此库依赖

[![The Newest Version](https://jitpack.io/v/DeweyReed/UltimateMusicPicker.svg)](https://jitpack.io/#DeweyReed/UltimateMusicPicker)

```Groovy
dependencies {
    implementation "com.github.DeweyReed:UltimateMusicPicker:${version}"
}
```

## 使用方法

1. 如果需要选择外部储存的音乐文件，需添加`READ_EXTERNAL_STORAGE`权限到`Manifest.xml`。

`<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />`

1. 如果需要使用对话框选择音乐, 为activity或fragment实现`MusicPickerListener`。

```Kotlin
 interface MusicPickerListener {
     fun onMusicPick(uri: Uri, title: String)
     fun onPickCanceled()
 }
```

1. 如果需要使用Activity选择音乐，需添加这一行到`Manifest.xml`:

`<activity android:name="xyz.aprildown.ultimatemusicpicker.MusicPickerActivity" />`

并在`onActivityResult`获取选择结果:

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

1. 开始选择

```Kotlin
UltimateMusicPicker()
    // 选择器Activity或对话框的标题
    .windowTitle("UltimateMusicPicker")

    // 添加一个默认条目
    .defaultUri(uri)
    // 添加的同时修改默认条目的名字(否则将会是"默认提示音")
    .defaultTitleAndUri("My default name", uri)

    // 默认有一个"静音"条目，使用这行移除"静音"条目
    .removeSilent()

    // 预选择一个条目
    .selectUri(uri)

    // 添加一些额外的条目(来自R.raw或应用的asset)
    .additional("Myself Music", uri)
    .additional("Another Music", uri)

    // 预览音乐的播放类型，默认是AudioManager.STREAM_MUSIC
    .streamType(AudioManager.STREAM_ALARM)

    // 显示设备的铃声、通知音、闹钟音，它们的显示顺序和这里的调用顺序一致
    .ringtone()
    .notification()
    .alarm()
    // 显示外部储存中的音乐文件，需要READ_EXTERNAL_STORAGE权限
    .music()

    // 显示选择对话框
    .goWithDialog(supportFragmentManager)
    // 显示选择Activity
    //.goWithActivity(this, 0, MusicPickerActivity::class.java)
```

**在fragment中启动选择对话框时，要使用`childFragmentManager`而不是`fragmentManager`**

## 高级用法

选择器界面使用`Fragment`实现，因此可以容易地适配到Activity或对话框中。

### 自定义Activity

只需要剪贴复制[`MusicPickerActivity`](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/library/src/main/java/xyz/aprildown/ringtone/MusicPickerActivity.kt)或[`MusicPickerDialog`](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/library/src/main/java/xyz/aprildown/ringtone/MusicPickerDialog.kt)，再稍加修改即可. 你可能注意到它只是包装了一下`MusicPickerFragment`，它可以用到很多地方(比如`ViewPager`?)。

此外，`UltimateMusicPicker`还有两个方法来帮你：

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

### 暗色主题

此库只实现了简单的暗色主题，通过`AppCompatDelegate.setDefaultNightMode`调用暗色主题时，没任何问题。这样不够的话，开Issue或发PR。

## 计划清单

- 使用`READ_CONTENT`实现无需权限的选择

## 从1.X升级

2.0.0将包名从`xyz.aprildown.ringtone.UltimateMusicPicker`改名为`xyz.aprildown.ultimatemusicpicker.UltimateMusicPicker`，来让它更有意义。

清理一下imports应该就可以了。

## License

[MIT License](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/LICENSE)