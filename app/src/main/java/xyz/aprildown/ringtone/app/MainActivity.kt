package xyz.aprildown.ringtone.app

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import xyz.aprildown.ringtone.MusicPickerActivity
import xyz.aprildown.ringtone.MusicPickerListener
import xyz.aprildown.ringtone.UltimateMusicPicker

class MainActivity : AppCompatActivity(),
    View.OnClickListener,
    MusicPickerListener {

    private var selectedTitle: String? = null
    private var selectedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpViews()
    }

    private fun setUpViews() {
        switchNightTheme.isChecked =
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        switchNightTheme.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
        }

        switchUseDefault.setOnCheckedChangeListener { _, isChecked ->
            inputDefaultTitle.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        spinnerStreamTypes.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.stream_types, android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        btnCustom.setOnClickListener(this)
        btnLaunch.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnCustom -> {
                UltimateMusicPicker()
                    .windowTitle("CustomCustomCustom")
                    .streamType(AudioManager.STREAM_MUSIC)
                    .alarm().music()
                    .goWithActivity(this, 0, CustomActivity::class.java)
            }
            R.id.btnLaunch -> {
                val picker = UltimateMusicPicker()
                    .windowTitle(inputWindowTitle.editText?.text.toString())

                if (switchUseDefault.isChecked) {
                    picker.defaultTitleAndUri(
                        inputDefaultTitle.editText?.text.toString(),
                        Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                            .authority(packageName)
                            .path((R.raw.custom_default).toString())
                            .build()
                    )
                }

                if (switchRemoveSilent.isChecked) picker.removeSilent()

                if (switchPreSelect.isChecked && selectedUri != null) picker.selectUri(selectedUri!!)

                if (switchAddAdditional.isChecked) {
                    // Any way not to use hardcoded string?
                    picker
                        .additional(
                            "Additional 1",
                            Uri.parse("file:///android_asset/music1.ogg")
                        )
                        .additional(
                            "Additional 2",
                            Uri.parse("file:///android_asset/music2.ogg")
                        )
                }

                val streamType = when (spinnerStreamTypes.selectedItemPosition) {
                    1 -> AudioManager.STREAM_ALARM
                    2 -> AudioManager.STREAM_MUSIC
                    3 -> AudioManager.STREAM_NOTIFICATION
                    4 -> AudioManager.STREAM_RING
                    else -> -1
                }
                if (streamType != -1) picker.streamType(streamType)

                if (switchAddRingtone.isChecked) picker.ringtone()
                if (switchAddNotification.isChecked) picker.notification()
                if (switchAddAlarm.isChecked) picker.alarm()
                if (switchAddMusic.isChecked) picker.music()

                if (switchShowDialog.isChecked) {
                    picker.goWithDialog(supportFragmentManager)
                } else {
                    picker.goWithActivity(this, 0, MusicPickerActivity::class.java)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra(UltimateMusicPicker.EXTRA_SELECTED_TITLE)
            val uri = data?.getParcelableExtra<Uri>(UltimateMusicPicker.EXTRA_SELECTED_URI)
            if (title != null && uri != null) {
                onMusicPick(uri, title)
            } else {
                onPickCanceled()
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onMusicPick(uri: Uri, title: String) {
        selectedTitle = title
        selectedUri = uri
        toast("$title: $uri")
    }

    override fun onPickCanceled() {
        selectedTitle = null
        selectedUri = null
        toast("Canceled")
    }
}
