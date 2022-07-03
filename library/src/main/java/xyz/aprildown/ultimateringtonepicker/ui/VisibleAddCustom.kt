package xyz.aprildown.ultimateringtonepicker.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.SharedPrefUtils
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
            urpTextRingtoneDuration.visibility = View.GONE
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): UrpRingtoneBinding {
        SharedPrefUtils.init(parent?.context!!)
        val binding = UrpRingtoneBinding.inflate(inflater, parent, false)
        if(!SharedPrefUtils.readBoolean(RingtonePickerActivity.IS_DARK_MODE)){
            ImageViewCompat.setImageTintList(binding.urpImageRingtone, ColorStateList.valueOf(
                    ContextCompat.getColor(parent.context!!, R.color.urp_image_tint)
            ))
        }else{
            binding.urpImageRingtone.clearColorFilter()
        }
        return binding
    }
}
