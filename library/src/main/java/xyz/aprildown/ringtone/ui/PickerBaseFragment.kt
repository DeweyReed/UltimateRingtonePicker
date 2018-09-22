package xyz.aprildown.ringtone.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xyz.aprildown.ringtone.R

/**
 * Created on 2018/4/20.
 * Handles RecyclerView
 */
internal abstract class PickerBaseFragment : Fragment(),
    LoaderManager.LoaderCallbacks<List<MusicListItem>>,
    MusicAdapter.OnItemCLickedListener {

    protected lateinit var viewModel: PickerViewModel

    protected lateinit var parent: MusicPickerFragment
    protected lateinit var localContext: Context
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var musicAdapter: MusicAdapter

    abstract fun init()
    abstract fun shouldShowContextMenu(): Boolean

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parent = (parentFragment as? MusicPickerFragment) ?:
                throw IllegalStateException("PickerBaseFragment should show in the MusicPickerActivity!")

        viewModel = ViewModelProviders.of(parent).get(PickerViewModel::class.java)

        val view = inflater.inflate(R.layout.layout_music_picker, container, false)
        localContext = view.context

        recyclerView = view.findViewById(R.id.list)
        musicAdapter = MusicAdapter(this, shouldShowContextMenu())
        recyclerView.adapter = musicAdapter

        init()

        LoaderManager.getInstance(this).initLoader(0, null, this)
        return view
    }

    override fun onLoaderReset(p0: Loader<List<MusicListItem>>) = Unit

    internal fun getSelectedSoundItem(): SoundItem? = getSoundItem(viewModel.selectedUri)

    protected fun MusicListItem.notifyItemChanged(scrollTo: Boolean = false) {
        val data = musicAdapter.getData()
        val index = musicAdapter.getData().indexOf(this)
        if (index in 0 until data.size) {
            musicAdapter.notifyItemChanged(index)
            if (scrollTo) {
                recyclerView.layoutManager
                    ?.smoothScrollToPosition(recyclerView, null, index)
            }
        }
    }

    protected fun onMusicItemClicked(viewHolder: RecyclerView.ViewHolder) {
        val old = getSelectedSoundItem()
        val new = musicAdapter.getData()
            .getOrNull(viewHolder.adapterPosition) as? SoundItem ?: return
        if (old == new) {
            if (new.isPlaying) {
                parent.stopPlayingMusic(new, false)
            } else {
                parent.startPlayingMusic(new)
            }
            new.notifyItemChanged()
        } else {
            parent.stopPlayingMusic(old, true)
            old?.notifyItemChanged()
            parent.startPlayingMusic(new)
            new.notifyItemChanged()
        }
    }

    protected fun getSoundItem(uri: Uri?): SoundItem? {
        return if (uri == null) null
        else musicAdapter.getData().find { it is SoundItem && it.uri == uri } as? SoundItem
    }
}