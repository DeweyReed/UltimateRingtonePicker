package xyz.aprildown.ultimateringtonepicker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.data.Ringtone
import xyz.aprildown.ultimateringtonepicker.databinding.UrpRingtoneBinding
import xyz.aprildown.ultimateringtonepicker.startDrawableAnimation

internal class VisibleRingtone(
    val ringtone: Ringtone,
    val ringtoneType: Int
) : AbstractBindingItem<UrpRingtoneBinding>() {

    var isPlaying: Boolean = false

    override val type: Int = R.id.urp_item_ringtone
    override var identifier: Long = ringtone.hashCode().toLong()
    override var isSelectable: Boolean = true

    override fun bindView(binding: UrpRingtoneBinding, payloads: List<Any>) {
        super.bindView(binding, payloads)
        binding.run {
            urpImageRingtone.setImageResource(
                when {
                    !ringtone.isValid -> R.drawable.urp_broken_ringtone
                    ringtoneType == RINGTONE_TYPE_CUSTOM -> R.drawable.urp_custom_music
                    ringtoneType == RINGTONE_TYPE_SILENT -> R.drawable.urp_ringtone_silent
                    isPlaying -> R.drawable.urp_ringtone_active
                    else -> R.drawable.urp_ringtone_normal
                }
            )
            // Only works on R.drawable.urp_ringtone_active
            urpImageRingtone.startDrawableAnimation()

            urpTextRingtoneName.text = ringtone.title

            urpImageSelected.isVisible = isSelected
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): UrpRingtoneBinding {
        return UrpRingtoneBinding.inflate(inflater, parent, false)
    }

    companion object {
        const val RINGTONE_TYPE_CUSTOM = 0
        const val RINGTONE_TYPE_SILENT = 1
        const val RINGTONE_TYPE_SYSTEM = 2
    }
}
