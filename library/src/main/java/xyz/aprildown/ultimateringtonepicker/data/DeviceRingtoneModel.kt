package xyz.aprildown.ultimateringtonepicker.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_ALBUM
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_ARTIST
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_FOLDER

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
                data.add(Category(CATEGORY_TYPE_ARTIST, id, name, numOfTracks))
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
                data.add(Category(CATEGORY_TYPE_ALBUM, id, name, numOfSongs))
            }
        }
        return data
    }

    // MediaStore.Audio.AudioColumns.GENRE is hidden API and I don't know how to do it.
    // private fun getGenres(): List<Category> {
    //     val data = mutableListOf<Category>()
    //     context.contentResolver.query(
    //         MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
    //         arrayOf(
    //             MediaStore.Audio.Genres._ID,
    //             MediaStore.Audio.Genres.NAME
    //         ),
    //         null,
    //         null,
    //         MediaStore.Audio.Genres.NAME
    //     )?.use {
    //         it.moveToPosition(-1)
    //         while (it.moveToNext()) {
    //             val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID))
    //             val name = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME))
    //             data.add(Category(CATEGORY_TYPE_ALBUM, id.toString(), name, 0))
    //         }
    //     }
    //     return data
    // }

    // region Folder

    private fun getFolders(): List<Category> {
        val data = mutableListOf<Category>()
        // This is hack. Is there any better way?
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.PARENT,
                // MediaStore.Files.FileColumns.DATA,
                "COUNT(${MediaStore.Files.FileColumns.DATA}) AS dataCount"
            ),
            """
                ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO}
                ) GROUP BY (${MediaStore.Files.FileColumns.PARENT}
            """.trimIndent(),
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use {
            it.moveToPosition(-1)
            while (it.moveToNext()) {
                val parentId =
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.PARENT))
                val numOfSongs = it.getInt(it.getColumnIndexOrThrow("dataCount"))
                context.contentResolver.query(
                    ContentUris.withAppendedId(
                        MediaStore.Files.getContentUri("external"),
                        parentId
                    ),
                    arrayOf(MediaStore.Files.FileColumns.TITLE),
                    null,
                    null,
                    null
                )?.use { parentCursor ->
                    if (parentCursor.count == 1) {
                        parentCursor.moveToPosition(0)
                        val parentTitle = parentCursor.getString(0)
                        if (parentTitle != null) {
                            data.add(
                                Category(
                                    CATEGORY_TYPE_FOLDER,
                                    parentId,
                                    parentTitle,
                                    numOfSongs
                                )
                            )
                        }
                    }
                }
            }
        }
        return data
    }

    fun getFolderRingtones(folderId: Long): List<Ringtone> {
        val data = mutableListOf<Ringtone>()
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE
            ),
            """
                ${MediaStore.Files.FileColumns.PARENT} = $folderId AND
                ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'audio%'
            """.trimIndent(),
            null,
            MediaStore.Audio.Media.TITLE_KEY
        )?.use {
            it.moveToPosition(-1)
            while (it.moveToNext()) {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                )
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                data.add(Ringtone(uri, title))
            }
        }
        return data
    }

    // endregion Folder

    fun getCategories(categoryType: Int): List<Category> = when (categoryType) {
        CATEGORY_TYPE_ARTIST -> getArtists()
        CATEGORY_TYPE_ALBUM -> getAlbums()
        CATEGORY_TYPE_FOLDER -> getFolders()
        else -> throw IllegalArgumentException("Wrong category categoryType: $categoryType")
    }
}

