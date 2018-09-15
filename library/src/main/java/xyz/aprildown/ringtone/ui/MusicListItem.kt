package xyz.aprildown.ringtone.ui

import android.net.Uri

/**
 * Created on 2018/4/17.
 */

internal sealed class MusicListItem

internal class HeaderItem(val title: String) : MusicListItem()

internal object AddCustomItem : MusicListItem()

internal open class SoundItem(
        val type: Int,
        val uri: Uri,
        val title: String,
        var isSelected: Boolean,
        var isPlaying: Boolean
) : MusicListItem() {
    companion object {
        const val TYPE_CUSTOM = 0
        const val TYPE_SILENT = 1
        const val TYPE_RINGTONE = 2
    }
}
