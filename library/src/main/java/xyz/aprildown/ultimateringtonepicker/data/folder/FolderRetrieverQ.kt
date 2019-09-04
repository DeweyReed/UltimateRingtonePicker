package xyz.aprildown.ultimateringtonepicker.data.folder

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_FOLDER
import xyz.aprildown.ultimateringtonepicker.data.Category
import xyz.aprildown.ultimateringtonepicker.data.Ringtone

@RequiresApi(Build.VERSION_CODES.Q)
internal class FolderRetrieverQ(private val context: Context) :
    IFolderRetriever {
    override fun getFolders(): List<Category> {
        val data = mutableListOf<Category>()
        val allIds = mutableSetOf<Long>()
        // This is hack. Is there any better way?
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media.BUCKET_ID,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
            ),
            null,
            null,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        )?.use {
            it.moveToPosition(-1)
            while (it.moveToNext()) {
                val bucketId =
                    it.getLong(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID))
                // TODO
                // val numOfSongs = it.getInt(it.getColumnIndexOrThrow("dataCount"))
                val bucketName =
                    it.getString(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME))
                if (bucketId !in allIds) {
                    data.add(
                        Category(
                            CATEGORY_TYPE_FOLDER,
                            bucketId,
                            bucketName,
                            0
                        )
                    )
                    allIds.add(bucketId)
                }
            }
        }
        return data
    }

    override fun getRingtonesFromFolder(folderId: Long): List<Ringtone> {
        val data = mutableListOf<Ringtone>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE
            ),
            "${MediaStore.Audio.Media.BUCKET_ID} = $folderId",
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
