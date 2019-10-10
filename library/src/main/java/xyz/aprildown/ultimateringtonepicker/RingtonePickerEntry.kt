package xyz.aprildown.ultimateringtonepicker

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RingtonePickerEntry(
    val uri: Uri,
    val name: String
) : Parcelable
