package xyz.aprildown.ringtone.ui

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import xyz.aprildown.ringtone.MUSIC_SILENT
import xyz.aprildown.ringtone.MusicPickerListener
import xyz.aprildown.ringtone.MusicPickerSetting
import xyz.aprildown.ringtone.R
import xyz.aprildown.ringtone.UltimateMusicPicker.Companion.EXTRA_SETTING_BUNDLE
import xyz.aprildown.ringtone.music.AsyncRingtonePlayer

/**
 * Created on 2018/6/7.
 */

class MusicPickerFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG_FRAGMENT = "tag_fragment"

        internal fun newInstance(
                setting: MusicPickerSetting?,
                listener: MusicPickerListener): MusicPickerFragment {
            return MusicPickerFragment().apply {
                arguments = Bundle().apply { putParcelable(EXTRA_SETTING_BUNDLE, setting) }
                musicPickerListener = listener
            }
        }
    }

    private lateinit var viewModel: PickerViewModel

    private lateinit var localContext: Context
    private lateinit var musicPickerListener: MusicPickerListener

    internal lateinit var musicPlayer: AsyncRingtonePlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(PickerViewModel::class.java)
        if (savedInstanceState == null) {
            viewModel.setMusicPickerSetting(arguments?.getParcelable(EXTRA_SETTING_BUNDLE))
        }
        return inflater.inflate(R.layout.fragment_music_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        localContext = view.context
        musicPlayer = AsyncRingtonePlayer(localContext)
        activity?.volumeControlStream = viewModel.setting.streamType

        view.findViewById<View>(R.id.btnRingtoneCancel).setOnClickListener(this)
        view.findViewById<View>(R.id.btnRingtoneSelect).setOnClickListener(this)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.layoutMusicPicker, PickerNormalFragment(), TAG_FRAGMENT)
                    .commit()
        }
    }

    override fun onStop() {
        super.onStop()
        musicPlayer.stop()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRingtoneCancel -> {
                if (isCustomFragment()) {
                    customPicked(null)
                } else {
                    musicPicked(null)
                }
            }
            R.id.btnRingtoneSelect -> {
                if (isCustomFragment()) {
                    customPicked(getSelectedSoundItem())
                } else {
                    musicPicked(getSelectedSoundItem())
                }
            }
        }
    }

    fun isBackHandled(): Boolean {
        val cfm = childFragmentManager
        return if (cfm.backStackEntryCount > 0) {
            cfm.popBackStack()
            true
        } else false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (permissions.size == 1 && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE
                && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCustom()
        } else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("InlinedApi")
    internal fun toCustom() {
        val context = requireContext()
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                AlertDialog.Builder(context)
                        .setMessage(R.string.permission_external_rational)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            requestPermissions(arrayOf(permission), 0)
                        }
                        .show()
            } else {
                requestPermissions(arrayOf(permission), 0)
            }
        } else {
            launchCustom()
        }
    }

    private fun launchCustom() {
        if (isNormalFragment()) {
            stopPlayingMusic(getSelectedSoundItem(), false)
            childFragmentManager.beginTransaction()
                    // first: enter animation for enter fragment
                    // second: exit animation for exit fragment
                    // third: enter animation for enter fragment when popBackStack
                    // fourth: exit animation for exit fragment when popBackStack
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.layoutMusicPicker, PickerCustomFragment(), TAG_FRAGMENT)
                    .addToBackStack(null)
                    .commit()
        }
    }

    internal fun customPicked(soundItem: SoundItem?) {
        val cfm = childFragmentManager
        if (isCustomFragment()) {
            cfm.popBackStackImmediate()
            val uri = soundItem?.uri
            val title = soundItem?.title
            if (uri != null && title != null) {
                val normal = getCurrentFragment()
                if (normal is PickerNormalFragment) {
                    normal.onCustomPicked(uri, title)
                }
            }
        }
    }

    internal fun startPlayingMusic(item: SoundItem) {
        if (!item.isPlaying && item.uri != MUSIC_SILENT) {
            musicPlayer.play(item.uri, true, viewModel.setting.streamType)
            item.isPlaying = true
            viewModel.isPreviewPlaying = true
        }
        if (!item.isSelected) {
            item.isSelected = true
            viewModel.selectedUri = item.uri
        }
    }

    internal fun stopPlayingMusic(item: SoundItem?, deselect: Boolean) {
        if (item != null) {
            if (item.isPlaying) {
                musicPlayer.stop()
                item.isPlaying = false
                viewModel.isPreviewPlaying = false
            }
            if (deselect && item.isSelected) {
                item.isSelected = false
                viewModel.selectedUri = null
            }
        }
    }

    private fun musicPicked(soundItem: SoundItem?) {
        val uri = soundItem?.uri
        val title = soundItem?.title
        if (uri != null && title != null) {
            musicPickerListener.onMusicPick(uri, title)
        } else {
            musicPickerListener.onPickCanceled()
        }
    }

    private fun getSelectedSoundItem() = getCurrentFragment().getSelectedSoundItem()

    private fun getCurrentFragment() = childFragmentManager
            .findFragmentByTag(TAG_FRAGMENT) as? PickerBaseFragment
            ?: throw IllegalStateException("Requires PickerBaseFragment in the MusicpickerFragment")

    private fun isNormalFragment() = getCurrentFragment() is PickerNormalFragment
    private fun isCustomFragment() = getCurrentFragment() is PickerCustomFragment
}