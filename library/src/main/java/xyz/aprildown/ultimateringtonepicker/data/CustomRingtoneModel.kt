package xyz.aprildown.ultimateringtonepicker.data

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.BaseColumns
import xyz.aprildown.ultimateringtonepicker.safeContext

internal class CustomRingtoneModel(
    private val context: Context,
    private val requireUriPermission: Boolean
) {

    /**
     * Stores all custom ringtones that users select
     */
    private val customRingtoneDAO = CustomRingtoneDAO(context.getCustomRingtoneSharedPrefs())

    /**
     * A mutable copy of the custom ringtones.
     */
    private val ringtoneCache: MutableList<CustomRingtone> by lazy {
        customRingtoneDAO.getCustomRingtones().apply {
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
     * User selects a custom ringtone and we store it in both shared preference and cache
     */
    fun addCustomRingtone(uri: Uri, title: String): CustomRingtone {
        // If the uri is already present in an existing ringtone, do nothing.
        val existing = getCustomRingtone(uri)
        if (existing != null) {
            return existing
        }

        val ringtone = customRingtoneDAO.addCustomRingtone(uri, title)
        ringtoneCache.add(ringtone)

        return ringtone
    }

    /**
     * Delete a custom ringtone in both shared preference and cache
     */
    fun removeCustomRingtone(uri: Uri) {
        getCustomRingtone(uri)?.let {
            customRingtoneDAO.removeCustomRingtone(it.id)
            ringtoneCache.remove(it)
        }
    }

    /**
     * Get all custom ringtones selected by users
     * @return an immutable list of ringtones that users select
     */
    fun getCustomRingtones(): List<Ringtone> = ringtoneCache.map {
        Ringtone(
            it.uri,
            it.title,
            /**
             * If it exists, it means you have READ_EXTERNAL_STORAGE permission and we've found it.
             * Otherwise, we may use SAF and don't have READ_EXTERNAL_STORAGE permission, but
             * we can still access it if we have Uri permission.
             */
            isValid = it.exists && if (requireUriPermission) {
                it.hasPermissions
            } else {
                true
            }
        )
    }

    private fun getCustomRingtone(uri: Uri) = ringtoneCache.find { it.uri == uri }
}

private fun Context.getCustomRingtoneSharedPrefs(): SharedPreferences {
    return safeContext().getSharedPreferences(
        "music_picker_prefs", Context.MODE_PRIVATE
    )
}

private fun ContentResolver.canFind(uri: Uri): Boolean {
    return try {
        query(uri, arrayOf(BaseColumns._ID), null, null, null)?.use {
            it.moveToFirst() && it.count == 1
        } ?: false
    } catch (e: SecurityException) {
        // We even don't have the permission to query.
        e.printStackTrace()
        false
    }
}
