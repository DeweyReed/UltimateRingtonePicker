package xyz.aprildown.ultimatemusicpicker.ui

import android.app.Application
import android.media.AudioManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import xyz.aprildown.ultimatemusicpicker.MusicPickerSetting
import xyz.aprildown.ultimatemusicpicker.data.MusicModel

/**
 * Created on 2018/6/10.
 */

internal class PickerViewModel(application: Application) : AndroidViewModel(application) {
    val musicModel = MusicModel(application)

    lateinit var setting: MusicPickerSetting

    var isPreviewPlaying: Boolean = false
    var selectedUri: Uri? = null

    fun setMusicPickerSetting(newSetting: MusicPickerSetting?) {
        if (::setting.isInitialized) return
        if (newSetting == null) {
            setting = MusicPickerSetting(
                false,
                "", Uri.EMPTY, true,
                null, listOf(), AudioManager.STREAM_MUSIC, intArrayOf()
            )
        } else {
            setting = newSetting
            selectedUri = newSetting.selectedUri
        }
    }
}