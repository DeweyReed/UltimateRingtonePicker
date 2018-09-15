package xyz.aprildown.ringtone

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import xyz.aprildown.ringtone.UltimateMusicPicker.Companion.EXTRA_SETTING_BUNDLE
import xyz.aprildown.ringtone.UltimateMusicPicker.Companion.EXTRA_WINDOW_TITLE
import xyz.aprildown.ringtone.ui.MusicPickerFragment

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

    internal lateinit var musicPickerListener: MusicPickerListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.UMP_Theme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_music_picker, container, false)

        val header = view.findViewById<TextView>(R.id.textMusicHeader)
        arguments?.getString(EXTRA_WINDOW_TITLE).let {
            if (it.isNullOrBlank()) header.gone() else header.text = it
        }

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.layoutDialogMusicPicker, MusicPickerFragment.newInstance(
                            arguments?.getParcelable(EXTRA_SETTING_BUNDLE),
                            this@MusicPickerDialog))
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