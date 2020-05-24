package xyz.aprildown.ultimateringtonepicker.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import com.mikepenz.fastadapter.select.getSelectExtension
import xyz.aprildown.ultimateringtonepicker.EXTRA_CATEGORY_ID
import xyz.aprildown.ultimateringtonepicker.EXTRA_CATEGORY_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker

internal class RingtoneFragment : Fragment(R.layout.urp_recycler_view), EventHandler {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view as RecyclerView

        val itemAdapter = GenericItemAdapter()
        val fastAdapter = FastAdapter.with(itemAdapter)
        fastAdapter.setUpSelectableRingtoneExtension(viewModel)

        recyclerView.adapter = fastAdapter

        val arguments = requireArguments()
        viewModel.getRingtoneLiveData(
            categoryType = arguments.getSerializable(EXTRA_CATEGORY_TYPE) as UltimateRingtonePicker.RingtoneCategoryType,
            categoryId = arguments.getLong(EXTRA_CATEGORY_ID)
        ).observe(viewLifecycleOwner, Observer { ringtones ->
            if (ringtones.isNotEmpty()) {
                itemAdapter.setNewList(ringtones.map { ringtone ->
                    VisibleRingtone(
                        ringtone = ringtone,
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_CUSTOM
                    )
                })
            } else {
                itemAdapter.setNewList(listOf(VisibleEmptyView()))
            }
        })
    }

    override fun onResume() {
        super.onResume()
        myself = this
    }

    override fun onSelect() {
        viewModel.stopPlaying()
        val ringtones =
            rootFastAdapter?.getSelectExtension()?.selectedItems?.mapNotNull { (it as? VisibleRingtone)?.ringtone }
        if (ringtones?.isNotEmpty() == true) {
            if (viewModel.settings.systemRingtonePicker == null) {
                viewModel.onFinalSelection(ringtones)
            } else {
                viewModel.onDeviceSelection(ringtones)
                findNavController().popBackStack(R.id.urp_dest_system, false)
            }
        }
    }

    override fun onBack(): Boolean {
        viewModel.stopPlaying()
        // If we pop back to DeviceRingtoneFragment, the scroll position is lost.
        return findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()
        myself = null
    }

    companion object {
        /**
         * I can't find a way to get current fragment in ViewPager so I use this way.
         */
        internal var myself: RingtoneFragment? = null
    }
}
