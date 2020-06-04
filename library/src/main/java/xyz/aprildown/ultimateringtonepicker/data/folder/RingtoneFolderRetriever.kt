package xyz.aprildown.ultimateringtonepicker.data.folder

import xyz.aprildown.ultimateringtonepicker.data.Category
import xyz.aprildown.ultimateringtonepicker.data.Ringtone

internal interface RingtoneFolderRetriever {
    fun getRingtoneFolders(): List<Category>
    fun getRingtonesFromFolder(folderId: Long): List<Ringtone>
}
