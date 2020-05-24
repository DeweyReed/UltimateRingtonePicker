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
import kotlinx.coroutines.withContext
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

    private val customRingtoneModel by lazy {
        CustomRingtoneModel(
            context = application,
            requireUriPermission = settings.systemRingtonePicker?.customSection?.useSafSelect == true
        )
    }
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

    private val categories by lazy {
        ArrayMap<UltimateRingtonePicker.RingtoneCategoryType, MutableLiveData<List<Category>>>().also { map ->
            arrayOf(
                UltimateRingtonePicker.RingtoneCategoryType.Artist,
                UltimateRingtonePicker.RingtoneCategoryType.Album,
                UltimateRingtonePicker.RingtoneCategoryType.Folder
            ).forEach { categoryType ->
                map[categoryType] = MutableLiveData()
            }
            viewModelScope.launch(Dispatchers.IO) {
                repeat(map.size) { index ->
                    val categoryType = map.keyAt(index)
                    val liveData = map.valueAt(index)
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
        viewModelScope.launch {

            currentSelectedUris.addAll(settings.preSelectUris)

            val systemRingtonePicker =
                settings.systemRingtonePicker
            val customSection =
                systemRingtonePicker?.customSection

            if (customSection != null) {
                withContext(Dispatchers.IO) {
                    customRingtones.addAll(customRingtoneModel.getCustomRingtones())
                }
            }

            val ringtoneTypes = systemRingtonePicker?.ringtoneTypes
            if (ringtoneTypes?.isNotEmpty() == true) {
                withContext(Dispatchers.IO) {
                    systemRingtoneModel.preloadRingtoneTitles(ringtoneTypes)
                    ringtoneTypes.forEach { ringtoneType ->
                        systemRingtones[ringtoneType] =
                            systemRingtoneModel.getRingtones(ringtoneType).map {
                                Ringtone(it, systemRingtoneModel.getRingtoneTitle(it))
                            }
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
        customRingtoneModel.addCustomRingtone(uri, title)
    }

    fun deleteCustomRingtone(uri: Uri) {
        customRingtoneModel.removeCustomRingtone(uri)
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
        customRingtones.addAll(customRingtoneModel.getCustomRingtones())

        systemRingtoneLoadedEvent.value = true
    }

    fun consumeFirstLoad(): Boolean {
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

    fun onFinalSelection(selectedRingtones: List<Ringtone>) {
        finalSelection.value = selectedRingtones
    }

    fun getRingtoneLiveData(
        categoryType: UltimateRingtonePicker.RingtoneCategoryType,
        categoryId: Long
    ): LiveData<List<Ringtone>> {
        return if (categoryType == UltimateRingtonePicker.RingtoneCategoryType.Folder) {
            folderRingtones[categoryId] ?: MutableLiveData<List<Ringtone>>().also {
                folderRingtones[categoryId] = it
                viewModelScope.launch(Dispatchers.IO) {
                    it.postValue(deviceRingtoneModel.getFolderRingtones(categoryId))
                }
            }
        } else {
            Transformations.map(deviceRingtones) { allRingtones ->
                when (categoryType) {
                    UltimateRingtonePicker.RingtoneCategoryType.All -> allRingtones
                    UltimateRingtonePicker.RingtoneCategoryType.Artist ->
                        allRingtones.filter { it.artistId == categoryId }
                    UltimateRingtonePicker.RingtoneCategoryType.Album ->
                        allRingtones.filter { it.albumId == categoryId }
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    fun getCategoryLiveData(categoryType: UltimateRingtonePicker.RingtoneCategoryType): LiveData<List<Category>>? {
        return categories[categoryType]
    }

    override fun onCleared() {
        super.onCleared()
        stopPlaying()
    }
}
