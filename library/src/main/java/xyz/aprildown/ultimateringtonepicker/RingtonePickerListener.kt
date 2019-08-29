package xyz.aprildown.ultimateringtonepicker

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RingtonePickerResult(
    val uri: Uri,
    val name: String
) : Parcelable

interface RingtonePickerListener {
    fun onRingtonePicked(ringtones: List<RingtonePickerResult>)
}
