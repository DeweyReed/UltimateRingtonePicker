package xyz.aprildown.ultimateringtonepicker.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Created on 2018/9/23.
 */

class MainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    // override fun onMusicPick(uri: Uri, title: String) {
    //     Toast.makeText(requireContext(), "MainFragment\n$title: $uri", Toast.LENGTH_SHORT).show()
    // }
    //
    // override fun onPickCanceled() {
    //     Toast.makeText(requireContext(), "MainFragment\nCanceled", Toast.LENGTH_SHORT).show()
    // }
}