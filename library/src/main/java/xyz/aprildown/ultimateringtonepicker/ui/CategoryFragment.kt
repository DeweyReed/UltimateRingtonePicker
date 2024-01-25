package xyz.aprildown.ultimateringtonepicker.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import xyz.aprildown.ultimateringtonepicker.EXTRA_CATEGORY_ID
import xyz.aprildown.ultimateringtonepicker.EXTRA_CATEGORY_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import xyz.aprildown.ultimateringtonepicker.createDefaultNavOptions
import xyz.aprildown.ultimateringtonepicker.databinding.UrpRecyclerViewBinding

internal class CategoryFragment : Fragment(R.layout.urp_recycler_view) {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context
        val binding = UrpRecyclerViewBinding.bind(view)

        val categoryType =
            requireArguments().getInt(EXTRA_CATEGORY_TYPE, -1).let { type ->
                UltimateRingtonePicker.RingtoneCategoryType.entries.first { it.ordinal == type }
            }

        val itemAdapter = GenericItemAdapter()
        val fastAdapter = FastAdapter.with(itemAdapter)
        fastAdapter.onClickListener = { _, _, item, _ ->
            when (item) {
                is VisibleCategory -> {
                    findNavController().navigate(
                        R.id.urp_dest_ringtone_list,
                        Bundle().apply {
                            putInt(EXTRA_CATEGORY_TYPE, categoryType.ordinal)
                            putLong(EXTRA_CATEGORY_ID, item.category.id)
                        },
                        createDefaultNavOptions()
                    )
                    true
                }
                else -> false
            }
        }

        binding.urpRecyclerView.run {
            adapter = fastAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel.getCategoryLiveData(categoryType)?.observe(viewLifecycleOwner) { categories ->
            binding.urpProgress.hide()
            if (categories.isNotEmpty()) {
                itemAdapter.setNewList(categories.map { category ->
                    VisibleCategory(
                        category = category,
                        primaryText = category.name,
                        secondaryText = category.numberOfSongs.toString()
                    )
                })
            } else {
                itemAdapter.setNewList(listOf(VisibleEmptyView()))
            }
        }
    }
}
