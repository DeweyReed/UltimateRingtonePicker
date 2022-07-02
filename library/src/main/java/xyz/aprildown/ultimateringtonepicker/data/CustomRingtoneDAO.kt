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

package xyz.aprildown.ultimateringtonepicker.data

import android.content.SharedPreferences
import android.net.Uri
import java.util.ArrayList

/**
 * This class encapsulates the transfer of data between [CustomRingtone] domain objects and
 * their permanent storage in [SharedPreferences].
 */
internal class CustomRingtoneDAO(private val prefs: SharedPreferences) {

    /**
     * @param uri points to an audio file located on the file system
     * @param title the title of the audio content at the given {@code uri}
     * @return the newly added custom ringtone
     */
    fun addCustomRingtone(uri: Uri, title: String, duration:String): CustomRingtone {
        val id = prefs.getLong(NEXT_RINGTONE_ID, 0)
        val ids = getRingtoneIds()
        ids.add(id.toString())

        prefs.edit()
            .putString(RINGTONE_URI + id, uri.toString())
            .putString(RINGTONE_TITLE + id, title)
                .putString(RINGTONE_LENGTH + id, duration)
            .putLong(NEXT_RINGTONE_ID, id + 1)
            .putStringSet(RINGTONE_IDS, ids)
            .apply()

        return CustomRingtone(id, uri, title, duration)
    }

    /**
     * @param id identifies the ringtone to be removed
     */
    fun removeCustomRingtone(id: Long) {
        val ids = getRingtoneIds()
        ids.remove(id.toString())

        val editor = prefs.edit()
        editor.remove(RINGTONE_URI + id)
        editor.remove(RINGTONE_TITLE + id)
        editor.remove(RINGTONE_LENGTH + id)
        if (ids.isEmpty()) {
            editor.remove(RINGTONE_IDS)
            editor.remove(NEXT_RINGTONE_ID)
        } else {
            editor.putStringSet(RINGTONE_IDS, ids)
        }
        editor.apply()
    }

    /**
     * @return a list of all known custom ringtones
     */
    fun getCustomRingtones(): MutableList<CustomRingtone> {
        val ids = prefs.getStringSet(RINGTONE_IDS, null) ?: return mutableListOf()
        val ringtones = ArrayList<CustomRingtone>(ids.size)

        for (id in ids) {
            val idLong = id.toLongOrNull() ?: continue
            val uri = Uri.parse(prefs.getString(RINGTONE_URI + id, null) ?: continue)
            val title = prefs.getString(RINGTONE_TITLE + id, null) ?: continue
            val duration = prefs.getString(RINGTONE_LENGTH + id, null) ?: continue
            ringtones.add(CustomRingtone(idLong, uri, title, duration))
        }

        return ringtones
    }

    private fun getRingtoneIds(): MutableSet<String> {
        return prefs.getStringSet(RINGTONE_IDS, null) ?: mutableSetOf()
    }

    companion object {
        /**
         * Key to a preference that stores the set of all custom ringtone ids.
         */
        private const val RINGTONE_IDS = "music_ids"

        /**
         * Key to a preference that stores the next unused ringtone id.
         */
        private const val NEXT_RINGTONE_ID = "next_music_id"

        /**
         * Prefix for a key to a preference that stores the URI associated with the ringtone id.
         */
        private const val RINGTONE_URI = "music_uri_"

        /**
         * Prefix for a key to a preference that stores the title associated with the ringtone id.
         */
        private const val RINGTONE_TITLE = "music_title_"

        private const val RINGTONE_LENGTH = "music_length"
    }
}