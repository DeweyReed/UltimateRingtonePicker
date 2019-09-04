package xyz.aprildown.ultimateringtonepicker.data.folder

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_FOLDER
import xyz.aprildown.ultimateringtonepicker.data.Category
import xyz.aprildown.ultimateringtonepicker.data.Ringtone

internal class FolderRetriever(private val context: Context) :
    IFolderRetriever {
    override fun getFolders(): List<Category> {
        val data = mutableListOf<Category>()
        // This is hack. Is there any better way?
        @Suppress("DEPRECATION")
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(
                MediaStore.Files.FileColumns.PARENT,
                // MediaStore.Files.FileColumns.DISPLAY_NAME,
                "COUNT(${MediaStore.Files.FileColumns.DATA}) AS dataCount"
            ),
            """
                ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO}
                ) GROUP BY (${MediaStore.Files.FileColumns.PARENT}
            """.trimIndent(),
            null,
            MediaStore.Files.FileColumns.TITLE
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
                    parentCursor.moveToPosition(-1)
                    while (parentCursor.moveToNext()) {
                        val parentTitle = parentCursor.getString(
                            parentCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)
                        )
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

    override fun getRingtonesFromFolder(folderId: Long): List<Ringtone> {
        val data = mutableListOf<Ringtone>()
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE
            ),
            """
                ${MediaStore.Files.FileColumns.PARENT} = $folderId AND
                (
                    ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'audio%' OR
                    ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'application/ogg'
                )
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
}
