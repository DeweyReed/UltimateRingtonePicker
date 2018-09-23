package xyz.aprildown.ringtone.ui

import android.app.Application
import android.media.AudioManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import xyz.aprildown.ringtone.MusicPickerSetting
import xyz.aprildown.ringtone.data.MusicModel

/**
 * Created on 2018/6/10.
 */

internal class PickerViewModel(application: Application) : AndroidViewModel(application) {
    val musicModel = MusicModel(application)

    lateinit var setting: MusicPickerSetting

    var isPreviewPlaying: Boolean = false
    var selectedUri: Uri? = null

    fun setMusicPickerSetting(setting: MusicPickerSetting?) {
        if (setting == null) {
            this.setting = MusicPickerSetting(
                false,
                "", Uri.EMPTY, true,
                null, listOf(), AudioManager.STREAM_MUSIC, intArrayOf()
            )
        } else {
            this.setting = setting
            this.selectedUri = setting.selectedUri
        }
    }
}