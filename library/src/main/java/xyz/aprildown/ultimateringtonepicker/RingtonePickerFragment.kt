package xyz.aprildown.ultimateringtonepicker

import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import xyz.aprildown.ultimateringtonepicker.ui.Navigator

/**
 * Structure:
 * - [RingtonePickerFragment]
 *     - [SystemRingtoneFragment]
 *     - [DeviceRingtoneFragment]
 *       - [RingtoneFragment]
 *       - [CategoryFragment]
 *         - [RingtoneFragment]
 */
class RingtonePickerFragment : NavHostFragment() {

    private lateinit var pickListener: RingtonePickerListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pickListener = findRingtonePickerListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val settings = arguments?.getParcelable(EXTRA_SETTINGS)
            ?: UltimateRingtonePicker.Settings(
                showCustomRingtone = true,
                showDefault = false,
                showSilent = true,
                systemRingtoneTypes = listOf(
                    RingtoneManager.TYPE_RINGTONE,
                    RingtoneManager.TYPE_NOTIFICATION,
                    RingtoneManager.TYPE_ALARM
                )
            )

        navController.graph = navController.navInflater.inflate(R.navigation.urp_nav_graph).apply {
            startDestination =
                if (settings.onlyShowDevice) R.id.urp_dest_device else R.id.urp_dest_system
        }

        val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return when (modelClass) {
                        RingtonePickerViewModel::class.java ->
                            RingtonePickerViewModel(requireActivity().application, settings) as T
                        else -> throw IllegalArgumentException()
                    }
                }
            }
        }

        viewModel.finalSelection.observe(viewLifecycleOwner, Observer { ringtones ->
            if (ringtones != null) {
                pickListener.onRingtonePicked(ringtones.filter { it.isValid }.map {
                    RingtonePickerEntry(it.uri, it.title)
                })
            }
        })
    }

    private fun findOurTopFragment(): Fragment? = childFragmentManager.primaryNavigationFragment

    fun onSelectClick() {
        val topFragment = findOurTopFragment()
        if (topFragment is Navigator.Selector) {
            topFragment.onSelect()
        }
    }

    /**
     * @return True if the back stack is empty and you should close the activity or dialog.
     *         False if the back stack is popped up once and you should do nothing.
     */
    fun onBackClick(): Boolean {
        val topFragment = findOurTopFragment()
        return if (topFragment is Navigator.Selector) {
            !topFragment.onBack()
        } else {
            true
        }
    }
}
