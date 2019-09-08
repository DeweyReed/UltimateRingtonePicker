package xyz.aprildown.ultimateringtonepicker.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.RingtonePickerDialog
import xyz.aprildown.ultimateringtonepicker.RingtonePickerEntry
import xyz.aprildown.ultimateringtonepicker.RingtonePickerListener
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import java.io.File

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity(), RingtonePickerListener {

    private var currentSelectedRingtones = listOf<RingtonePickerEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openPermission(view: View) {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:${packageName}"))
        )
    }

    fun openStandardActivity(view: View) {
        startActivityForResult(
            RingtonePickerActivity.putInfoToLaunchIntent(
                Intent(this, RingtonePickerActivity::class.java),
                createStandardSettings(),
                "Picker Picker"
            ),
            REQUEST_CODE_ACTIVITY
        )
    }

    fun openStandardDialog(view: View) {
        RingtonePickerDialog.createInstance(
            createStandardSettings(),
            "Dialog!"
        ).show(supportFragmentManager, null)
    }

    fun emulateSystemDialog(view: View) {
        RingtonePickerDialog.createInstance(
            UltimateRingtonePicker.Settings(
                showCustomRingtone = false,
                preSelectUris = currentSelectedRingtones.map { it.uri },
                systemRingtoneTypes = UltimateRingtonePicker.Settings.allSystemRingtoneTypes
            ),
            "System Ringtones"
        ).show(supportFragmentManager, null)
    }

    fun showOnlyDeviceRingtones(view: View) {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (EasyPermissions.hasPermissions(this, permission)) {
            pickDeviceRingtones()
        } else {
            EasyPermissions.requestPermissions(
                PermissionRequest.Builder(this, 0, permission).build()
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @Suppress("unused")
    @AfterPermissionGranted(0)
    fun onGetPermission() {
        pickDeviceRingtones()
    }

    private fun pickDeviceRingtones() {
        RingtonePickerDialog.createInstance(
            UltimateRingtonePicker.Settings(
                onlyShowDevice = true,
                deviceRingtoneTypes = listOf(UltimateRingtonePicker.Settings.DEVICE_RINGTONE_TYPE_ALL)
            ),
            "All Device Ringtones"
        ).show(supportFragmentManager, null)
    }

    fun useAdditionalRingtones(view: View) {
        startActivityForResult(
            RingtonePickerActivity.putInfoToLaunchIntent(
                Intent(this, RingtonePickerActivity::class.java),
                UltimateRingtonePicker.Settings(
                    showCustomRingtone = false,
                    showDefault = true,
                    defaultUri = UltimateRingtonePicker.Settings.createRawUri(
                        this,
                        R.raw.default_ringtone
                    ),
                    defaultTitle = "Default Ringtone",
                    additionalRingtones = listOf(
                        RingtonePickerEntry(
                            UltimateRingtonePicker.Settings.createRawUri(
                                this,
                                R.raw.short_message
                            ),
                            "R.raw.short_message"
                        ), RingtonePickerEntry(
                            UltimateRingtonePicker.Settings.createAssetUri("asset1.wav"),
                            "Assets/asset1.mp3"
                        ),
                        RingtonePickerEntry(
                            UltimateRingtonePicker.Settings.createAssetUri("ringtones${File.separator}asset2.mp3"),
                            "Assets/ringtones/asset2.mp3"
                        )
                    ),
                    preSelectUris = currentSelectedRingtones.map { it.uri }
                ),
                "Additional"
            ),
            REQUEST_CODE_ACTIVITY
        )
    }

    fun enableMultiSelect(view: View) {
        startActivityForResult(
            RingtonePickerActivity.putInfoToLaunchIntent(
                Intent(this, RingtonePickerActivity::class.java),
                UltimateRingtonePicker.Settings(
                    defaultUri = UltimateRingtonePicker.Settings.createRawUri(
                        this,
                        R.raw.default_ringtone
                    ),
                    preSelectUris = currentSelectedRingtones.map { it.uri },
                    enableMultiSelect = true,
                    systemRingtoneTypes = UltimateRingtonePicker.Settings.allSystemRingtoneTypes,
                    deviceRingtoneTypes = UltimateRingtonePicker.Settings.allDeviceRingtoneTypes
                ),
                "Multi Select"
            ),
            REQUEST_CODE_ACTIVITY
        )
    }

    fun safPick(view: View) {
        RingtonePickerDialog.createInstance(
            UltimateRingtonePicker.Settings(
                preSelectUris = currentSelectedRingtones.map { it.uri },
                useSafSelect = true,
                systemRingtoneTypes = UltimateRingtonePicker.Settings.allSystemRingtoneTypes
            ),
            "All Device Ringtones"
        ).show(supportFragmentManager, null)
    }

    private fun createStandardSettings(): UltimateRingtonePicker.Settings =
        UltimateRingtonePicker.Settings(
            showDefault = true,
            defaultUri = UltimateRingtonePicker.Settings.createRawUri(this, R.raw.default_ringtone),
            defaultTitle = "Default Ringtone",
            additionalRingtones = listOf(
                RingtonePickerEntry(
                    UltimateRingtonePicker.Settings.createRawUri(this, R.raw.short_message),
                    "R.raw.short_message"
                )
            ),
            preSelectUris = currentSelectedRingtones.map { it.uri },
            systemRingtoneTypes = UltimateRingtonePicker.Settings.allSystemRingtoneTypes,
            deviceRingtoneTypes = UltimateRingtonePicker.Settings.allDeviceRingtoneTypes
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // You must check requestCode here because RingtonePickerFragment may
        // startActivityForResult internally and require super.onActivityResult here to be called.
        if (requestCode == REQUEST_CODE_ACTIVITY && resultCode == Activity.RESULT_OK) {
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

private const val REQUEST_CODE_ACTIVITY = 315
