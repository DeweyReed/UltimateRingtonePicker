package xyz.aprildown.ultimateringtonepicker

import android.content.ContentResolver
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.AnyRes
import kotlinx.android.parcel.Parcelize
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.DEVICE_RINGTONE_TYPE_ALBUM
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.DEVICE_RINGTONE_TYPE_ALL
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.DEVICE_RINGTONE_TYPE_ARTIST
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.DEVICE_RINGTONE_TYPE_FOLDER
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.SYSTEM_RINGTONE_TYPE_ALARM
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.SYSTEM_RINGTONE_TYPE_NOTIFICATION
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.SYSTEM_RINGTONE_TYPE_RINGTONE
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.allDeviceRingtoneTypes
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.allSystemRingtoneTypes
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.createAssetUri
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker.Settings.Companion.createRawUri

class UltimateRingtonePicker {

    @Parcelize
    data class Settings(
        /**
         * If this area should be shown:
         *
         * Your sounds
         *     Device Sound 1
         *     Device Sound 2
         *     Add new
         */
        var showCustomRingtone: Boolean = true,

        /**
         * An extra silent ringtone entry below Device sounds.
         */
        var showSilent: Boolean = true,

        /**
         * An extra default ringtone entry below Device sounds.
         *
         * When set to true, you must define [defaultUri] as well.
         * [defaultTitle] is optional.
         */
        var showDefault: Boolean = false,
        var defaultUri: Uri? = null,
        var defaultTitle: String? = null,

        /**
         * Some other ringtone entries.
         *
         * Use [createAssetUri] to create Asset ringtone URIs.
         * Use [createRawUri] to create R.raw.XXX URIs
         */
        var additionalRingtones: List<RingtonePickerEntry> = emptyList(),

        var preSelectUris: List<Uri> = emptyList(),
        var enableMultiSelect: Boolean = false,

        /**
         * Ringtone preview stream type.
         */
        var streamType: Int = AudioManager.STREAM_MUSIC,

        /**
         * One or more values from [SYSTEM_RINGTONE_TYPE_RINGTONE],
         * [SYSTEM_RINGTONE_TYPE_NOTIFICATION] and [SYSTEM_RINGTONE_TYPE_ALARM].
         * You can use [allSystemRingtoneTypes] to include them all.
         */
        var systemRingtoneTypes: List<Int> = emptyList(),

        /**
         * By default, the picker will ask for READ_EXTERNAL_STORAGE permission and show external
         * ringtones, set this to true to use Storage Access Framework which doesn't require
         * the permission.
         */
        var useSafSelect: Boolean = false,

        /**
         * Show device ringtones without showing system ringtones first.
         *
         * When set to true, you should handle READ_EXTERNAL_STORAGE permission by yourself.
         * When set to false(default), the permission will be handled for you.
         */
        var onlyShowDevice: Boolean = false,

        /**
         * One or more values from [DEVICE_RINGTONE_TYPE_ALL], [DEVICE_RINGTONE_TYPE_ARTIST],
         * [DEVICE_RINGTONE_TYPE_ALBUM], [DEVICE_RINGTONE_TYPE_FOLDER]
         * You can use [allDeviceRingtoneTypes] to include them all.
         */
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

            /**
             * Help you build a asset URI.
             */
            @JvmStatic
            fun createAssetUri(fileName: String): Uri = Uri.parse("$ASSET_URI_PREFIX$fileName")

            /**
             * Help you build a raw URI.
             * @param resourceId identifies an application resource
             * @return the Uri by which the application resource is accessed
             */
            @JvmStatic
            fun createRawUri(context: Context, @AnyRes resourceId: Int): Uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.packageName)
                .path(resourceId.toString())
                .build()
        }
    }
}
