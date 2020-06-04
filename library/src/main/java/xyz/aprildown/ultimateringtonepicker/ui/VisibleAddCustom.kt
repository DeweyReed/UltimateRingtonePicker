package xyz.aprildown.ultimateringtonepicker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.databinding.UrpRingtoneBinding

internal class VisibleAddCustom : AbstractBindingItem<UrpRingtoneBinding>() {

    override val type: Int = R.id.urp_item_add_custom
    override var identifier: Long = 1
    override var isSelectable: Boolean = false

    override fun bindView(binding: UrpRingtoneBinding, payloads: List<Any>) {
        super.bindView(binding, payloads)
        binding.run {
            urpImageRingtone.setImageResource(R.drawable.urp_add_custom)
            urpTextRingtoneName.setText(R.string.urp_add_new_sound)
            urpImageSelected.visibility = View.GONE
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): UrpRingtoneBinding {
        return UrpRingtoneBinding.inflate(inflater, parent, false)
    }
}
