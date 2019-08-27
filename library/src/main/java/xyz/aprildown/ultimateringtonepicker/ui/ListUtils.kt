package xyz.aprildown.ultimateringtonepicker.ui

import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import xyz.aprildown.ultimateringtonepicker.RINGTONE_URI_SILENT
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel

internal fun FastAdapter<IItem<*>>.setUpSelectableRingtoneExtension(
    viewModel: RingtonePickerViewModel
): SelectExtension<IItem<*>> = getSelectExtension().apply {
    isSelectable = true
    multiSelect = viewModel.settings.enableMultiSelect
    selectWithItemUpdate = true

    selectionListener = object : ISelectionListener<IItem<*>> {
        override fun onSelectionChanged(item: IItem<*>?, selected: Boolean) {
            if (item !is VisibleRingtone) return

            // Clicked ringtone item
            if (selected) {
                // item.isSelected = true
                if (item.ringtone.uri != RINGTONE_URI_SILENT) {
                    item.isPlaying = true
                    viewModel.startPlaying(item.ringtone.uri)
                }
                // Stop other playing items
                if (multiSelect) {
                    selectedItems.forEach { selectedItem ->
                        if (selectedItem is VisibleRingtone && selectedItem != item) {
                            selectedItem.isPlaying = false
                            notifyItemChanged(getPosition(selectedItem))
                        }
                    }
                }
            } else {
                // item.isSelected = false
                item.isPlaying = false
                viewModel.stopPlaying()
            }
        }
    }
}
