package xyz.aprildown.ultimateringtonepicker.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.items.AbstractItem
import xyz.aprildown.ultimateringtonepicker.R

internal class VisibleEmptyView : AbstractItem<VisibleEmptyView.ViewHolder>() {

    override val layoutRes: Int = R.layout.urp_empty
    override val type: Int = R.layout.urp_empty
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
