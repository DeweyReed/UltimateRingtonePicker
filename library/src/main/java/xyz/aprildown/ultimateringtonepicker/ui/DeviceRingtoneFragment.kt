package xyz.aprildown.ultimateringtonepicker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_ALBUM
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_ARTIST
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_FOLDER
import xyz.aprildown.ultimateringtonepicker.KEY_CATEGORY_TYPE
import xyz.aprildown.ultimateringtonepicker.KEY_RINGTONE_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RINGTONE_TYPE_ALL
import xyz.aprildown.ultimateringtonepicker.RingtonePickerViewModel

internal class DeviceRingtoneFragment : Fragment(), Navigator.Selector {

    private val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initDeviceRingtones()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.urp_fragment_device_ringtone, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout = view.findViewById<TabLayout>(R.id.urpDeviceTabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.urpDeviceViewPager)

        viewPager.adapter = CategoryAdapter(this)

        TabLayoutMediator(
            tabLayout,
            viewPager,
            TabLayoutMediator.OnConfigureTabCallback { tab, position ->
                tab.text = when (position) {
                    0 -> "ALL"
                    1 -> "ARTIST"
                    2 -> "ALBUM"
                    3 -> "FOLDER"
                    else -> null
                }
            }
        ).attach()
    }

    override fun onSelect() {
        RingtoneFragment.myself?.onSelect()
    }

    override fun onBack(): Boolean {
        return if (viewModel.settings.onlyShowDevice) {
            false
        } else {
            findNavController().popBackStack()
        }
    }
}

private class CategoryAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> RingtoneFragment().apply {
            this@CategoryAdapter.itemCount
            arguments = Bundle().apply {
                putInt(KEY_RINGTONE_TYPE, RINGTONE_TYPE_ALL)
            }
        }
        1 -> CategoryFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_CATEGORY_TYPE, CATEGORY_TYPE_ARTIST)
            }
        }
        2 -> CategoryFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_CATEGORY_TYPE, CATEGORY_TYPE_ALBUM)
            }
        }
        3 -> CategoryFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_CATEGORY_TYPE, CATEGORY_TYPE_FOLDER)
            }
        }
        else -> throw IllegalArgumentException("Too bing position: $position")
    }
}