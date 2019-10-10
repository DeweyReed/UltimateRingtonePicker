package xyz.aprildown.ultimateringtonepicker.data.folder

import android.content.Context
import xyz.aprildown.ultimateringtonepicker.isQOrLater

internal class FolderRetrieverCompat(
    private val context: Context
) : IFolderRetriever by when {
    isQOrLater() -> FolderRetrieverQ(context)
    else -> FolderRetrieverPreQ(context)
}
