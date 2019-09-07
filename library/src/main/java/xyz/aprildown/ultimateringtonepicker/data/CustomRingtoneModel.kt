package xyz.aprildown.ultimateringtonepicker.data

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.BaseColumns
import xyz.aprildown.ultimateringtonepicker.safeContext
import xyz.aprildown.ultimateringtonepicker.sortWithCollator

internal class CustomRingtoneModel(
    private val context: Context,
    private val requireUriPermission: Boolean
) {

    /**
     * Stores all custom ringtones that users select
     */
    private val customMusicDAO = CustomRingtoneDAO(context.getCustomMusicSharedPrefs())

    /**
     * A mutable copy of the custom ringtones.
     */
    private val localCustomRingtones: MutableList<CustomRingtone> by lazy {
        customMusicDAO.getCustomRingtones().apply {
            val cr = context.contentResolver
            forEach {
                it.exists = cr.canFind(it.uri)
            }
            if (requireUriPermission) {
                val allPermissions = cr.persistedUriPermissions.mapNotNull { it?.uri }
                forEach {
                    it.hasPermissions = it.uri in allPermissions
                }
            }
        }
    }

    /**
     * User selects a custom music and we store it in both shared preference and cache
     */
    fun addCustomMusic(uri: Uri, title: String): CustomRingtone {
        // If the uri is already present in an existing ringtone, do nothing.
        val existing = getCustomRingtone(uri)
        if (existing != null) {
            return existing
        }

        val ringtone = customMusicDAO.addCustomRingtone(uri, title)
        localCustomRingtones.add(ringtone)

        localCustomRingtones.sortWithCollator()
        return ringtone
    }

    /**
     * Delete a custom music in both shared preference and cache
     */
    fun removeCustomMusic(uri: Uri) {
        getCustomRingtone(uri)?.let {
            customMusicDAO.removeCustomRingtone(it.id)
            localCustomRingtones.remove(it)
        }
    }

    /**
     * Get all custom musics selected by users
     * @return an immutable list of musics that users select
     */
    fun getCustomRingtones(): List<Ringtone> = localCustomRingtones.map {
        Ringtone(
            it.uri,
            it.title,
            isValid = it.exists && if (requireUriPermission) {
                it.hasPermissions
            } else {
                true
            }
        )
    }

    private fun getCustomRingtone(uri: Uri) = localCustomRingtones.find { it.uri == uri }
}

private fun Context.getCustomMusicSharedPrefs(): SharedPreferences {
    return safeContext().getSharedPreferences(
        "music_picker_prefs", Context.MODE_PRIVATE
    )
}

private fun ContentResolver.canFind(uri: Uri): Boolean {
    return query(uri, arrayOf(BaseColumns._ID), null, null, null)?.use {
        it.moveToFirst() && it.count == 1
    } ?: false
}
