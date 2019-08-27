package xyz.aprildown.ultimateringtonepicker.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Created on 2018/9/16.
 */

class CustomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

        // supportActionBar?.run {
        //     setDisplayHomeAsUpEnabled(true)
        //     title = intent.getStringExtra(UltimateMusicPicker.EXTRA_WINDOW_TITLE)
        // }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // override fun onMusicPick(uri: Uri, title: String) {
    //     setResult(
    //         Activity.RESULT_OK,
    //         Intent()
    //             .putExtra(UltimateMusicPicker.EXTRA_SELECTED_URI, uri)
    //             .putExtra(UltimateMusicPicker.EXTRA_SELECTED_TITLE, title)
    //     )
    //     finish()
    // }
    //
    // override fun onPickCanceled() {
    //     setResult(Activity.RESULT_CANCELED)
    //     finish()
    // }
}