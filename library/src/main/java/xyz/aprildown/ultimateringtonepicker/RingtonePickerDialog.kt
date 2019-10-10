package xyz.aprildown.ultimateringtonepicker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class RingtonePickerDialog : DialogFragment(), RingtonePickerListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                handleBack()
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
            val fragment = arguments?.getParcelable<UltimateRingtonePicker.Settings>(EXTRA_SETTINGS)
                ?.createFragment()!!
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
            handleBack()
        }
        view.findViewById<View>(R.id.urpBtnDialogSelect).setOnClickListener {
            (childFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER) as RingtonePickerFragment)
                .onSelectClick()
        }
    }

    private fun handleBack() {
        if ((childFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER)
                    as RingtonePickerFragment).onBackClick()
        ) {
            dismiss()
        }
    }

    override fun onRingtonePicked(ringtones: List<RingtonePickerEntry>) {
        findRingtonePickerListener().onRingtonePicked(ringtones)
        dismiss()
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
