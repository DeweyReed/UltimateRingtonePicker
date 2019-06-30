package xyz.aprildown.ultimateringtonepicker.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.loader.content.AsyncTaskLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.data.MusicModel

internal class PickerCustomFragment : PickerBaseFragment() {

    override fun init() = Unit
    override fun shouldShowContextMenu(): Boolean = false

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<MusicListItem>> {
        return CustomMusicLoader(requireContext(), viewModel.musicModel)
    }

    override fun onLoadFinished(
        loader: Loader<List<MusicListItem>>,
        data: List<MusicListItem>?
    ) {
        if (data != null) {
            if (data.isNotEmpty()) {
                musicAdapter.populateData(data)
            } else {
                Toast.makeText(requireContext(), R.string.no_music_found, Toast.LENGTH_LONG).show()
                parent.customPicked(null)
            }
        }
    }

    override fun onItemClicked(viewHolder: RecyclerView.ViewHolder, id: Int) {
        when (id) {
            MusicAdapter.CLICK_NORMAL -> onMusicItemClicked(viewHolder)
        }
    }

    private class CustomMusicLoader(
        context: Context,
        private val musicModel: MusicModel
    ) : AsyncTaskLoader<List<MusicListItem>>(context) {

        override fun onStartLoading() {
            super.onStartLoading()
            forceLoad()
        }

        @SuppressLint("MissingPermission")
        override fun loadInBackground(): List<MusicListItem>? {
            val available = musicModel.getAvailableCustomMusics()
            return List(available.size) {
                val item = available[it]
                SoundItem(
                    SoundItem.TYPE_CUSTOM, item.uri, item.title,
                    false, false
                )
            }
        }
    }
}
