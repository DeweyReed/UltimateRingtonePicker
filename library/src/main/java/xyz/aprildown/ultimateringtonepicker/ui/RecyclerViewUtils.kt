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
            if (selected) {
                if (item.ringtone.uri != RINGTONE_URI_SILENT) {
                    item.isPlaying = true
                    viewModel.startPlaying(item.ringtone.uri)
                }
                // Stop other playing items
                if (selectExtension.multiSelect) {
                    selectExtension.selectedItems.forEach { selectedItem ->
                        if (selectedItem is VisibleRingtone && selectedItem != item) {
                            selectedItem.isPlaying = false
                            notifyItemChanged(getPosition(selectedItem))
                        }
                    }
                }

                onSelectionChanged?.invoke(item, selected)
            } else {
                item.isPlaying = false
                viewModel.stopPlaying()
            }
        }
    }
}

internal val Fragment.rootRecyclerView: RecyclerView? get() = view as? RecyclerView

@Suppress("UNCHECKED_CAST")
internal val Fragment.rootFastAdapter: GenericFastAdapter?
    get() = rootRecyclerView?.adapter as? GenericFastAdapter
