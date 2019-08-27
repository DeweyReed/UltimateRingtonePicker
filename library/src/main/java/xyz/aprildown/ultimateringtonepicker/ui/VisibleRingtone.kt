package xyz.aprildown.ultimateringtonepicker.ui

import android.graphics.Color
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.data.Ringtone
import xyz.aprildown.ultimateringtonepicker.startDrawableAnimation

internal class VisibleRingtone(
    val ringtone: Ringtone,
    val ringtoneType: Int
) : AbstractItem<VisibleRingtone.ViewHolder>() {

    override val layoutRes: Int = R.layout.urp_ringtone
    override val type: Int = R.id.urp_item_ringtone
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override var identifier: Long = ringtone.hashCode().toLong()
    override var isSelectable: Boolean = true
    var isPlaying: Boolean = false

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        holder.run {
            itemView.setBackgroundColor(
                if (isSelected) Color.parseColor("#14000000")
                else Color.TRANSPARENT
            )

            ringtoneImage.setImageResource(
                when {
                    ringtoneType == RINGTONE_TYPE_CUSTOM -> R.drawable.urp_custom_music
                    ringtoneType == RINGTONE_TYPE_SILENT -> R.drawable.urp_ringtone_silent
                    isPlaying -> R.drawable.urp_ringtone_active
                    else -> R.drawable.urp_ringtone_normal
                }
            )
            ringtoneImage.startDrawableAnimation()

            nameView.text = ringtone.title

            selectedImage.visibility = if (isSelected) VISIBLE else GONE
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ringtoneImage: ImageView = view.findViewById(R.id.urpImageRingtone)
        val nameView: TextView = view.findViewById(R.id.urpTextRingtoneName)
        val selectedImage: ImageView = view.findViewById(R.id.urpImageSelected)
    }

    companion object {
        const val RINGTONE_TYPE_CUSTOM = 0
        const val RINGTONE_TYPE_SILENT = 1
        const val RINGTONE_TYPE_SYSTEM = 2
    }
}