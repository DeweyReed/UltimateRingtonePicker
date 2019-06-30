package xyz.aprildown.ultimateringtonepicker

import android.net.Uri

/**
 * Created on 2018/9/8.
 */

interface MusicPickerListener {
    fun onMusicPick(uri: Uri, title: String)
    fun onPickCanceled()
}