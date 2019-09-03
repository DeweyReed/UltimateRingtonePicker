package xyz.aprildown.ultimateringtonepicker

import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
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
        var additionalRingtones: List<RingtonePickerEntry> = emptyList(),
        var preSelectUris: List<Uri> = emptyList(),
        var enableMultiSelect: Boolean = false,
        var streamType: Int = AudioManager.STREAM_MUSIC,
        var systemRingtoneTypes: List<Int> = emptyList(),
        var useSafSelect: Boolean = false,
        var onlyShowDevice: Boolean = false,
        var deviceRingtoneTypes: List<Int> = emptyList()
    ) : Parcelable {

        init {
            require(!(showDefault && defaultUri == null)) {
                "Provide a default URI when show default ringtone"
            }
            require(!(onlyShowDevice && deviceRingtoneTypes.isEmpty())) {
                "Provide at least one device ringtone type when only show device ringtones"
            }
            if (!enableMultiSelect && preSelectUris.size > 1) {
                preSelectUris = preSelectUris.take(1)
            }
        }

        fun createFragment(): RingtonePickerFragment = RingtonePickerFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_SETTINGS, this@Settings)
            }
        }

        companion object {
            const val SYSTEM_RINGTONE_TYPE_RINGTONE = RingtoneManager.TYPE_RINGTONE
            const val SYSTEM_RINGTONE_TYPE_NOTIFICATION = RingtoneManager.TYPE_NOTIFICATION
            const val SYSTEM_RINGTONE_TYPE_ALARM = RingtoneManager.TYPE_ALARM

            const val DEVICE_RINGTONE_TYPE_ALL = RINGTONE_TYPE_ALL
            const val DEVICE_RINGTONE_TYPE_ARTIST = RINGTONE_TYPE_ARTIST
            const val DEVICE_RINGTONE_TYPE_ALBUM = RINGTONE_TYPE_ALBUM
            const val DEVICE_RINGTONE_TYPE_FOLDER = RINGTONE_TYPE_FOLDER

            @JvmStatic
            val allSystemRingtoneTypes: List<Int>
                get() = listOf(
                    SYSTEM_RINGTONE_TYPE_RINGTONE,
                    SYSTEM_RINGTONE_TYPE_NOTIFICATION,
                    SYSTEM_RINGTONE_TYPE_ALARM
                )

            @JvmStatic
            val allDeviceRingtoneTypes: List<Int>
                get() = listOf(
                    DEVICE_RINGTONE_TYPE_ALL,
                    DEVICE_RINGTONE_TYPE_ARTIST,
                    DEVICE_RINGTONE_TYPE_ALBUM,
                    DEVICE_RINGTONE_TYPE_FOLDER
                )

            internal const val ASSET_URI_PREFIX = "file:///android_asset/"

            @JvmStatic
            fun createAssetUri(fileName: String): Uri = Uri.parse("$ASSET_URI_PREFIX$fileName")
        }
    }
}
