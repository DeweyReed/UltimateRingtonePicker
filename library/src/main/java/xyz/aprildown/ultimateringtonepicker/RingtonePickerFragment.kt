package xyz.aprildown.ultimateringtonepicker

import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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
class RingtonePickerFragment : Fragment() {

    private lateinit var pickListener: RingtonePickerListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        pickListener = when {
            // Check parentFragment first in case using MusicPickerDialog
            parentFragment is RingtonePickerListener -> parentFragment as RingtonePickerListener
            context is RingtonePickerListener -> context
            activity is RingtonePickerListener -> activity as RingtonePickerListener
            else -> throw IllegalStateException("Cannot find RingtonePickerListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.urp_fragment_ringtone_picker, container, false)
        // if (savedInstanceState == null) {
        //     val navHostFragment = NavHostFragment.create(R.navigation.urp_nav_graph)
        //     childFragmentManager.beginTransaction()
        //         .replace(R.id.urpFrameNavHost, navHostFragment, "host")
        //         .setPrimaryNavigationFragment(navHostFragment)
        //         .commit()
        // }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = view.context

        val navController = findOurNavController()
        val viewModel = ViewModelProvider(
            navController.getViewModelStoreOwner(R.id.urp_nav_graph),
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return when (modelClass) {
                        RingtonePickerViewModel::class.java -> RingtonePickerViewModel(
                            context,
                            arguments?.getParcelable(EXTRA_SETTINGS)
                                ?: UltimateRingtonePicker.Settings(
                                    showCustomRingtone = true,
                                    showDefault = false,
                                    showSilent = true,
                                    ringtoneTypes = listOf(
                                        RingtoneManager.TYPE_RINGTONE,
                                        RingtoneManager.TYPE_NOTIFICATION,
                                        RingtoneManager.TYPE_ALARM
                                    )
                                )
                        ) as T
                        else -> throw IllegalArgumentException()
                    }
                }
            }
        ).get(RingtonePickerViewModel::class.java)

        viewModel.totalSelection.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                pickListener.onRingtonePicked(it.map { ringtone -> ringtone.uri to ringtone.title })
            }
        })
    }

    private fun findOurNavController(): NavController =
        (childFragmentManager.findFragmentById(R.id.urpFragmentNavHost) as NavHostFragment)
            .navController

    fun onSelectClick() {
        val topFragment =
            (childFragmentManager.findFragmentById(R.id.urpFragmentNavHost) as NavHostFragment).childFragmentManager.primaryNavigationFragment
        if (topFragment is Navigator.Selector) {
            topFragment.onSelect()
        }
    }

    /**
     * @return True if the back stack is empty and you should close the activity or dialog.
     *         False if the back stack is popped up once and you should do nothing.
     */
    fun onBackClick(): Boolean {
        val topFragment =
            (childFragmentManager.findFragmentById(R.id.urpFragmentNavHost) as NavHostFragment).childFragmentManager.primaryNavigationFragment
        return if (topFragment is Navigator.Selector) {
            !topFragment.onBack()
        } else {
            true
        }
    }
}