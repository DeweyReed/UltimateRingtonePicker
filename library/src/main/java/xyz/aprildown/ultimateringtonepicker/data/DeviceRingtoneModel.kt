package xyz.aprildown.ultimateringtonepicker.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import xyz.aprildown.ultimateringtonepicker.data.folder.RingtoneFolderRetrieverCompat

internal class DeviceRingtoneModel(private val context: Context) {

    fun getAllDeviceRingtones(): List<Ringtone> {
        val data = mutableListOf<Ringtone>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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
        )?.use {
            it.moveToPosition(-1)
            while (it.moveToNext()) {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID))
                )
                val title =
                    it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE))
                val artistId =
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST_ID))
                val albumId =
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID))
                data.add(Ringtone(uri, title, artistId, albumId))
            }
        }
        return data
    }

    private fun getArtists(): List<Category> {
        val data = mutableListOf<Category>()
        context.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS
            ),
            null,
            null,
            MediaStore.Audio.Artists.ARTIST_KEY
        )?.use {
            it.moveToPosition(-1)
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
                val name = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
                val numOfTracks =
                    it.getInt(it.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
                data.add(
                    Category(
                        type = UltimateRingtonePicker.RingtoneCategoryType.Artist,
                        id = id,
                        name = name,
                        numberOfSongs = numOfTracks
                    )
                )
            }
        }
        return data
    }

    private fun getAlbums(): List<Category> {
        val data = mutableListOf<Category>()
        context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
            ),
            null,
            null,
            MediaStore.Audio.Albums.ALBUM_KEY
        )?.use {
            it.moveToPosition(-1)
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))
                val name = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
                val numOfSongs =
                    it.getInt(it.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
                data.add(
                    Category(
                        type = UltimateRingtonePicker.RingtoneCategoryType.Album,
                        id = id,
                        name = name,
                        numberOfSongs = numOfSongs
                    )
                )
            }
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
