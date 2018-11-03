package xyz.aprildown.ultimatemusicpicker.ui

import android.graphics.Color
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.aprildown.ultimatemusicpicker.R
import xyz.aprildown.ultimatemusicpicker.startDrawableAnimation

internal class MusicAdapter(
    private val listener: OnItemCLickedListener,
    private val showContextMenu: Boolean = true
) : RecyclerView.Adapter<MusicAdapter.BaseViewHolder>() {

    companion object {
        const val CLICK_NORMAL = 0
        const val CLICK_LONG_PRESS = -1
        const val CLICK_ADD_NEW = Int.MAX_VALUE

        private const val ITEM_VIEW_TYPE_HEADER = 0
        private const val ITEM_VIEW_TYPE_ADD_CUSTOM = 1
        private const val ITEM_VIEW_TYPE_SOUND = 2
    }

    interface OnItemCLickedListener {
        fun onItemClicked(viewHolder: RecyclerView.ViewHolder, id: Int)
    }

    private val values = mutableListOf<MusicListItem>()

    override fun getItemCount(): Int = values.size

    override fun getItemViewType(position: Int): Int {
        return when (values[position]) {
            is HeaderItem -> ITEM_VIEW_TYPE_HEADER
            is AddCustomItem -> ITEM_VIEW_TYPE_ADD_CUSTOM
            is SoundItem -> ITEM_VIEW_TYPE_SOUND
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        fun inflateView(layoutRes: Int): View = LayoutInflater.from(parent.context)
            .inflate(layoutRes, parent, false)
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder(
                inflateView(R.layout.music_section_header),
                listener
            )
            ITEM_VIEW_TYPE_ADD_CUSTOM ->
                AddCustomViewHolder(inflateView(R.layout.music_item_sound), listener)
            ITEM_VIEW_TYPE_SOUND ->
                SoundViewHolder(inflateView(R.layout.music_item_sound), listener, showContextMenu)
            else -> throw IllegalArgumentException("Wrong viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(values[position])
    }

    fun getData(): List<MusicListItem> {
        return values
    }

    fun populateData(data: List<MusicListItem>) {
        values.clear()
        values.addAll(data)
        notifyDataSetChanged()
    }

    fun removeViewHolder(holder: SoundItem) {
        val index = values.indexOf(holder)
        if (index in 0 until values.size) {
            values.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    abstract class BaseViewHolder(
        view: View,
        protected val listener: OnItemCLickedListener
    ) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: MusicListItem)
    }

    class HeaderViewHolder(
        view: View,
        listener: OnItemCLickedListener
    ) : BaseViewHolder(view, listener) {
        private val titleView = view.findViewById<TextView>(R.id.textItemHeader)

        override fun bind(item: MusicListItem) {
            if (item is HeaderItem) {
                titleView.text = item.title
            }
        }
    }

    class AddCustomViewHolder(
        view: View,
        listener: OnItemCLickedListener
    ) : BaseViewHolder(view, listener), View.OnClickListener {
        override fun bind(item: MusicListItem) {
            itemView.setOnClickListener(this)

            val selectedView = itemView.findViewById<ImageView>(R.id.imageSelected)
            selectedView.visibility = GONE

            val nameView = itemView.findViewById<TextView>(R.id.textSoundName)
            nameView.text = itemView.context.getString(R.string.add_new_sound)

            val imageView = itemView.findViewById<ImageView>(R.id.imageSound)
            imageView.setImageResource(R.drawable.ic_add_custom)
        }

        override fun onClick(v: View?) {
            listener.onItemClicked(this, CLICK_ADD_NEW)
        }
    }

    class SoundViewHolder(
        view: View,
        listener: OnItemCLickedListener,
        private val showContextMenu: Boolean
    ) : BaseViewHolder(view, listener),
        View.OnClickListener, View.OnCreateContextMenuListener {
        private val imageSound = view.findViewById<ImageView>(R.id.imageSound)
        private val textSoundName = view.findViewById<TextView>(R.id.textSoundName)
        private val imageSelected = view.findViewById<ImageView>(R.id.imageSelected)

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(item: MusicListItem) {
            if (item is SoundItem) {
                textSoundName.text = item.title
                imageSound.clearColorFilter()

                val itemType = item.type

                imageSound.setImageResource(
                    when {
                        itemType == SoundItem.TYPE_CUSTOM -> R.drawable.ic_custom_music
                        itemType == SoundItem.TYPE_SILENT -> R.drawable.ic_ringtone_silent
                        item.isPlaying -> R.drawable.ic_ringtone_active
                        else -> R.drawable.ic_ringtone_normal
                    }
                )
                imageSound.startDrawableAnimation()

                imageSelected.visibility = if (item.isSelected) VISIBLE else GONE

                itemView.setBackgroundColor(
                    if (item.isSelected) Color.parseColor("#14000000")
                    else Color.TRANSPARENT
                )

                if (showContextMenu && itemType == SoundItem.TYPE_CUSTOM) {
                    itemView.setOnCreateContextMenuListener(this)
                }
            }
        }

        override fun onClick(v: View?) {
            listener.onItemClicked(this, CLICK_NORMAL)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?, menuInfo:
            ContextMenu.ContextMenuInfo?
        ) {
            listener.onItemClicked(this, CLICK_LONG_PRESS)
            menu?.add(Menu.NONE, 0, Menu.NONE, R.string.remove_sound)
        }
    }
}
