package xyz.aprildown.ultimateringtonepicker.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.SharedPrefUtils
import xyz.aprildown.ultimateringtonepicker.data.Ringtone
import xyz.aprildown.ultimateringtonepicker.databinding.UrpRingtoneBinding
import xyz.aprildown.ultimateringtonepicker.startDrawableAnimation
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

internal class VisibleRingtone(
    val ringtone: Ringtone,
    val ringtoneType: Int
) : AbstractBindingItem<UrpRingtoneBinding>() {

    var isPlaying: Boolean = false
    var contex: Context? = null


    override val type: Int = R.id.urp_item_ringtone
    override var identifier: Long = ringtone.hashCode().toLong()
    override var isSelectable: Boolean = true

    override fun bindView(binding: UrpRingtoneBinding, payloads: List<Any>) {
        super.bindView(binding, payloads)
        binding.run {
                    val text =  ringtone.title
                    urpTextRingtoneName.text = text
            urpTextRingtoneDuration.text = ringtone.duration
            urpImageSelected.isVisible = isSelected
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
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): UrpRingtoneBinding {
        this.contex = parent?.context
        SharedPrefUtils.init(contex!!)
        val bindingView = UrpRingtoneBinding.inflate(inflater, parent, false)
        /**
         * Handle dark mode
         */
        if(!SharedPrefUtils.readBoolean(RingtonePickerActivity.IS_DARK_MODE)){
           ImageViewCompat.setImageTintList(bindingView.urpImageRingtone, ColorStateList.valueOf(
                   ContextCompat.getColor(contex!!, R.color.urp_image_tint)
           ))
            ImageViewCompat.setImageTintList(bindingView.urpImageSelected, ColorStateList.valueOf(
                    ContextCompat.getColor(contex!!, R.color.urp_image_tint)
            ))
        }else{
            bindingView.urpImageRingtone.clearColorFilter()
            bindingView.urpImageSelected.clearColorFilter()
        }
        return bindingView
    }

    companion object {
        const val RINGTONE_TYPE_CUSTOM = 0
        const val RINGTONE_TYPE_SILENT = 1
        const val RINGTONE_TYPE_SYSTEM = 2
    }
}
