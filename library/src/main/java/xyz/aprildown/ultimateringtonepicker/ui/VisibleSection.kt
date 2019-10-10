package xyz.aprildown.ultimateringtonepicker.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import xyz.aprildown.ultimateringtonepicker.R

internal class VisibleSection(
    val title: String
) : AbstractItem<VisibleSection.ViewHolder>() {
    override val layoutRes: Int = R.layout.urp_section
    override val type: Int = R.id.urp_item_section
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override var identifier: Long = title.hashCode().toLong()
    override var isSelectable: Boolean = false

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        holder.run {
            holder.titleView.text = title
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view as TextView
    }
}