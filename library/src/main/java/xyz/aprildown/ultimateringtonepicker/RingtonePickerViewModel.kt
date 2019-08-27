package xyz.aprildown.ultimateringtonepicker

import android.content.Context
import android.net.Uri
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
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
    context: Context,
    val settings: UltimateRingtonePicker.Settings
) : ViewModel() {

    private val mediaPlayer by lazy { AsyncRingtonePlayer(context) }

    val initialSelection = mutableListOf<Ringtone>()

    private val customRingtoneModel by lazy { CustomRingtoneModel(context) }
    val customRingtones = mutableSetOf<Ringtone>()
    private val systemRingtoneModel by lazy { SystemRingtoneModel(context) }
    val systemRingtones = ArrayMap<Int, List<Ringtone>>()

    val dataLoadedEvent = MutableLiveData<Boolean>()

    val totalSelection = MutableLiveData<List<Ringtone>>()

    private val deviceRingtoneModel by lazy { DeviceRingtoneModel(context) }

    /**
     * All device ringtones. Artist and Album ringtones are filtered from this.
     */
    private val deviceRingtones = MutableLiveData<List<Ringtone>>()

    /**
     * Artists, albums and folders
     */
    private val categories = ArrayMap<Int, MutableLiveData<List<Category>>>()

    /**
     * Since folder ringtones have a different load mechanism, we use another map to host them.
     */
    private val folderRingtones = ArrayMap<Long, MutableLiveData<List<Ringtone>>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            check(initialSelection.isEmpty())

            val preSelectUris = settings.preSelectUris

            if (settings.showCustomRingtone) {
                customRingtones.addAll(customRingtoneModel.getCustomRingtones().map {
                    Ringtone(it.uri, it.title).also { ringtone ->
                        if (ringtone.uri in preSelectUris) {
                            initialSelection.add(ringtone)
                        }
                    }
                })
            }

            val ringtoneTypes = settings.ringtoneTypes
            // System ringtones doesn't change so we don't repeat loading.
            if (systemRingtones.isEmpty && ringtoneTypes.isNotEmpty()) {
                systemRingtoneModel.preloadRingtoneTitles(ringtoneTypes)
                settings.ringtoneTypes.forEach { ringtoneType ->
                    systemRingtones[ringtoneType] =
                        systemRingtoneModel.getRingtones(ringtoneType).map {
                            Ringtone(
                                it,
                                systemRingtoneModel.getRingtoneTitle(it)
                            ).also { ringtone ->
                                if (ringtone.uri in preSelectUris) {
                                    initialSelection.add(ringtone)
                                }
                            }
                        }
                }

                dataLoadedEvent.postValue(true)
            }
        }
    }

    fun startPlaying(uri: Uri) {
        mediaPlayer.stop()
        mediaPlayer.play(uri, true, settings.streamType)
    }

    fun stopPlaying() {
        mediaPlayer.stop()
    }

    fun addCustomRingtone(title: String, uri: Uri) {
        customRingtoneModel.addCustomMusic(uri, title)
    }

    fun deleteCustomRingtone(uri: Uri) {
        customRingtoneModel.removeCustomMusic(uri)
    }

    fun onDeviceSelection(selectedRingtones: List<Ringtone>) {
        initialSelection.addAll(selectedRingtones)

        selectedRingtones.forEach {
            addCustomRingtone(it.title, it.uri)
        }
        customRingtones.addAll(selectedRingtones)

        dataLoadedEvent.value = true
    }

    fun onTotalSelection(selectedRingtones: List<Ringtone>) {
        totalSelection.value = selectedRingtones
    }

    fun initDeviceRingtones() = viewModelScope.launch(Dispatchers.Default) {
        if (categories.isEmpty && deviceRingtones.value == null) {

            deviceRingtones.postValue(deviceRingtoneModel.getAllDeviceRingtones())

            arrayOf(
                CATEGORY_TYPE_ARTIST,
                CATEGORY_TYPE_ALBUM,
                CATEGORY_TYPE_FOLDER
            ).forEach { categoryType ->
                categories[categoryType] = MutableLiveData<List<Category>>().apply {
                    postValue(deviceRingtoneModel.getCategories(categoryType))
                }
            }
        }
    }

    fun getRingtoneLiveData(ringtoneType: Int, extraId: Long): LiveData<List<Ringtone>> {
        return if (ringtoneType == RINGTONE_TYPE_FOLDER) {
            folderRingtones[extraId] ?: MutableLiveData<List<Ringtone>>().also {
                folderRingtones[extraId] = it
                viewModelScope.launch {
                    it.value = deviceRingtoneModel.getFolderRingtones(extraId)
                }
            }
        } else {
            Transformations.map(deviceRingtones) {
                it.filterWithType(ringtoneType, extraId)
            }
        }
    }

    fun getCategoryLiveData(categoryType: Int): LiveData<List<Category>> {
        return if (categories.containsKey(categoryType)) {
            categories[categoryType]!!
        } else {
            val liveData = MutableLiveData<List<Category>>()
            categories[categoryType] = liveData
            liveData
        }
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
