/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.aprildown.ultimatemusicpicker.data

import android.content.SharedPreferences
import android.net.Uri
import xyz.aprildown.ultimatemusicpicker.sortWithCollator
import java.util.*

/**
 * This class encapsulates the transfer of data between [CustomMusic] domain objects and
 * their permanent storage in [SharedPreferences].
 */
internal class CustomMusicDAO(private val prefs: SharedPreferences) {

    companion object {
        /**
         * Key to a preference that stores the set of all custom music ids.
         */
        private const val MUSIC_IDS = "music_ids"

        /**
         * Key to a preference that stores the next unused music id.
         */
        private const val NEXT_MUSIC_ID = "next_music_id"

        /**
         * Prefix for a key to a preference that stores the URI associated with the music id.
         */
        private const val MUSIC_URI = "music_uri_"

        /**
         * Prefix for a key to a preference that stores the title associated with the music id.
         */
        private const val MUSIC_TITLE = "music_title_"
    }

    /**
     * @param uri   points to an audio file located on the file system
     * @param title the title of the audio content at the given `uri`
     * @return the newly added custom music
     */
    fun addCustomMusic(uri: Uri, title: String): CustomMusic {
        val id = prefs.getLong(NEXT_MUSIC_ID, 0)
        val ids = getMusicIds()
        ids.add(id.toString())

        prefs.edit()
            .putString(MUSIC_URI + id, uri.toString())
            .putString(MUSIC_TITLE + id, title)
            .putLong(NEXT_MUSIC_ID, id + 1)
            .putStringSet(MUSIC_IDS, ids)
            .apply()

        return CustomMusic(id, uri, title)
    }

    /**
     * @param id identifies the music to be removed
     */
    fun removeCustomMusic(id: Long) {
        val ids = getMusicIds()
        ids.remove(id.toString())

        val editor = prefs.edit()
        editor.remove(MUSIC_URI + id)
        editor.remove(MUSIC_TITLE + id)
        if (ids.isEmpty()) {
            editor.remove(MUSIC_IDS)
            editor.remove(NEXT_MUSIC_ID)
        } else {
            editor.putStringSet(MUSIC_IDS, ids)
        }
        editor.apply()
    }

    /**
     * @return a list of all known custom musics
     */
    fun getCustomMusics(): MutableList<CustomMusic> {
        val ids = prefs.getStringSet(MUSIC_IDS, null) ?: return mutableListOf()
        val musics = ArrayList<CustomMusic>(ids.size)

        for (id in ids) {
            val idLong = id.toLongOrNull() ?: continue
            val uri = Uri.parse(
                prefs.getString(MUSIC_URI + id, null) ?: continue
            )
            val title = prefs.getString(MUSIC_TITLE + id, null) ?: continue
            musics.add(CustomMusic(idLong, uri, title))
        }

        musics.sortWithCollator()
        return musics
    }

    private fun getMusicIds(): MutableSet<String> {
        return prefs.getStringSet(MUSIC_IDS, mutableSetOf()) ?: mutableSetOf()
    }
}