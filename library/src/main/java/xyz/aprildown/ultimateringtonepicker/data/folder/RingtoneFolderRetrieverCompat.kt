package xyz.aprildown.ultimateringtonepicker.data.folder

import android.content.Context
import xyz.aprildown.ultimateringtonepicker.isQOrLater

internal class RingtoneFolderRetrieverCompat(
    private val context: Context
) : RingtoneFolderRetriever by when {
    isQOrLater() -> RingtoneFolderRetrieverQ(context)
    else -> RingtoneFolderRetrieverPreQ(context)
}
