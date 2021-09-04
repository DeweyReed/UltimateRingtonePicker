package xyz.aprildown.ultimateringtonepicker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import xyz.aprildown.ultimateringtonepicker.databinding.UrpDialogBinding

class RingtonePickerDialog : DialogFragment(), UltimateRingtonePicker.RingtonePickerListener {

    private var directListener: UltimateRingtonePicker.RingtonePickerListener? = null
    private lateinit var binding: UrpDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = UrpDialogBinding.inflate(layoutInflater)
        val builder = MaterialAlertDialogBuilder(requireContext())

        val title = arguments?.getCharSequence(EXTRA_TITLE)

        builder.apply {
            setView(binding.root)
            if (!title.isNullOrBlank()) {
                setTitle(title)
            }
            setNegativeButton(android.R.string.cancel) { _, _ -> handleBack() }
            setPositiveButton(android.R.string.ok) { _, _ -> getRingtonePickerFragment().onSelectClick() }
        }
        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val arguments = requireArguments()
        if (arguments.getBoolean(EXTRA_EPHEMERAL) && directListener == null) {
            dismiss()
        }

        if (savedInstanceState == null) {
            val fragment =
                arguments.getParcelable<UltimateRingtonePicker.Settings>(EXTRA_SETTINGS)!!
                    .createFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.urpFrameDialog, fragment, TAG_RINGTONE_PICKER)
                .setPrimaryNavigationFragment(fragment)
                .commit()
        }
    }

    override fun onRingtonePicked(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
        (directListener ?: requireRingtonePickerListener()).onRingtonePicked(ringtones)
        dismiss()
    }

    private fun handleBack() {
        if (!getRingtonePickerFragment().onBackClick()) {
            dismiss()
        }
    }

    private fun getRingtonePickerFragment(): RingtonePickerFragment {
        return childFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER) as RingtonePickerFragment
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_EPHEMERAL = "ephemeral"

        @JvmStatic
        fun createInstance(
            settings: UltimateRingtonePicker.Settings,
            dialogTitle: CharSequence?
        ): RingtonePickerDialog = RingtonePickerDialog().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_SETTINGS, settings)
                putCharSequence(EXTRA_TITLE, dialogTitle)
            }
        }

        /**
         * The dialog will be dismissed in onPause but give you the result directly in the [listener].
         */
        @JvmStatic
        fun createEphemeralInstance(
            settings: UltimateRingtonePicker.Settings,
            dialogTitle: CharSequence?,
            listener: UltimateRingtonePicker.RingtonePickerListener
        ): RingtonePickerDialog = RingtonePickerDialog().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_SETTINGS, settings)
                putCharSequence(EXTRA_TITLE, dialogTitle)
                putBoolean(EXTRA_EPHEMERAL, true)
            }
            directListener = listener
        }
    }
}
