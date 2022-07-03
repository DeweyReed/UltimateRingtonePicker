package xyz.aprildown.ultimateringtonepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import xyz.aprildown.ultimateringtonepicker.databinding.UrpActivityRingtonePickerBinding
import java.util.ArrayList

/**
 * Created on 2018/6/7.
 */

class RingtonePickerActivity : AppCompatActivity(), UltimateRingtonePicker.RingtonePickerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = UrpActivityRingtonePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = intent.getStringExtra(EXTRA_TITLE)
        }

        if (savedInstanceState == null) {
            val fragment =
                intent.getParcelableExtra<UltimateRingtonePicker.Settings>(EXTRA_SETTINGS)!!
                    .createFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.layoutRingtonePicker, fragment, TAG_RINGTONE_PICKER)
                .setPrimaryNavigationFragment(fragment)
                .commit()
        }

        binding.btnSelect.setOnClickListener {
            getRingtonePickerFragment().onSelectClick()
        }
        binding.btnCancel.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        /**
         * Save if the dark mode is on or not
         */
        SharedPrefUtils.init(this)
        SharedPrefUtils.write(IS_DARK_MODE, intent.getBooleanExtra(IS_DARK_MODE, false))
        changeTheme()
    }

    private fun changeTheme() {
        if (!SharedPrefUtils.contains(IS_DARK_MODE)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            if (SharedPrefUtils.readBoolean(IS_DARK_MODE)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onRingtonePicked(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
        setResult(
            Activity.RESULT_OK,
            Intent().putParcelableArrayListExtra(EXTRA_RESULT, ArrayList(ringtones))
        )
        finish()
    }

    private fun getRingtonePickerFragment(): RingtonePickerFragment {
        return supportFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER) as RingtonePickerFragment
    }

    companion object {

        private const val EXTRA_TITLE = "title"
        private const val EXTRA_RESULT = "result"
        const val IS_DARK_MODE = "is_dark_mode"

        @JvmStatic
        fun getIntent(
                isDarkMode: Boolean,
            context: Context,
            settings: UltimateRingtonePicker.Settings,
            windowTitle: CharSequence
        ): Intent = Intent(context, RingtonePickerActivity::class.java).apply {
            putExtra(EXTRA_SETTINGS, settings)
            putExtra(EXTRA_TITLE, windowTitle)
            putExtra(IS_DARK_MODE, isDarkMode)
        }

        @JvmStatic
        fun getIntent(
                context: Context,
                settings: UltimateRingtonePicker.Settings,
                windowTitle: CharSequence
        ): Intent = Intent(context, RingtonePickerActivity::class.java).apply {
            putExtra(EXTRA_SETTINGS, settings)
            putExtra(EXTRA_TITLE, windowTitle)
        }


        @JvmStatic
        fun getPickerResult(intent: Intent): List<UltimateRingtonePicker.RingtoneEntry> {
            return intent.getParcelableArrayListExtra(EXTRA_RESULT)!!
        }
    }
}
