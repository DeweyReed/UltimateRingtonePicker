package xyz.aprildown.ultimateringtonepicker.data

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.media.RingtoneManager
import android.net.Uri
import androidx.collection.ArrayMap
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RINGTONE_URI_NULL

internal class SystemRingtoneModel(private val context: Context) {

    /**
     * Maps ringtone uri to ringtone title; looking up a title from scratch is expensive.
     */
    private val ringtoneTitles = ArrayMap<Uri, String>(16)

    fun preloadRingtoneTitles(types: List<Int>) {
        // Early return if the cache is already primed.
        if (!ringtoneTitles.isEmpty) {
            return
        }

        val supportedTypes = listOf(
            RingtoneManager.TYPE_RINGTONE,
            RingtoneManager.TYPE_NOTIFICATION,
            RingtoneManager.TYPE_ALARM
        )
        for (type in types) {
            if (type !in supportedTypes) continue

            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(type)
            // Cache a title for each system ringtone.
            try {
                val cursor = ringtoneManager.cursor
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val ringtoneTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                    val ringtoneUri = ringtoneManager.getRingtoneUri(cursor.position)
                    ringtoneTitles[ringtoneUri] = ringtoneTitle
                    cursor.moveToNext()
                }
            } catch (ignored: Throwable) {
                // best attempt only
            }
        }
    }

    fun getRingtoneTitle(uri: Uri): String {
        // Special case: no ringtone has a title of "Silent".
        if (RINGTONE_URI_NULL == uri) {
            return context.getString(R.string.urp_silent_ringtone_title)
        }

        // Check the cache.
        var title: String? = ringtoneTitles[uri]

        if (title == null) {
            // This is slow because a media player is created during Ringtone object creation.
            title = RingtoneManager.getRingtone(context, uri)?.getTitle(context)
                ?: context.getString(R.string.urp_unknown_ringtone_title)
            // Cache the title for later use.
            ringtoneTitles[uri] = title
        }
        requireNotNull(title)
        return title
    }

    /**
     * Retrieve all system [type] ringtones
     */
    fun getRingtones(type: Int): List<Uri> {
        if (type !in listOf(
                RingtoneManager.TYPE_RINGTONE,
                RingtoneManager.TYPE_NOTIFICATION,
                RingtoneManager.TYPE_ALARM
            )
        ) return emptyList()

        val result = mutableListOf<Uri>()

        // Fetch the standard system ringtones.
        val ringtoneManager = RingtoneManager(context)
        ringtoneManager.setType(type)

        val systemRingtoneCursor: Cursor = try {
            ringtoneManager.cursor
        } catch (e: Exception) {
            // Could not get system ringtone cursor
            MatrixCursor(arrayOf())
        }

        // Add an item holder for each system ringtone.
        for (i in 0 until systemRingtoneCursor.count) {
            result.add(ringtoneManager.getRingtoneUri(i))
        }

        return result
    }
}