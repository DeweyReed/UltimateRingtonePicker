package xyz.aprildown.ultimateringtonepicker.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import xyz.aprildown.ultimateringtonepicker.safeContext
import xyz.aprildown.ultimateringtonepicker.sortWithCollator

internal class CustomRingtoneModel(context: Context) {

    /**
     * Stores all custom ringtones that users select
     */
    private val customMusicDAO = CustomRingtoneDAO(context.getCustomMusicSharedPrefs())

    /**
     * A mutable copy of the custom ringtones.
     */
    private val localCustomRingtones: MutableList<CustomRingtone> by lazy {
        customMusicDAO.getCustomRingtones()
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
    fun getCustomRingtones(): List<CustomRingtone> = localCustomRingtones

    private fun getCustomRingtone(uri: Uri) = localCustomRingtones.find { it.uri == uri }
}

private fun Context.getCustomMusicSharedPrefs(): SharedPreferences {
    return safeContext().getSharedPreferences(
        "music_picker_prefs", Context.MODE_PRIVATE
    )
}