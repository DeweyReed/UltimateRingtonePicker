package xyz.aprildown.ultimateringtonepicker.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import xyz.aprildown.ultimateringtonepicker.R

internal class VisibleAddCustom : AbstractItem<VisibleAddCustom.ViewHolder>() {

    override val layoutRes: Int = R.layout.urp_ringtone
    override val type: Int = R.id.urp_item_add_custom
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override var identifier: Long = 1
    override var isSelectable: Boolean = false

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        holder.run {
            ringtoneImage.setImageResource(R.drawable.urp_add_custom)
            nameView.setText(R.string.urp_add_new_sound)
            selectedImage.visibility = View.GONE
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ringtoneImage: ImageView = view.findViewById(R.id.urpImageRingtone)
        val nameView: TextView = view.findViewById(R.id.urpTextRingtoneName)
        val selectedImage: ImageView = view.findViewById(R.id.urpImageSelected)
    }
}