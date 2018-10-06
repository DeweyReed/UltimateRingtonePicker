package xyz.aprildown.ultimatemusicpicker.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.loader.app.LoaderManager
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import xyz.aprildown.ultimatemusicpicker.MUSIC_SILENT
import xyz.aprildown.ultimatemusicpicker.R
import xyz.aprildown.ultimatemusicpicker.UltimateMusicPicker
import xyz.aprildown.ultimatemusicpicker.data.CustomMusic
import xyz.aprildown.ultimatemusicpicker.data.MusicModel

internal class PickerNormalFragment : PickerBaseFragment(), View.OnCreateContextMenuListener {

    private var indexOfMusicToRemove = RecyclerView.NO_POSITION

    override fun init() {
        registerForContextMenu(recyclerView)
        recyclerView.setOnCreateContextMenuListener(this)
    }

    override fun shouldShowContextMenu(): Boolean = true

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<MusicListItem>> {
        return viewModel.setting.run {
            MusicLoader(
                localContext, viewModel.musicModel, musicTypes,
                hasDefault, defaultTitle, defaultUri, hasSilent, additional
            )
        }
    }

    override fun onLoadFinished(
        loader: Loader<List<MusicListItem>>,
        data: List<MusicListItem>?
    ) {
        if (data != null) {
            musicAdapter.populateData(data)

            val toSelect: SoundItem? = getSoundItem(viewModel.selectedUri)

            parent.musicPlayer.stop()
            if (toSelect != null) {
                toSelect.isSelected = true
                viewModel.selectedUri = toSelect.uri
                toSelect.notifyItemChanged(true)
            } else {
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

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val listener = MenuItem.OnMenuItemClickListener { item ->
            onContextItemSelected(item)
            true
        }

        for (i in 0 until (menu?.size() ?: 0)) {
            menu?.getItem(i)?.setOnMenuItemClickListener(listener)
        }
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
                    items.add(
                        SoundItem(
                            SoundItem.TYPE_CUSTOM, it.uri,
                            it.title, false, false
                        )
                    )
                }
                items.add(AddCustomItem)
            }

            items.add(HeaderItem(context.getString(R.string.device_sounds)))

            if (hasSilent) {
                items.add(
                    SoundItem(
                        SoundItem.TYPE_SILENT, MUSIC_SILENT,
                        context.getString(R.string.silent_ringtone_title),
                        false, false
                    )
                )
            }

            if (hasDefault && defaultUri != MUSIC_SILENT) {
                items.add(
                    SoundItem(
                        SoundItem.TYPE_RINGTONE, defaultUri,
                        if (defaultTitle.isEmpty())
                            context.getString(R.string.default_ringtone_title) else defaultTitle,
                        false, false
                    )
                )
            }

            additional.forEach { (title, uri) ->
                items.add(
                    SoundItem(
                        SoundItem.TYPE_RINGTONE, uri,
                        title, false, false
                    )
                )
            }

            for (index in 0 until musicTypes.size) {
                val type = musicTypes[index]
                val title = when (type) {
                    UltimateMusicPicker.TYPE_ALARM -> R.string.music_type_alarm
                    UltimateMusicPicker.TYPE_NOTIFICATION -> R.string.music_type_notification
                    UltimateMusicPicker.TYPE_RINGTONE -> R.string.music_type_ringtone
                    else -> null
                } ?: continue
                items.add(HeaderItem(context.getString(title)))
                musicModel.getRingtones(type).forEach {
                    items.add(
                        SoundItem(
                            SoundItem.TYPE_RINGTONE, it,
                            musicModel.getMusicTitle(it), false, false
                        )
                    )
                }
            }

            return items
        }
    }
}
