package xyz.aprildown.ultimateringtonepicker.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import xyz.aprildown.ultimateringtonepicker.data.folder.RingtoneFolderRetrieverCompat
import xyz.aprildown.ultimateringtonepicker.isQOrLater

internal class DeviceRingtoneModel(private val context: Context) {

    fun getAllDeviceRingtones(): List<Ringtone> {
        val data = mutableListOf<Ringtone>()
        try {
            context.contentResolver.query(
                if (isQOrLater()) {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                },
                arrayOf(
                    MediaStore.Audio.AudioColumns._ID,
                    MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.AudioColumns.ARTIST_ID,
                    MediaStore.Audio.AudioColumns.ALBUM_ID
                ),
                """
                    ${MediaStore.Audio.AudioColumns.IS_PODCAST} == 0 AND
                    (
                        ${MediaStore.Audio.AudioColumns.IS_MUSIC} != 0 OR
                        ${MediaStore.Audio.AudioColumns.IS_ALARM} != 0 OR
                        ${MediaStore.Audio.AudioColumns.IS_NOTIFICATION} != 0 OR
                        ${MediaStore.Audio.AudioColumns.IS_RINGTONE} != 0
                    )
                """.trimIndent(),
                null,
                MediaStore.Audio.Media.TITLE_KEY
            )?.use { cursor ->
                cursor.moveToPosition(-1)
                while (cursor.moveToNext()) {
                    try {
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID))
                        )
                        val title =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE))
                        val artistId =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST_ID))
                        val albumId =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID))
                        data.add(Ringtone(uri, title, artistId, albumId))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    private fun getArtists(): List<Category> {
        val data = mutableListOf<Category>()
        try {
            context.contentResolver.query(
                if (isQOrLater()) {
                    MediaStore.Audio.Artists.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
                },
                arrayOf(
                    MediaStore.Audio.Artists._ID,
                    MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                ),
                null,
                null,
                MediaStore.Audio.Artists.ARTIST_KEY
            )?.use { cursor ->
                cursor.moveToPosition(-1)
                while (cursor.moveToNext()) {
                    try {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
                        val name =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
                        val numOfTracks =
                            cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
                        data.add(
                            Category(
                                type = UltimateRingtonePicker.RingtoneCategoryType.Artist,
                                id = id,
                                name = name,
                                numberOfSongs = numOfTracks
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    private fun getAlbums(): List<Category> {
        val data = mutableListOf<Category>()
        try {
            context.contentResolver.query(
                if (isQOrLater()) {
                    MediaStore.Audio.Albums.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
                },
                arrayOf(
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS
                ),
                null,
                null,
                MediaStore.Audio.Albums.ALBUM_KEY
            )?.use { cursor ->
                cursor.moveToPosition(-1)
                while (cursor.moveToNext()) {
                    try {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))
                        val name =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
                        val numOfSongs =
                            cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
                        data.add(
                            Category(
                                type = UltimateRingtonePicker.RingtoneCategoryType.Album,
                                id = id,
                                name = name,
                                numberOfSongs = numOfSongs
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    private fun getFolders(): List<Category> {
        return RingtoneFolderRetrieverCompat(context).getRingtoneFolders()
    }

    fun getFolderRingtones(folderId: Long): List<Ringtone> {
        return RingtoneFolderRetrieverCompat(context).getRingtonesFromFolder(folderId)
    }

    fun getCategories(
        categoryType: UltimateRingtonePicker.RingtoneCategoryType
    ): List<Category> = when (categoryType) {
        UltimateRingtonePicker.RingtoneCategoryType.Artist -> getArtists()
        UltimateRingtonePicker.RingtoneCategoryType.Album -> getAlbums()
        UltimateRingtonePicker.RingtoneCategoryType.Folder -> getFolders()
        else -> throw IllegalArgumentException("Wrong category categoryType: $categoryType")
    }
}
