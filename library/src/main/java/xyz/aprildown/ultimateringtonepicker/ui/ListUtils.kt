package xyz.aprildown.ultimateringtonepicker.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.ISelectionListener
import com.mikepenz.fastadapter.select.SelectExtension
import com.mikepenz.fastadapter.select.getSelectExtension
import xyz.aprildown.ultimateringtonepicker.KEY_LAYOUT_MANAGER_POSITION
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

internal fun RecyclerView.savePositionTo(outState: Bundle) {
    layoutManager?.onSaveInstanceState()?.let {
        outState.putParcelable(KEY_LAYOUT_MANAGER_POSITION, it)
    }
}

internal fun RecyclerView.retrievePositionFrom(savedInstanceState: Bundle?) {
    savedInstanceState?.getParcelable<Parcelable>(KEY_LAYOUT_MANAGER_POSITION)?.let {
        layoutManager?.onRestoreInstanceState(it)
    }
}

internal fun Fragment.viewAsRecyclerView(): RecyclerView? = view as? RecyclerView
