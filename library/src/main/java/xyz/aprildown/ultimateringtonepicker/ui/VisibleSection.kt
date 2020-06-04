package xyz.aprildown.ultimateringtonepicker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.databinding.UrpSectionBinding

internal class VisibleSection(val title: String) : AbstractBindingItem<UrpSectionBinding>() {

    override val type: Int = R.id.urp_item_section
    override var identifier: Long = title.hashCode().toLong()
    override var isSelectable: Boolean = false

    override fun bindView(binding: UrpSectionBinding, payloads: List<Any>) {
        super.bindView(binding, payloads)
        binding.urpTextSection.text = title
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): UrpSectionBinding {
        return UrpSectionBinding.inflate(inflater, parent, false)
    }
}
