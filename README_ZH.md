<div align="center">
  <img src="https://github.com/DeweyReed/UltimateMusicPicker/blob/master/art/ic_launcher-web.webp?raw=true" height="128" />
</div>

<h1 align="center">UltimateMusicPicker</h1>

<div align="center">
  <strong>通过Activity或对话框选择铃声、通知音、闹钟音或外部存储的音乐文件</strong>
</div>
</br>
<div align="center">
    <a href="https://travis-ci.org/DeweyReed/UltimateMusicPicker">
        <img src="https://travis-ci.org/DeweyReed/UltimateMusicPicker.svg?branch=master"/>
    </a>
    <a href="https://jitpack.io/#DeweyReed/UltimateMusicPicker">
        <img src="https://jitpack.io/v/DeweyReed/UltimateMusicPicker.svg"/>
    </a>
</div>
</br>

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

```Kotlin
UltimateMusicPicker()
    // 选择器Activity或对话框的标题
    .windowTitle("UltimateMusicPicker")

    // 添加一个默认条目
    .defaultUri(uri)
    // 添加的同时修改默认条目的名字(否则将会是"默认提示音")
    .defaultTitleAndUri("My default name", uri)

    // 移除"静音"条目
    .removeSilent()

    // 预选择一个条目
    .selectUri(uri)

    // 添加一些额外的条目(来自R.raw或应用的asset)
    .additional("Myself Music", uri)
    .additional("Another Music", uri)

    // 预览音乐的播放类型
    .streamType(AudioManager.STREAM_MUSIC)

    // 显示设备的铃声
    .ringtone()
    // 显示设备的通知音
    .notification()
    // 显示设备的闹钟音
    .alarm()
    // 显示外部储存中的音乐文件
    // 记得添加READ_EXTERNAL_STORAGE到`Manifest.xml`中
    .music()

    // 显示选择对话框
    // 要在调用此方法Activity或Fragment实现MusicPickerListener
    .goWithDialog(supportFragmentManager)
    // 显示选择Activity，具体方法见下
    //.goWithActivity(this, 0, MusicPickerActivity::class.java)
```

需要注意：

- 默认不需要任何权限(内部使用了`RingtoneManager`实现)，除非你要用`.music()`选择外部储存的音乐文件。

    这样的话，在你的`Manifest.xml`中添加`READ_EXTERNAL_STORAGE`权限，此库内部会处理权限请求。
- 使用`.goWithDialog(supportFragmentManager)`时, 调用此方法的类需要实现`MusicPickerListener`来获取结果。
- 使用`.goWithActivity(this, 0, MusicPickerActivity::class.java)`(第三个参数是一个实现了`MusicPickerListener`的Activity)时, 要在`Manifest.xml`中添加`MusicPickerActivity`(它已经定义好啦)。

    然后在Activity's的`onActivityResult`,

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

## License

[MIT License](https://github.com/DeweyReed/UltimateMusicPicker/blob/master/LICENSE)