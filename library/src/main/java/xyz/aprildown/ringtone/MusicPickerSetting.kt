package xyz.aprildown.ringtone

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

/**
 * Created on 2018/4/20.
 */

internal class MusicPickerSetting(
        val hasDefault: Boolean,
        val defaultTitle: String,
        val defaultUri: Uri,
        val hasSilent: Boolean,
        val selectedUri: Uri?,
        val additional: List<Pair<String, Uri>>,
        val streamType: Int,
        val musicTypes: IntArray
) : Parcelable {
    constructor(source: Parcel) : this(
            1 == source.readInt(),
            source.readString() ?: "",
            source.readParcelable<Uri>(Uri::class.java.classLoader) ?: NO_MUSIC_URI,
            1 == source.readInt(),
            source.readParcelable<Uri>(Uri::class.java.classLoader),
            ArrayList<Pair<String, Uri>>().apply { source.readList(this, Pair::class.java.classLoader) },
            source.readInt(),
            source.createIntArray() ?: intArrayOf()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt((if (hasDefault) 1 else 0))
        writeString(defaultTitle)
        writeParcelable(defaultUri, 0)
        writeInt((if (hasSilent) 1 else 0))
        writeParcelable(selectedUri, 0)
        writeList(additional)
        writeInt(streamType)
        writeIntArray(musicTypes)
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<MusicPickerSetting> = object : Parcelable.Creator<MusicPickerSetting> {
            override fun createFromParcel(source: Parcel): MusicPickerSetting = MusicPickerSetting(source)
            override fun newArray(size: Int): Array<MusicPickerSetting?> = arrayOfNulls(size)
        }
    }
}