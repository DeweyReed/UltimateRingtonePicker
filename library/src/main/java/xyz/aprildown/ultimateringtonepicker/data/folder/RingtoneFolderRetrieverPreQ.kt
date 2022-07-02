package xyz.aprildown.ultimateringtonepicker.data.folder

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import xyz.aprildown.ultimateringtonepicker.data.Category
import xyz.aprildown.ultimateringtonepicker.data.Ringtone

internal class RingtoneFolderRetrieverPreQ(private val context: Context) : RingtoneFolderRetriever {
    override fun getRingtoneFolders(): List<Category> {
        val data = mutableListOf<Category>()
        try {
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
            )?.use { cursor ->
                cursor.moveToPosition(-1)
                while (cursor.moveToNext()) {
                    try {
                        val parentId =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.PARENT))
                        val numOfSongs =
                            cursor.getInt(cursor.getColumnIndexOrThrow("dataCount"))

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
                                try {
                                    val parentTitle = parentCursor.getString(
                                        parentCursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)
                                    )
                                    if (parentTitle != null) {
                                        data.add(
                                            Category(
                                                type = UltimateRingtonePicker.RingtoneCategoryType.Folder,
                                                id = parentId,
                                                name = parentTitle,
                                                numberOfSongs = numOfSongs
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
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

    override fun getRingtonesFromFolder(folderId: Long): List<Ringtone> {
        val data = mutableListOf<Ringtone>()
        try {
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
            )?.use { cursor: Cursor ->
                cursor.moveToPosition(-1)
                while (cursor.moveToNext()) {
                    try {
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        )
                        val title =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                        data.add(Ringtone(uri, title,"109-RPQ"))
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
}
