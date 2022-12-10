package xyz.aprildown.ultimateringtonepicker

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import xyz.aprildown.ultimateringtonepicker.ui.EventHandler

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

    private lateinit var pickListener: UltimateRingtonePicker.RingtonePickerListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pickListener = requireRingtonePickerListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val settings =
            arguments?.getParcelableCompat(EXTRA_SETTINGS) ?: UltimateRingtonePicker.Settings()

        navController.graph = navController.navInflater.inflate(R.navigation.urp_nav_graph).apply {
            setStartDestination(
                if (settings.systemRingtonePicker == null) {
                    R.id.urp_dest_device
                } else {
                    R.id.urp_dest_system
                }
            )
        }

        val viewModel by navGraphViewModels<RingtonePickerViewModel>(R.id.urp_nav_graph) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return when (modelClass) {
                        RingtonePickerViewModel::class.java ->
                            RingtonePickerViewModel(requireActivity().application, settings) as T
                        else -> throw IllegalArgumentException()
                    }
                }
            }
        }

        viewModel.finalSelection.observe(viewLifecycleOwner) { ringtones ->
            if (ringtones != null) {
                pickListener.onRingtonePicked(
                    ringtones.filter { it.isValid }
                        .map {
                            UltimateRingtonePicker.RingtoneEntry(uri = it.uri, name = it.title)
                        }
                )
            }
        }
    }

    private fun getTopFragment(): Fragment? {
        return childFragmentManager.primaryNavigationFragment
    }

    fun onSelectClick() {
        (getTopFragment() as? EventHandler)?.onSelect()
    }

    /**
     * @return If the back event is consumed.
     *         If it isn't consumed(false), you can finish the activity or the dialog.
     */
    fun onBackClick(): Boolean {
        return (getTopFragment() as? EventHandler)?.onBack() == true
    }
}
