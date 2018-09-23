package xyz.aprildown.ringtone.app

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.jetbrains.anko.toast
import xyz.aprildown.ringtone.MusicPickerListener
import xyz.aprildown.ringtone.UltimateMusicPicker

/**
 * Created on 2018/9/23.
 */

class MainFragment : Fragment(),
    MusicPickerListener {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnFragmentLaunch).setOnClickListener {
            UltimateMusicPicker()
                .windowTitle("Picker")
                .ringtone()
                .goWithDialog(childFragmentManager)
        }
    }

    override fun onMusicPick(uri: Uri, title: String) {
        requireContext().toast("MainFragment\n$title: $uri")
    }

    override fun onPickCanceled() {
        requireContext().toast("MainFragment\nCanceled")
    }
}