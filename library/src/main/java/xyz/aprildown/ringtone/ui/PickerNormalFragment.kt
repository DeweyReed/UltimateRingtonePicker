package xyz.aprildown.ringtone.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import xyz.aprildown.ringtone.MUSIC_SILENT
import xyz.aprildown.ringtone.R
import xyz.aprildown.ringtone.UltimateMusicPicker
import xyz.aprildown.ringtone.data.CustomMusic
import xyz.aprildown.ringtone.data.MusicModel

internal class PickerNormalFragment : PickerBaseFragment() {

    private var indexOfMusicToRemove = RecyclerView.NO_POSITION

    override fun init() {
        registerForContextMenu(recyclerView)
    }

    override fun shouldShowContextMenu(): Boolean = true

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<MusicListItem>> {
        return viewModel.setting.run {
            MusicLoader(localContext, viewModel.musicModel, musicTypes,
                    hasDefault, defaultTitle, defaultUri, hasSilent, additional)
        }
    }

    override fun onLoadFinished(loader: Loader<List<MusicListItem>>, data: List<MusicListItem>?) {
        if (data != null) {
            musicAdapter.populateData(data)

            val toSelect: SoundItem? = getSoundItem(viewModel.selectedUri)

            if (toSelect != null) {
                toSelect.isSelected = true
                viewModel.selectedUri = toSelect.uri
                toSelect.notifyItemChanged(true)
            } else {
                parent.musicPlayer.stop()
                viewModel.selectedUri = null
                viewModel.isPreviewPlaying = false
            }
        }
    }

    fun onCustomPicked(uri: Uri, title: String) {
        viewModel.musicModel.addCustomMusic(uri, title)
        viewModel.selectedUri = uri
        viewModel.isPreviewPlaying = false
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        // remove selected item
        val toRemove = musicAdapter.getData().getOrNull(indexOfMusicToRemove)
        if (toRemove is SoundItem) {
            indexOfMusicToRemove = RecyclerView.NO_POSITION
            removeCustomMusic(toRemove.uri)
        }
        return true
    }

    override fun onItemClicked(viewHolder: RecyclerView.ViewHolder, id: Int) {
        when (id) {
            MusicAdapter.CLICK_NORMAL -> {
                onMusicItemClicked(viewHolder)
            }
            MusicAdapter.CLICK_ADD_NEW -> {
                parent.toCustom()
            }
            MusicAdapter.CLICK_LONG_PRESS -> {
                indexOfMusicToRemove = viewHolder.adapterPosition
            }
        }
    }

    private fun removeCustomMusic(uri: Uri) {
        viewModel.musicModel.removeCustomMusic(uri)
        getSoundItem(uri)?.let { toRemove ->
            if (toRemove.isSelected) {
                parent.stopPlayingMusic(toRemove, false)
                toRemove.notifyItemChanged()
                getSoundItem(viewModel.setting.defaultUri)?.let { default ->
                    default.isSelected = true
                    viewModel.selectedUri = default.uri
                    default.notifyItemChanged()
                }
            }
            musicAdapter.removeViewHolder(toRemove)
        }
    }

    private class MusicLoader(
            context: Context,
            private val musicModel: MusicModel,
            private val musicTypes: IntArray,
            private val hasDefault: Boolean,
            private val defaultTitle: String,
            private val defaultUri: Uri,
            private val hasSilent: Boolean,
            private val additional: List<Pair<String, Uri>>
    ) : AsyncTaskLoader<List<MusicListItem>>(context) {

        private lateinit var customMusics: List<CustomMusic>

        override fun onStartLoading() {
            super.onStartLoading()
            customMusics = musicModel.getCustomMusics()
            forceLoad()
        }

        override fun loadInBackground(): List<MusicListItem>? {
            musicModel.loadMusicTitles(*musicTypes)

            // Add custom musics
            val context = context
            val items = mutableListOf<MusicListItem>()
            if (musicTypes.contains(UltimateMusicPicker.TYPE_MUSIC)) {
                items.add(HeaderItem(context.getString(R.string.your_sounds)))
                customMusics.forEach {
                    items.add(SoundItem(SoundItem.TYPE_CUSTOM,
                            it.uri, it.title, false, false))
                }
                items.add(AddCustomItem)
            }

            items.add(HeaderItem(context.getString(R.string.device_sounds)))

            if (hasSilent) {
                items.add(SoundItem(SoundItem.TYPE_SILENT, MUSIC_SILENT,
                        context.getString(R.string.silent_ringtone_title),
                        false, false))
            }

            if (hasDefault && defaultUri != MUSIC_SILENT) {
                items.add(SoundItem(SoundItem.TYPE_RINGTONE, defaultUri,
                        if (defaultTitle.isEmpty())
                            context.getString(R.string.default_ringtone_title) else defaultTitle,
                        false, false))
            }

            additional.forEach { (title, uri) ->
                items.add(SoundItem(SoundItem.TYPE_RINGTONE, uri, title,
                        false, false))
            }

            val ringtones = musicModel.getRingtones(*musicTypes)

            // Foreach will crash the app on Kitkat and some Lollipops. Weired.
            val info = ArrayMap<Int, Int>().apply {
                put(UltimateMusicPicker.TYPE_RINGTONE, R.string.music_type_ringtone)
                put(UltimateMusicPicker.TYPE_NOTIFICATION, R.string.music_type_notification)
                put(UltimateMusicPicker.TYPE_ALARM, R.string.music_type_alarm)
            }
            for (index in 0 until info.size) {
                val type = info.keyAt(index)
                val title = info.valueAt(index)
                if (ringtones.keys.contains(type)) {
                    items.add(HeaderItem(context.getString(title)))
                    ringtones[type]?.forEach {
                        items.add(SoundItem(SoundItem.TYPE_RINGTONE,
                                it, musicModel.getMusicTitle(it), false, false))
                    }
                }
            }

            return items
        }
    }
}
