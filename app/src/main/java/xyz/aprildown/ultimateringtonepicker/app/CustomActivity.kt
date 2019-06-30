package xyz.aprildown.ultimateringtonepicker.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xyz.aprildown.ultimateringtonepicker.MusicPickerListener
import xyz.aprildown.ultimateringtonepicker.UltimateMusicPicker
import xyz.aprildown.ultimateringtonepicker.ui.MusicPickerFragment

/**
 * Created on 2018/9/16.
 */

class CustomActivity : AppCompatActivity(), MusicPickerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra(UltimateMusicPicker.EXTRA_WINDOW_TITLE)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fragment,
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