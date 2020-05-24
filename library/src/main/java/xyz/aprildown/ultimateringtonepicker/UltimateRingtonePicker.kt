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

class UltimateRingtonePicker {

    @Parcelize
    data class RingtoneEntry(
        val uri: Uri,
        val name: String
    ) : Parcelable

    interface RingtonePickerListener {
        /**
         * @param ringtones It may be empty or contain one or more entries.
         *                  You should also check Uri.EMPTY if user can select the silent ringtone.
         */
        fun onRingtonePicked(ringtones: List<RingtoneEntry>)
    }

    @Parcelize
    data class SystemRingtonePicker(
        val customSection: CustomSection? = null,
        val defaultSection: DefaultSection? = null,
        /**
         * Values from [RingtoneManager.TYPE_RINGTONE], [RingtoneManager.TYPE_NOTIFICATION] and
         * [RingtoneManager.TYPE_ALARM].
         */
        val ringtoneTypes: List<Int> = emptyList()
    ) : Parcelable {

        @Parcelize
        data class CustomSection(
            /**
             * By default, the library will ask for READ_EXTERNAL_STORAGE permission and show external
             * ringtones, set this to true to use Storage Access Framework which doesn't require
             * the permission.
             */
            val useSafSelect: Boolean = false
        ) : Parcelable

        @Parcelize
        data class DefaultSection(
            /**
             * An extra silent ringtone entry.
             */
            val showSilent: Boolean = true,

            /**
             * An extra default ringtone entry.
             */
            val defaultUri: Uri? = null,
            val defaultTitle: String? = null,

            /**
             * Some other ringtone entries.
             *
             * Use [createAssetRingtoneUri] to create Asset ringtone URIs.
             * Use [createRawRingtoneUri] to create R.raw.XXX URIs
             */
            val additionalRingtones: List<RingtoneEntry> = emptyList()
        ) : Parcelable
    }

    /**
     * Used in [DeviceRingtonePicker]
     */
    enum class RingtoneCategoryType {
        All, Artist, Album, Folder
    }

    @Parcelize
    data class DeviceRingtonePicker(
        val deviceRingtoneTypes: List<RingtoneCategoryType> = emptyList()
    ) : Parcelable

    @Parcelize
    data class Settings(
        val preSelectUris: List<Uri> = emptyList(),
        val enableMultiSelect: Boolean = false,

        /**
         * Ringtone preview stream type.
         */
        val streamType: Int = AudioManager.STREAM_MUSIC,

        val systemRingtonePicker: SystemRingtonePicker? = null,

        /**
         * If [systemRingtonePicker] == null && [deviceRingtonePicker] != null, you need to
         * handle READ_EXTERNAL_STORAGE permission before showing the picker.
         */
        val deviceRingtonePicker: DeviceRingtonePicker? = null
    ) : Parcelable {

        init {
            require(!(systemRingtonePicker == null && deviceRingtonePicker == null))
        }

        fun createFragment(): RingtonePickerFragment = RingtonePickerFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_SETTINGS, this@Settings)
            }
        }
    }

    companion object {
        /**
         * Help you build a asset URI.
         */
        @JvmStatic
        fun createAssetRingtoneUri(fileName: String): Uri = Uri.parse("$ASSET_URI_PREFIX$fileName")

        /**
         * Help you build a R.raw.* URI.
         * @param resourceId identifies an application resource
         * @return the Uri by which the application resource is accessed
         */
        @JvmStatic
        fun createRawRingtoneUri(context: Context, @AnyRes resourceId: Int): Uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .path(resourceId.toString())
            .build()
    }
}
