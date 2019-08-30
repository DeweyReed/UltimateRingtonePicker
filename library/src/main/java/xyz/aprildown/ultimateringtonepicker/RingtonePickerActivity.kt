package xyz.aprildown.ultimateringtonepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.urp_activity_ringtone_picker.*
import java.util.ArrayList

/**
 * Created on 2018/6/7.
 */

class RingtonePickerActivity : AppCompatActivity(), RingtonePickerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.urp_activity_ringtone_picker)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra(EXTRA_TITLE)
        }

        if (savedInstanceState == null) {
            val fragment =
                intent.getParcelableExtra<UltimateRingtonePicker.Settings>(EXTRA_SETTINGS)
                    .createFragment()
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

    override fun onRingtonePicked(ringtones: List<RingtonePickerEntry>) {
        setResult(
            Activity.RESULT_OK,
            Intent()
                .putParcelableArrayListExtra(EXTRA_RESULT, ArrayList(ringtones))
        )
        finish()
    }

    companion object {

        private const val EXTRA_TITLE = "title"
        private const val EXTRA_RESULT = "result"

        @JvmStatic
        fun putInfoToLaunchIntent(
            launchIntent: Intent,
            settings: UltimateRingtonePicker.Settings,
            windowTitle: CharSequence
        ): Intent = launchIntent.apply {
            putExtra(EXTRA_SETTINGS, settings)
            putExtra(EXTRA_TITLE, windowTitle)
        }

        @JvmStatic
        fun getPickerResult(intent: Intent): List<RingtonePickerEntry> {
            return intent.getParcelableArrayListExtra(EXTRA_RESULT)
        }
    }
}
