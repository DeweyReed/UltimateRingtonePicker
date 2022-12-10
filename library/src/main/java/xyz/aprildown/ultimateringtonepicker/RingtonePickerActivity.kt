package xyz.aprildown.ultimateringtonepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xyz.aprildown.ultimateringtonepicker.databinding.UrpActivityRingtonePickerBinding

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
                intent.getParcelableExtraCompat<UltimateRingtonePicker.Settings>(EXTRA_SETTINGS)!!
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
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onRingtonePicked(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(EXTRA_RESULT, ringtones.toTypedArray())
        )
        finish()
    }

    private fun getRingtonePickerFragment(): RingtonePickerFragment {
        return supportFragmentManager.findFragmentByTag(TAG_RINGTONE_PICKER) as RingtonePickerFragment
    }

    companion object {

        private const val EXTRA_TITLE = "title"
        private const val EXTRA_RESULT = "result"

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
        fun getPickerResult(intent: Intent?): List<UltimateRingtonePicker.RingtoneEntry> {
            if (intent == null) return emptyList()
            return intent
                .getParcelableArrayExtraCompat<UltimateRingtonePicker.RingtoneEntry>(EXTRA_RESULT)
                .toList()
        }
    }
}
