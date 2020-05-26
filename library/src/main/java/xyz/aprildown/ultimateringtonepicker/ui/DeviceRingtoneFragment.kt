package xyz.aprildown.ultimateringtonepicker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import xyz.aprildown.ultimateringtonepicker.EXTRA_CATEGORY_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import xyz.aprildown.ultimateringtonepicker.data.Ringtone
import xyz.aprildown.ultimateringtonepicker.gone
import xyz.aprildown.ultimateringtonepicker.launchSaf

internal class DeviceRingtoneFragment :
    Fragment(R.layout.urp_fragment_device_ringtone), EventHandler {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    init {
        lifecycleScope.launchWhenResumed {
            // viewModel may not be available in onCreate.
            if (viewModel.settings.deviceRingtonePicker?.alwaysUseSaf == true) {
                launchSaf()
            } else {
                viewModel.allDeviceRingtones.observe(
                    this@DeviceRingtoneFragment,
                    object : Observer<List<Ringtone>> {
                        override fun onChanged(t: List<Ringtone>?) {
                            if (t == null) return
                            viewModel.allDeviceRingtones.removeObserver(this)
                            if (t.isEmpty()) {
                                launchSaf()
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout = view.findViewById<TabLayout>(R.id.urpDeviceTabLayout)
        val viewPager = view.findViewById<ViewPager>(R.id.urpDeviceViewPager)

        val deviceRingtonesTypes =
            viewModel.settings.deviceRingtonePicker?.deviceRingtoneTypes ?: emptyList()

        viewPager.adapter = CategoryAdapter(this, deviceRingtonesTypes)
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                viewModel.stopPlaying()
            }
        })

        if (deviceRingtonesTypes.size == 1) {
            tabLayout.gone()
        }
        tabLayout.setupWithViewPager(viewPager)
    }

    /**
     * MediaStore returns nothing or we request it, launch SAF.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
) : FragmentStatePagerAdapter(
    fragment.childFragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    override fun getCount(): Int = deviceRingtoneTypes.size

    override fun getItem(position: Int): Fragment {
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
            else -> throw IllegalArgumentException("Too bing position: $position")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> "ALL"
        1 -> "ARTIST"
        2 -> "ALBUM"
        3 -> "FOLDER"
        else -> null
    }
}
