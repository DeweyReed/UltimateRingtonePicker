package xyz.aprildown.ultimateringtonepicker.app

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import xyz.aprildown.ultimateringtonepicker.MusicPickerListener
import xyz.aprildown.ultimateringtonepicker.UltimateMusicPicker

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
        Toast.makeText(requireContext(), "MainFragment\n$title: $uri", Toast.LENGTH_SHORT).show()
    }

    override fun onPickCanceled() {
        Toast.makeText(requireContext(), "MainFragment\nCanceled", Toast.LENGTH_SHORT).show()
    }
}