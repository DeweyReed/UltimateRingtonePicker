package xyz.aprildown.ultimateringtonepicker

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.aprildown.ultimateringtonepicker.data.Category
import xyz.aprildown.ultimateringtonepicker.data.CustomRingtoneModel
import xyz.aprildown.ultimateringtonepicker.data.DeviceRingtoneModel
import xyz.aprildown.ultimateringtonepicker.data.Ringtone
import xyz.aprildown.ultimateringtonepicker.data.SystemRingtoneModel
import xyz.aprildown.ultimateringtonepicker.music.AsyncRingtonePlayer

internal class RingtonePickerViewModel(
    application: Application,
    val settings: UltimateRingtonePicker.Settings
) : AndroidViewModel(application) {

    private val mediaPlayer by lazy { AsyncRingtonePlayer(application) }

    val currentSelectedUris = mutableSetOf<Uri>()

    private val customRingtoneModel by lazy { CustomRingtoneModel(application) }
    val customRingtones = mutableSetOf<Ringtone>()
    private val systemRingtoneModel by lazy { SystemRingtoneModel(application) }
    val systemRingtones = ArrayMap<Int, List<Ringtone>>()

    /**
     * Use this event to get [customRingtones] and [systemRingtones].
     */
    val systemRingtoneLoadedEvent = MutableLiveData<Boolean>()
    private var firstLoad: Boolean = true

    val finalSelection = MutableLiveData<List<Ringtone>>()

    private val deviceRingtoneModel by lazy { DeviceRingtoneModel(application) }

    /**
     * All device ringtones. Artist and Album ringtones are filtered from this.
     */
    private val deviceRingtones by lazy {
        val result = MutableLiveData<List<Ringtone>>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(deviceRingtoneModel.getAllDeviceRingtones())
        }
        result
    }

    /**
     * Artists, albums and folders
     * Key: [CATEGORY_TYPE_ARTIST], [CATEGORY_TYPE_ALBUM], [CATEGORY_TYPE_FOLDER] and no more.
     */
    private val categories by lazy {
        ArrayMap<Int, MutableLiveData<List<Category>>>().also { map ->
            arrayOf(
                CATEGORY_TYPE_ARTIST,
                CATEGORY_TYPE_ALBUM,
                CATEGORY_TYPE_FOLDER
            ).forEach { categoryType ->
                map[categoryType] = MutableLiveData()
            }
            viewModelScope.launch(Dispatchers.IO) {
                repeat(map.size) {
                    val categoryType = map.keyAt(it)
                    val liveData = map.valueAt(it)
                    liveData.postValue(deviceRingtoneModel.getCategories(categoryType))
                }
            }
        }
    }

    /**
     * Since folder ringtones have a different load mechanism, we use another map to host them.
     * Key: Folder id
     */
    private val folderRingtones = ArrayMap<Long, MutableLiveData<List<Ringtone>>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            currentSelectedUris.addAll(settings.preSelectUris)

            if (settings.showCustomRingtone) {
                customRingtones.addAll(customRingtoneModel.getCustomRingtones().map {
                    Ringtone(it.uri, it.title)
                })
            }

            val ringtoneTypes = settings.systemRingtoneTypes
            // System ringtones doesn't change so we don't repeat loading.
            if (systemRingtones.isEmpty && ringtoneTypes.isNotEmpty()) {
                systemRingtoneModel.preloadRingtoneTitles(ringtoneTypes)
                settings.systemRingtoneTypes.forEach { ringtoneType ->
                    systemRingtones[ringtoneType] =
                        systemRingtoneModel.getRingtones(ringtoneType).map {
                            Ringtone(it, systemRingtoneModel.getRingtoneTitle(it))
                        }
                }
            }

            systemRingtoneLoadedEvent.postValue(true)
        }
    }

    fun startPlaying(uri: Uri) {
        mediaPlayer.play(uri, true, settings.streamType)
    }

    fun stopPlaying() {
        mediaPlayer.stop()
    }

    private fun addCustomRingtone(title: String, uri: Uri) {
        customRingtoneModel.addCustomMusic(uri, title)
    }

    fun deleteCustomRingtone(uri: Uri) {
        customRingtoneModel.removeCustomMusic(uri)
    }

    fun onDeviceSelection(selectedRingtones: List<Ringtone>) {
        if (!settings.enableMultiSelect && selectedRingtones.isNotEmpty()) {
            require(selectedRingtones.size == 1)
            currentSelectedUris.clear()
        }
        currentSelectedUris.addAll(selectedRingtones.map { it.uri })

        selectedRingtones.forEach {
            addCustomRingtone(it.title, it.uri)
        }
        // In this way we can keep ringtone order. They're cached anyway.
        customRingtones.clear()
        customRingtones.addAll(customRingtoneModel.getCustomRingtones().map {
            Ringtone(it.uri, it.title)
        })

        systemRingtoneLoadedEvent.value = true
    }

    fun requireFirstLoad(): Boolean {
        val result = firstLoad
        firstLoad = false
        return result
    }

    fun onSafSelect(contentResolver: ContentResolver, uri: Uri) {
        // Take the long-term permission to read (playback) the audio at the uri.
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->

            if (!cursor.moveToFirst()) return@use

            var title: String? = null

            // If the file was a media file, return its title.
            val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            if (titleIndex != -1) {
                title = cursor.getString(titleIndex)
            } else {
                // If the file was a simple openable, return its display name.
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    var displayName = cursor.getString(displayNameIndex)
                    val dotIndex = displayName.lastIndexOf(".")
                    if (dotIndex > 0) {
                        displayName = displayName.substring(0, dotIndex)
                    }
                    title = displayName
                }
            }

            if (title != null) {
                onDeviceSelection(listOf(Ringtone(uri, title)))
            }
        }
    }

    fun onTotalSelection(selectedRingtones: List<Ringtone>) {
        finalSelection.value = selectedRingtones
    }

    fun getRingtoneLiveData(ringtoneType: Int, extraId: Long): LiveData<List<Ringtone>> {
        return if (ringtoneType == RINGTONE_TYPE_FOLDER) {
            folderRingtones[extraId] ?: MutableLiveData<List<Ringtone>>().also {
                folderRingtones[extraId] = it
                viewModelScope.launch(Dispatchers.IO) {
                    it.postValue(deviceRingtoneModel.getFolderRingtones(extraId))
                }
            }
        } else {
            Transformations.map(deviceRingtones) {
                it.filterWithType(ringtoneType, extraId)
            }
        }
    }

    fun getCategoryLiveData(categoryType: Int): LiveData<List<Category>> {
        return categories[categoryType]!!
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaying()
    }
}

private fun List<Ringtone>.filterWithType(
    ringtoneType: Int,
    extraId: Long
): List<Ringtone> = when (ringtoneType) {
    RINGTONE_TYPE_ALL -> this
    RINGTONE_TYPE_ARTIST -> this.filter { it.artistId == extraId }
    RINGTONE_TYPE_ALBUM -> this.filter { it.albumId == extraId }
    else -> throw IllegalArgumentException()
}
