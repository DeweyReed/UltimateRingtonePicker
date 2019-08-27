package xyz.aprildown.ultimateringtonepicker

import android.net.Uri

interface RingtonePickerListener {
    fun onRingtonePicked(ringtones: List<Pair<Uri, String>>)
}
