package xyz.aprildown.ringtone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xyz.aprildown.ringtone.UltimateMusicPicker.Companion.EXTRA_WINDOW_TITLE
import xyz.aprildown.ringtone.ui.MusicPickerFragment

/**
 * Created on 2018/6/7.
 */

class MusicPickerActivity : AppCompatActivity(), MusicPickerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra(EXTRA_WINDOW_TITLE)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    android.R.id.content,
                    UltimateMusicPicker.createFragmentFromIntent(intent)
                )
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(android.R.id.content)
        if (fragment == null ||
            (fragment is MusicPickerFragment && !fragment.isBackHandled())
        ) {
            super.onBackPressed()
        }
    }

    override fun onMusicPick(uri: Uri, title: String) {
        setResult(
            Activity.RESULT_OK,
            Intent()
                .putExtra(UltimateMusicPicker.EXTRA_SELECTED_URI, uri)
                .putExtra(UltimateMusicPicker.EXTRA_SELECTED_TITLE, title)
        )
        finish()
    }

    override fun onPickCanceled() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}