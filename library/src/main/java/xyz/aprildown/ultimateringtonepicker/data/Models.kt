package xyz.aprildown.ultimateringtonepicker.data

import android.net.Uri

internal data class Ringtone(
    val uri: Uri,
    val title: String,
    val artistId: Long? = null,
    val albumId: Long? = null
)

internal data class Category(
    val type: Int,
    val categoryId: Long,
    val name: String,
    val numberOfSongs: Int
)
