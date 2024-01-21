package xyz.aprildown.ultimateringtonepicker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import xyz.aprildown.ultimateringtonepicker.EXTRA_CATEGORY_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import xyz.aprildown.ultimateringtonepicker.data.Ringtone
import xyz.aprildown.ultimateringtonepicker.databinding.UrpFragmentDeviceRingtoneBinding
import xyz.aprildown.ultimateringtonepicker.gone
import xyz.aprildown.ultimateringtonepicker.launchSaf

internal class DeviceRingtoneFragment :
    Fragment(R.layout.urp_fragment_device_ringtone), EventHandler {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    private val safLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onSafResult(resultCode = it.resultCode, data = it.data)
        }

    init {
        lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
                    lifecycle.removeObserver(this)

                    if (viewModel.settings.deviceRingtonePicker?.alwaysUseSaf == true) {
                        safLauncher.launchSaf(requireContext())
                    } else {
                        viewModel.allDeviceRingtones.observe(
                            this@DeviceRingtoneFragment,
                            object : Observer<List<Ringtone>> {
                                override fun onChanged(value: List<Ringtone>) {
                                    viewModel.allDeviceRingtones.removeObserver(this)
                                    if (value.isEmpty()) {
                                        safLauncher.launchSaf(requireContext())
                                    }
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = UrpFragmentDeviceRingtoneBinding.bind(view)

        val deviceRingtonesTypes =
            viewModel.settings.deviceRingtonePicker?.deviceRingtoneTypes ?: emptyList()

        binding.urpDeviceViewPager.adapter = CategoryAdapter(this, deviceRingtonesTypes)
        binding.urpDeviceViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.stopPlaying()
                }
            }
        )

        if (deviceRingtonesTypes.size == 1) {
            binding.urpDeviceTabLayout.gone()
        }

        TabLayoutMediator(binding.urpDeviceTabLayout, binding.urpDeviceViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.urp_ringtone)
                1 -> getString(R.string.urp_artist)
                2 -> getString(R.string.urp_album)
                3 -> getString(R.string.urp_folder)
                else -> null
            }
        }.attach()
    }

    /**
     * MediaStore returns nothing or we request it, launch SAF.
     */
    private fun onSafResult(resultCode: Int, data: Intent?) {
        val hasSystemPicker = viewModel.settings.systemRingtonePicker != null

        fun onNothingSelected() {
            if (hasSystemPicker) {
                findNavController().popBackStack()
            } else {
                viewModel.onFinalSelection(emptyList())
            }
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            val selected = viewModel.onSafSelect(requireContext().contentResolver, data)
            if (selected != null) {
                if (hasSystemPicker) {
                    viewModel.onDeviceSelection(listOf(selected))
                    findNavController().popBackStack(R.id.urp_dest_system, false)
                } else {
                    viewModel.onFinalSelection(listOf(selected))
                }
            } else {
                onNothingSelected()
            }
        } else {
            onNothingSelected()
        }
    }

    override fun onSelect() {
        RingtoneFragment.myself?.onSelect()
    }

    override fun onBack(): Boolean {
        viewModel.stopPlaying()
        return if (viewModel.settings.systemRingtonePicker == null) {
            false
        } else {
            findNavController().popBackStack()
        }
    }
}

private class CategoryAdapter(
    fragment: Fragment,
    private val deviceRingtoneTypes: List<UltimateRingtonePicker.RingtoneCategoryType>
) : FragmentStateAdapter(
    fragment.childFragmentManager,
    fragment.viewLifecycleOwner.lifecycle
) {

    override fun getItemCount(): Int = deviceRingtoneTypes.size

    override fun createFragment(position: Int): Fragment {
        return when (val type = deviceRingtoneTypes[position]) {
            UltimateRingtonePicker.RingtoneCategoryType.All -> RingtoneFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CATEGORY_TYPE, type)
                }
            }
            UltimateRingtonePicker.RingtoneCategoryType.Artist -> CategoryFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CATEGORY_TYPE, type)
                }
            }
            UltimateRingtonePicker.RingtoneCategoryType.Album -> CategoryFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CATEGORY_TYPE, type)
                }
            }
            UltimateRingtonePicker.RingtoneCategoryType.Folder -> CategoryFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CATEGORY_TYPE, type)
                }
            }
        }
    }
}
