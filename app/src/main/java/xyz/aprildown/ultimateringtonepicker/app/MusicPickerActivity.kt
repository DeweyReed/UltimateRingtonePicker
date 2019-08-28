package xyz.aprildown.ultimateringtonepicker.app

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_ringtone_picker.*
import xyz.aprildown.ultimateringtonepicker.RingtonePickerFragment
import xyz.aprildown.ultimateringtonepicker.RingtonePickerListener

/**
 * Created on 2018/6/7.
 */

class MusicPickerActivity : AppCompatActivity(),
    RingtonePickerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ringtone_picker)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
        }

        if (savedInstanceState == null) {
            val fragment = RingtonePickerFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.layoutRingtonePicker, fragment, TAG_RINGTONE_PICKER)
                .setPrimaryNavigationFragment(fragment)
                .commit()
        }

        btnSelect.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER)
            (fragment as RingtonePickerFragment).onSelectClick()
        }
        btnCancel.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER)
            if ((fragment as RingtonePickerFragment).onBackClick()) {
                toast("Back")
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER)
        return if ((fragment as RingtonePickerFragment).onBackClick()) {
            finish()
            true
        } else {
            false
        }
    }

    override fun onRingtonePicked(ringtones: List<Pair<Uri, String>>) {
        toast(ringtones.joinToString(separator = "\n") { it.second })
        finish()
    }
}

private const val TAG_RINGTONE_PICKER = "ringtone_picker"
