package xyz.aprildown.ultimateringtonepicker.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.data.Category

internal class VisibleCategory(
    val category: Category,
    val primaryText: String,
    val secondaryText: String
) : AbstractItem<VisibleCategory.ViewHolder>() {

    override val layoutRes: Int = R.layout.urp_two_lines
    override val type: Int = R.id.urp_item_two_lines
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override var identifier: Long = category.hashCode().toLong()
    override var isSelectable: Boolean = false

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)
        holder.run {
            primaryTextView.text = primaryText
            if (secondaryText.isNotBlank()) {
                secondaryTextView.visibility = View.VISIBLE
                secondaryTextView.text = secondaryText
            } else {
                secondaryTextView.visibility = View.GONE
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val primaryTextView: TextView = view.findViewById(R.id.urpTextCategoryName)
        val secondaryTextView: TextView = view.findViewById(R.id.urpTextCategoryContent)
    }
}