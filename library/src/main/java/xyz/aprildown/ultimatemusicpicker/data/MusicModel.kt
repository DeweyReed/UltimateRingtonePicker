package xyz.aprildown.ultimatemusicpicker.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import android.media.RingtoneManager
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import androidx.collection.ArrayMap
import xyz.aprildown.ultimatemusicpicker.*

/**
 * All music data is accessed via this model.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
internal class MusicModel(private val context: Context) {

    companion object {
        internal fun Context.getCustomMusicSharedPrefs(): SharedPreferences {
            return safeContext().getSharedPreferences(
                "music_picker_prefs", Context.MODE_PRIVATE
            )
        }
    }

    /**
     * Stores all custom musics that users select
     */
    private val customMusicDAO = CustomMusicDAO(context.getCustomMusicSharedPrefs())

    /**
     * Maps music uri to music title; looking up a title from scratch is expensive.
     * Includes all types system ringtone titles. Loaded using [RingtoneManager]
     */
    private val musicTitles = ArrayMap<Uri, String>(16)

    /**
     * Local custom musics cache
     */
    private val localCustomMusics: MutableList<CustomMusic> by lazy {
        customMusicDAO.getCustomMusics()
    }

    /**
     * User selects a custom music and we store it in both shared preference and cache
     */
    fun addCustomMusic(uri: Uri, title: String): CustomMusic {
        // If the uri is already present in an existing ringtone, do nothing.
        val existing = getCustomMusic(uri)
        if (existing != null) {
            return existing
        }

        val ringtone = customMusicDAO.addCustomMusic(uri, title)
        localCustomMusics.add(ringtone)

        localCustomMusics.sortWithCollator()
        return ringtone
    }

    /**
     * Delete a custom music in both shared preference and cache
     */
    fun removeCustomMusic(uri: Uri) {
        getCustomMusic(uri)?.let {
            customMusicDAO.removeCustomMusic(it.id)
            localCustomMusics.remove(it)
        }
    }

    /**
     * Get all custom musics selected by users
     * @return an immutable list of musics that users select
     */
    fun getCustomMusics(): List<CustomMusic> {
        return localCustomMusics.toList()
    }

    /**
     * Get all musics from external storage
     * @return list of all musics. ids are all 0 since they're not stored in shared preference
     */
    @SuppressLint("InlinedApi")
    @RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun getAvailableCustomMusics(): List<CustomMusic> {
        val musics = mutableListOf<CustomMusic>()

        val contentResolver = context.contentResolver ?: return musics
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            uri,
            arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE),
            "${MediaStore.Audio.Media.IS_PODCAST} == 0 AND (" +
                    "${MediaStore.Audio.Media.IS_MUSIC} != 0 OR " +
                    "${MediaStore.Audio.Media.IS_ALARM} != 0 OR " +
                    "${MediaStore.Audio.Media.IS_NOTIFICATION} != 0 OR " +
                    "${MediaStore.Audio.Media.IS_RINGTONE} != 0" +
                    ")",
            null,
            MediaStore.Audio.Media.TITLE
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                do {
                    val thisId = cursor.getLong(idColumn)
                    val thisTitle = cursor.getString(titleColumn)
                    val customMusic = CustomMusic(
                        thisId,
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            thisId
                        ),
                        thisTitle
                    )
                    musics.add(customMusic)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        musics.sortWithCollator()

        return musics
    }

    /**
     * Preload all ringtone titles
     */
    fun loadMusicTitles(@UltimateMusicPicker.Companion.MusicType vararg types: Int) {
        // Early return if the cache is already primed.
        if (!musicTitles.isEmpty) {
            return
        }

        for (type in types) {
            if (type == UltimateMusicPicker.TYPE_MUSIC) continue
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(type)

            // Cache a title for each system ringtone.
            try {
                val cursor = ringtoneManager.cursor
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val ringtoneTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                    val ringtoneUri = ringtoneManager.getRingtoneUri(cursor.position)
                    musicTitles[ringtoneUri] = ringtoneTitle
                    cursor.moveToNext()
                }
            } catch (ignored: Throwable) {
                // best attempt only
                // LogUtils.e("Error loading ringtone title cache", ignored);
            }
        }
    }

    /**
     * Find an [Uri]'s title from [localCustomMusics] and [musicTitles]
     */
    fun getMusicTitle(uri: Uri): String {
        // Special case: no ringtone has a title of "Silent".
        if (NO_MUSIC_URI == uri) {
            return context.getString(R.string.silent_ringtone_title)
        }

        // If the ringtone is custom, it has its own title.
        val customMusic = getCustomMusic(uri)
        if (customMusic != null) {
            return customMusic.title
        }

        // Check the cache.
        var title: String? = musicTitles[uri]

        if (title == null) {
            // This is slow because a media player is created during Ringtone object creation.
            title = RingtoneManager.getRingtone(context, uri)?.getTitle(context)
                    ?: context.getString(R.string.unknown_ringtone_title)
            // Cache the title for later use.
            musicTitles[uri] = title
        }
        return title!!
    }

    /**
     * Retrieve all system [type] ringtones
     */
    fun getRingtones(@UltimateMusicPicker.Companion.MusicType type: Int): MutableList<Uri> {
        val result = mutableListOf<Uri>()
        if (type == UltimateMusicPicker.TYPE_MUSIC) return result

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

    private fun getCustomMusic(uri: Uri) = localCustomMusics.find { it.uri == uri }
}