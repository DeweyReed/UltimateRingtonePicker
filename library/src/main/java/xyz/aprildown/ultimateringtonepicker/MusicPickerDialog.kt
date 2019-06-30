package xyz.aprildown.ultimateringtonepicker

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import xyz.aprildown.ultimateringtonepicker.UltimateMusicPicker.Companion.EXTRA_SETTING_BUNDLE
import xyz.aprildown.ultimateringtonepicker.UltimateMusicPicker.Companion.EXTRA_WINDOW_TITLE
import xyz.aprildown.ultimateringtonepicker.ui.MusicPickerFragment

/**
 * Created on 2018/9/9.
 */

class MusicPickerDialog : DialogFragment(), MusicPickerListener {

    companion object {
        fun newInstance(parcelable: Parcelable, dialogTitle: String): MusicPickerDialog {
            val args = Bundle().apply {
                putParcelable(EXTRA_SETTING_BUNDLE, parcelable)
                putString(EXTRA_WINDOW_TITLE, dialogTitle)
            }
            val dialog = MusicPickerDialog()
            dialog.arguments = args
            return dialog
        }
    }

    private lateinit var musicPickerListener: MusicPickerListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.UMP_Theme)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        musicPickerListener = when {
            // In case using picker dialog in a fragment
            parentFragment is MusicPickerListener -> parentFragment as MusicPickerListener
            context is MusicPickerListener -> context
            activity is MusicPickerListener -> activity as MusicPickerListener
            else -> throw IllegalStateException("MusicPickerDialog requires a MusicPickerListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_music_picker, container, false)

        val header = view.findViewById<TextView>(R.id.textMusicHeader)
        arguments?.getString(EXTRA_WINDOW_TITLE).let {
            if (it.isNullOrBlank()) header.gone() else header.text = it
        }

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(
                    R.id.layoutDialogMusicPicker, MusicPickerFragment.newInstance(
                        arguments?.getParcelable(EXTRA_SETTING_BUNDLE)
                    )
                )
                .commit()
        }
        return view
    }

    override fun onMusicPick(uri: Uri, title: String) {
        musicPickerListener.onMusicPick(uri, title)
        dismiss()
    }

    override fun onPickCanceled() {
        musicPickerListener.onPickCanceled()
        dismiss()
    }
}