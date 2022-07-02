package xyz.aprildown.ultimateringtonepicker.data

import android.net.Uri
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker

internal data class Ringtone(
    val uri: Uri,
    val title: String,
    val duration: String,
    val artistId: Long? = null,
    val albumId: Long? = null,
    val isValid: Boolean = true
)

internal data class Category(
    val type: UltimateRingtonePicker.RingtoneCategoryType,
    val id: Long,
    val name: String,
    val numberOfSongs: Int
)
