package xyz.aprildown.ultimateringtonepicker

import android.media.AudioManager
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class UltimateRingtonePicker {

    @Parcelize
    data class Settings(
        var showCustomRingtone: Boolean = true,
        var showDefault: Boolean = false,
        var defaultUri: Uri? = null,
        var defaultTitle: String? = null,
        var showSilent: Boolean = true,
        var additionalRingtones: List<Pair<Uri, String>> = emptyList(),
        var preSelectUris: List<Uri> = emptyList(),
        var enableMultiSelect: Boolean = true,
        var streamType: Int = AudioManager.STREAM_MUSIC,
        var ringtoneTypes: List<Int> = emptyList(),
        var useSafSelect: Boolean = false,
        var onlyShowDevice: Boolean = false
    ) : Parcelable {
        companion object {
            const val TYPE_RINGTONE = 0
            const val TYPE_NOTIFICATION = 1
            const val TYPE_ALARM = 2
            const val TYPE_CUSTOM = 2
        }
    }
}
