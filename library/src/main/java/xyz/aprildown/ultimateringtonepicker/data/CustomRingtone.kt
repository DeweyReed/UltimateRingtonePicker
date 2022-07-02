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

import android.net.Uri

/**
 * A read-only domain object representing a custom music chosen from the file system.
 */
internal data class CustomRingtone(
    /**
     * The unique identifier of the custom music.
     */
    val id: Long,
    /**
     * The uri that allows playback of the music.
     */
    val uri: Uri,
    /**
     * The title describing the file at the given uri; typically the file name.
     */
    val title: String,

    /**
     * This duration saves the length of the media file
     */
    val duration: String
) {
    /**
     * {@code true} iff the application has permission to read the content of {@code mUri uri}.
     */
    var hasPermissions: Boolean = true

    var canBeQueried: Boolean = true
}