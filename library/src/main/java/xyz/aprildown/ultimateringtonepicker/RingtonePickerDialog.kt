package xyz.aprildown.ultimateringtonepicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.DialogFragment

class RingtonePickerDialog : DialogFragment(), UltimateRingtonePicker.RingtonePickerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!getRingtonePickerFragment().onBackClick()) {
                remove()
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.urp_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragment =
                requireArguments().getParcelable<UltimateRingtonePicker.Settings>(EXTRA_SETTINGS)!!
                    .createFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.urpFrameDialog, fragment, TAG_RINGTONE_PICKER)
                .setPrimaryNavigationFragment(fragment)
                .commit()
        }

        view.findViewById<TextView>(R.id.urpTextDialogTitle).run {
            val title = arguments?.getCharSequence(EXTRA_TITLE)
            if (title.isNullOrBlank()) {
                gone()
            } else {
                text = title
            }
        }
        view.findViewById<View>(R.id.urpBtnDialogCancel).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        view.findViewById<View>(R.id.urpBtnDialogSelect).setOnClickListener {
            getRingtonePickerFragment().onSelectClick()
        }
    }

    override fun onRingtonePicked(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
        requireRingtonePickerListener().onRingtonePicked(ringtones)
        dismiss()
    }

    private fun getRingtonePickerFragment(): RingtonePickerFragment {
        return childFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER) as RingtonePickerFragment
    }

    companion object {
        private const val EXTRA_TITLE = "title"

        @JvmStatic
        fun createInstance(
            settings: UltimateRingtonePicker.Settings,
            dialogTitle: CharSequence
        ): RingtonePickerDialog = RingtonePickerDialog().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_SETTINGS, settings)
                putCharSequence(EXTRA_TITLE, dialogTitle)
            }
        }
    }
}
