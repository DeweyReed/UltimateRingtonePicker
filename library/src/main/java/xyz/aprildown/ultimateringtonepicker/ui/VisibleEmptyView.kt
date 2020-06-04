package xyz.aprildown.ultimateringtonepicker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.databinding.UrpEmptyBinding

internal class VisibleEmptyView : AbstractBindingItem<UrpEmptyBinding>() {

    override val type: Int = R.layout.urp_empty
    override var isSelectable: Boolean = false

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): UrpEmptyBinding {
        return UrpEmptyBinding.inflate(inflater, parent, false)
    }
}
