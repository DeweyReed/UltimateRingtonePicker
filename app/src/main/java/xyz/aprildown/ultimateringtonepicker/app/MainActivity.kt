package xyz.aprildown.ultimateringtonepicker.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.RingtonePickerDialog
import xyz.aprildown.ultimateringtonepicker.RingtonePickerListener
import xyz.aprildown.ultimateringtonepicker.RingtonePickerResult
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker

class MainActivity : AppCompatActivity(), RingtonePickerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpViews()
    }

    private fun setUpViews() {
        btnActivity.setOnClickListener {
            startActivityForResult(
                RingtonePickerActivity.putInfoToLaunchIntent(
                    Intent(this, RingtonePickerActivity::class.java),
                    UltimateRingtonePicker.Settings(
                        showCustomRingtone = true,
                        showDefault = true,
                        defaultUri = getResourceUri(R.raw.default_ringtone),
                        defaultTitle = "Default Title",
                        showSilent = true,
                        systemRingtoneTypes = UltimateRingtonePicker.Settings.allSystemRingtoneTypes,
                        useSafSelect = false,
                        deviceRingtoneTypes = UltimateRingtonePicker.Settings.allDeviceRingtoneTypes
                    ),
                    "Picker Picker"
                ),
                0
            )
        }
        btnDialog.setOnClickListener {
            RingtonePickerDialog.createInstance(
                UltimateRingtonePicker.Settings(
                    showCustomRingtone = true,
                    showDefault = false,
                    showSilent = true,
                    systemRingtoneTypes = UltimateRingtonePicker.Settings.allSystemRingtoneTypes,
                    enableMultiSelect = true,
                    useSafSelect = true
                ),
                "Dialog!"
            ).show(supportFragmentManager, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            showResult(RingtonePickerActivity.getPickerResult(data!!))
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRingtonePicked(ringtones: List<RingtonePickerResult>) {
        showResult(ringtones)
    }

    private fun showResult(ringtones: List<RingtonePickerResult>) {
        toast(ringtones.joinToString(separator = "\n") { it.name })
    }
}
