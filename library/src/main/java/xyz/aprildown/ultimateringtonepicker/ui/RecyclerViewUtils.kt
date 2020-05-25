package xyz.aprildown.ultimateringtonepicker.ui

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import xyz.aprildown.ultimateringtonepicker.RINGTONE_URI_SILENT
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel

internal fun FastAdapter<GenericItem>.setUpSelectableRingtoneExtension(
    viewModel: RingtonePickerViewModel,
    onSelectionChanged: ((item: VisibleRingtone, selected: Boolean) -> Unit)? = null
): SelectExtension<GenericItem> = getSelectExtension().also { selectExtension ->

    selectExtension.isSelectable = true
    selectExtension.multiSelect = viewModel.settings.enableMultiSelect
    selectExtension.selectWithItemUpdate = true

    selectExtension.selectionListener = object : ISelectionListener<GenericItem> {
        override fun onSelectionChanged(item: GenericItem, selected: Boolean) {
            if (item !is VisibleRingtone) return

            // Clicked ringtone item
            val itemPosition = getPosition(item)
            if (selected) {
                if (item.ringtone.uri != RINGTONE_URI_SILENT) {
                    item.isPlaying = true
                    notifyItemChanged(itemPosition)
                    viewModel.startPlaying(item.ringtone.uri)
                }
                // Stop other playing items
                if (selectExtension.multiSelect) {
                    forEachIndexed { currentItem, position ->
                        if (currentItem.isSelected &&
                            currentItem is VisibleRingtone &&
                            currentItem != item
                        ) {
                            currentItem.isPlaying = false
                            notifyItemChanged(position)
                        }
                    }
                }

                onSelectionChanged?.invoke(item, selected)
            } else {
                item.isPlaying = false
                notifyItemChanged(itemPosition)
                viewModel.stopPlaying()
            }
        }
    }
}

internal val Fragment.rootRecyclerView: RecyclerView? get() = view as? RecyclerView

@Suppress("UNCHECKED_CAST")
internal val Fragment.rootFastAdapter: GenericFastAdapter?
    get() = rootRecyclerView?.adapter as? GenericFastAdapter

internal fun <Item : GenericItem> FastAdapter<Item>.forEachIndexed(f: (item: Item, position: Int) -> Unit) {
    for (index in 0 until itemCount) {
        f.invoke(getItem(index) ?: continue, index)
    }
}
