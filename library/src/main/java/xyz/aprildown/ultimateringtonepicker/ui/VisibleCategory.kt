package xyz.aprildown.ultimateringtonepicker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.data.Category
import xyz.aprildown.ultimateringtonepicker.databinding.UrpTwoLinesBinding

internal class VisibleCategory(
    val category: Category,
    val primaryText: String,
    val secondaryText: String
) : AbstractBindingItem<UrpTwoLinesBinding>() {

    override val type: Int = R.id.urp_item_two_lines
    override var isSelectable: Boolean = false

    override fun bindView(binding: UrpTwoLinesBinding, payloads: List<Any>) {
        super.bindView(binding, payloads)
        binding.run {
            urpTextCategoryName.text = primaryText
            if (secondaryText.isNotBlank()) {
                urpTextCategoryContent.visibility = View.VISIBLE
                urpTextCategoryContent.text = secondaryText
            } else {
                urpTextCategoryContent.visibility = View.GONE
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): UrpTwoLinesBinding {
        return UrpTwoLinesBinding.inflate(inflater, parent, false)
    }
}
