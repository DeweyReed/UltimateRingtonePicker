package xyz.aprildown.ultimateringtonepicker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import xyz.aprildown.ultimateringtonepicker.KEY_CATEGORY_TYPE
import xyz.aprildown.ultimateringtonepicker.KEY_EXTRA_ID
import xyz.aprildown.ultimateringtonepicker.KEY_RINGTONE_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.categoryTypeToRingtoneType

internal class CategoryFragment : Fragment() {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.urp_recycler_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context
        val recyclerView = view as RecyclerView

        val categoryType = requireArguments().getInt(KEY_CATEGORY_TYPE)

        val itemAdapter = GenericItemAdapter()
        val fastAdapter = FastAdapter.with(itemAdapter)
        fastAdapter.onClickListener = { _, _, item, _ ->
            when (item) {
                is VisibleCategory -> {
                    findNavController().navigate(
                        R.id.urp_action_device_to_list,
                        Bundle().apply {
                            putInt(KEY_RINGTONE_TYPE, categoryType.categoryTypeToRingtoneType())
                            putLong(KEY_EXTRA_ID, item.category.categoryId)
                        }
                    )
                    true
                }
                else -> false
            }
        }

        recyclerView.run {
            layoutManager = LinearLayoutManager(context)
            adapter = fastAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        viewModel.getCategoryLiveData(categoryType)
            .observe(viewLifecycleOwner, Observer { categories ->
                itemAdapter.add(categories.map { category ->
                    VisibleCategory(
                        category = category,
                        primaryText = category.name,
                        secondaryText = category.numberOfSongs.toString()
                    )
                })
            })
    }
}