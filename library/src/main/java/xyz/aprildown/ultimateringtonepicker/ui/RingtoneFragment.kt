package xyz.aprildown.ultimateringtonepicker.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import com.mikepenz.fastadapter.select.getSelectExtension
import xyz.aprildown.ultimateringtonepicker.EXTRA_CATEGORY_ID
import xyz.aprildown.ultimateringtonepicker.EXTRA_CATEGORY_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import xyz.aprildown.ultimateringtonepicker.databinding.UrpRecyclerViewBinding

internal class RingtoneFragment : Fragment(R.layout.urp_recycler_view), EventHandler {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = UrpRecyclerViewBinding.bind(view)

        val itemAdapter = GenericItemAdapter()
        val fastAdapter = FastAdapter.with(itemAdapter)
        fastAdapter.setUpSelectableRingtoneExtension(viewModel)

        binding.urpRecyclerView.adapter = fastAdapter

        val arguments = requireArguments()
        viewModel.getRingtoneLiveData(
            categoryType = arguments.getInt(EXTRA_CATEGORY_TYPE, -1).let { type ->
                UltimateRingtonePicker.RingtoneCategoryType.entries.first { it.ordinal == type }
            },
            categoryId = arguments.getLong(EXTRA_CATEGORY_ID)
        ).observe(viewLifecycleOwner) { ringtones ->
            binding.urpProgress.hide()
            if (ringtones.isNotEmpty()) {
                itemAdapter.setNewList(ringtones.map { ringtone ->
                    VisibleRingtone(
                        ringtone = ringtone,
                        ringtoneType = VisibleRingtone.RINGTONE_TYPE_CUSTOM
                    )
                })
                fastAdapter.getSelectExtension()
                    .withSavedInstanceState(savedInstanceState, KEY_SELECTION)
            } else {
                itemAdapter.setNewList(listOf(VisibleEmptyView()))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        myself = this
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ringtoneFastAdapter?.getSelectExtension()?.saveInstanceState(outState, KEY_SELECTION)
    }

    override fun onSelect() {
        val ringtones =
            ringtoneFastAdapter?.getSelectExtension()?.selectedItems?.mapNotNull { (it as? VisibleRingtone)?.ringtone }
        if (ringtones?.isNotEmpty() == true) {
            if (viewModel.settings.systemRingtonePicker == null) {
                viewModel.stopPlaying()
                viewModel.onFinalSelection(ringtones)
            } else {
                viewModel.onDeviceSelection(ringtones)
                findNavController().popBackStack(R.id.urp_dest_system, false)
            }
        } else {
            viewModel.stopPlaying()
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
        private const val KEY_SELECTION = "selection"

        /**
         * I can't find a way to get current fragment in ViewPager so I use this way.
         */
        internal var myself: RingtoneFragment? = null
    }
}
