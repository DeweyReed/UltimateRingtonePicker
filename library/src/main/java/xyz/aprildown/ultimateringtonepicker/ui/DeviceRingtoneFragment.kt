package xyz.aprildown.ultimateringtonepicker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_ALBUM
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_ARTIST
import xyz.aprildown.ultimateringtonepicker.CATEGORY_TYPE_FOLDER
import xyz.aprildown.ultimateringtonepicker.KEY_CATEGORY_TYPE
import xyz.aprildown.ultimateringtonepicker.KEY_RINGTONE_TYPE
import xyz.aprildown.ultimateringtonepicker.R
import xyz.aprildown.ultimateringtonepicker.RINGTONE_TYPE_ALBUM
import xyz.aprildown.ultimateringtonepicker.RINGTONE_TYPE_ALL
import xyz.aprildown.ultimateringtonepicker.RINGTONE_TYPE_ARTIST
import xyz.aprildown.ultimateringtonepicker.RINGTONE_TYPE_FOLDER
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
        val viewPager = view.findViewById<ViewPager>(R.id.urpDeviceViewPager)

        viewPager.adapter = CategoryAdapter(this, viewModel.settings.deviceRingtoneTypes)
        tabLayout.setupWithViewPager(viewPager)

        // viewPager.onRestoreInstanceState(savedInstanceState?.getParcelable(KEY_VIEW_PAGER_STATE))
    }

    override fun onSelect() {
        RingtoneFragment.myself?.onSelect()
    }

    override fun onBack(): Boolean {
        viewModel.stopPlaying()
        return if (viewModel.settings.onlyShowDevice) {
            false
        } else {
            findNavController().popBackStack()
        }
    }
}

private class CategoryAdapter(
    fragment: Fragment,
    private val deviceRingtoneTypes: List<Int>
) : FragmentStatePagerAdapter(
    fragment.childFragmentManager,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    override fun getCount(): Int = deviceRingtoneTypes.size

    override fun getItem(position: Int): Fragment {
        return when (deviceRingtoneTypes[position]) {
            RINGTONE_TYPE_ALL -> RingtoneFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_RINGTONE_TYPE, RINGTONE_TYPE_ALL)
                }
            }
            RINGTONE_TYPE_ARTIST -> CategoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_CATEGORY_TYPE, CATEGORY_TYPE_ARTIST)
                }
            }
            RINGTONE_TYPE_ALBUM -> CategoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_CATEGORY_TYPE, CATEGORY_TYPE_ALBUM)
                }
            }
            RINGTONE_TYPE_FOLDER -> CategoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_CATEGORY_TYPE, CATEGORY_TYPE_FOLDER)
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

private const val KEY_VIEW_PAGER_STATE = "view_pager_state"
