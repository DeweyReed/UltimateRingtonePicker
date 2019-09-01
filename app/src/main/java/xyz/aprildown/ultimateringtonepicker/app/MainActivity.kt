package xyz.aprildown.ultimateringtonepicker.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.RingtonePickerDialog
import xyz.aprildown.ultimateringtonepicker.RingtonePickerEntry
import xyz.aprildown.ultimateringtonepicker.RingtonePickerListener
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker

class MainActivity : AppCompatActivity(), RingtonePickerListener {

    private var currentSelectedRingtones = listOf<RingtonePickerEntry>()

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
                    createStandardSettings(),
                    "Picker Picker"
                ),
                0
            )
        }
        btnDialog.setOnClickListener {
            RingtonePickerDialog.createInstance(
                createStandardSettings(),
                "Dialog!"
            ).show(supportFragmentManager, null)
        }
        btnSystem.setOnClickListener {
            RingtonePickerDialog.createInstance(
                createSystemRingtonePickerSettings(),
                "System Ringtones"
            ).show(supportFragmentManager, null)
        }
        btnAllDeviceRingtones.setOnClickListener {
            RingtonePickerDialog.createInstance(
                createDeviceRingtonesSettings(),
                "All Device Ringtones"
            ).show(supportFragmentManager, null)
        }
    }

    private fun createStandardSettings(): UltimateRingtonePicker.Settings =
        UltimateRingtonePicker.Settings(
            showDefault = true,
            defaultUri = getResourceUri(R.raw.default_ringtone),
            defaultTitle = "Default Ringtone",
            additionalRingtones = listOf(
                RingtonePickerEntry(
                    getResourceUri(R.raw.short_message),
                    "Ringtone from raw"
                )
            ),
            preSelectUris = currentSelectedRingtones.map { it.uri },
            systemRingtoneTypes = UltimateRingtonePicker.Settings.allSystemRingtoneTypes,
            deviceRingtoneTypes = UltimateRingtonePicker.Settings.allDeviceRingtoneTypes
        )

    private fun createSystemRingtonePickerSettings(): UltimateRingtonePicker.Settings =
        UltimateRingtonePicker.Settings(
            showCustomRingtone = false,
            preSelectUris = currentSelectedRingtones.map { it.uri },
            systemRingtoneTypes = UltimateRingtonePicker.Settings.allSystemRingtoneTypes
        )

    private fun createDeviceRingtonesSettings(): UltimateRingtonePicker.Settings =
        UltimateRingtonePicker.Settings(
            onlyShowDevice = true,
            deviceRingtoneTypes = listOf(UltimateRingtonePicker.Settings.DEVICE_RINGTONE_TYPE_ALL)
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            handleResult(RingtonePickerActivity.getPickerResult(data!!))
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRingtonePicked(ringtones: List<RingtonePickerEntry>) {
        handleResult(ringtones)
    }

    private fun handleResult(ringtones: List<RingtonePickerEntry>) {
        currentSelectedRingtones = ringtones
        toast(ringtones.joinToString(separator = "\n") { it.name })
    }
}
