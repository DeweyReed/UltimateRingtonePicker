package xyz.aprildown.ultimateringtonepicker.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import xyz.aprildown.ultimateringtonepicker.RingtonePickerActivity
import xyz.aprildown.ultimateringtonepicker.RingtonePickerDialog
import xyz.aprildown.ultimateringtonepicker.UltimateRingtonePicker
import java.io.File

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity(), UltimateRingtonePicker.RingtonePickerListener {

    private var currentSelectedRingtones = listOf<UltimateRingtonePicker.RingtoneEntry>()

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
            RingtonePickerActivity.getIntent(true,
                context = this,
                settings = createStandardSettings(),
                windowTitle = "Picker Picker"
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
                preSelectUris = currentSelectedRingtones.map { it.uri },
                systemRingtonePicker = UltimateRingtonePicker.SystemRingtonePicker(
                    ringtoneTypes = listOf(
                        RingtoneManager.TYPE_RINGTONE,
                        RingtoneManager.TYPE_NOTIFICATION,
                        RingtoneManager.TYPE_ALARM
                    )
                )
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
                preSelectUris = currentSelectedRingtones.map { it.uri },
                deviceRingtonePicker = UltimateRingtonePicker.DeviceRingtonePicker(
                    deviceRingtoneTypes = listOf(
                        UltimateRingtonePicker.RingtoneCategoryType.All,
                        UltimateRingtonePicker.RingtoneCategoryType.Artist,
                        UltimateRingtonePicker.RingtoneCategoryType.Album,
                        UltimateRingtonePicker.RingtoneCategoryType.Folder
                    )
                )
            ),
            "All Device Ringtones"
        ).show(supportFragmentManager, null)
    }

    fun useAdditionalRingtones(view: View) {
        startActivityForResult(
            RingtonePickerActivity.getIntent(
                context = this,
                settings = UltimateRingtonePicker.Settings(
                    preSelectUris = currentSelectedRingtones.map { it.uri },
                    systemRingtonePicker = UltimateRingtonePicker.SystemRingtonePicker(
                        defaultSection = UltimateRingtonePicker.SystemRingtonePicker.DefaultSection(
                            defaultUri = UltimateRingtonePicker.createRawRingtoneUri(
                                this,
                                R.raw.default_ringtone
                            ),
                            defaultTitle = "Default Ringtone",
                            additionalRingtones = listOf(
                                UltimateRingtonePicker.RingtoneEntry(
                                    UltimateRingtonePicker.createRawRingtoneUri(
                                        this,
                                        R.raw.short_message
                                    ),
                                    "R.raw.short_message"
                                ),
                                UltimateRingtonePicker.RingtoneEntry(
                                    UltimateRingtonePicker.createAssetRingtoneUri("asset1.wav"),
                                    "Assets/asset1.mp3"
                                ),
                                UltimateRingtonePicker.RingtoneEntry(
                                    UltimateRingtonePicker.createAssetRingtoneUri("ringtones${File.separator}asset2.mp3"),
                                    "Assets/ringtones/asset2.mp3"
                                )
                            )
                        )
                    )

                ),
                windowTitle = "Additional"
            ),
            REQUEST_CODE_ACTIVITY
        )
    }

    fun enableMultiSelect(view: View) {
        startActivityForResult(
            RingtonePickerActivity.getIntent(
                context = this,
                settings = UltimateRingtonePicker.Settings(
                    preSelectUris = currentSelectedRingtones.map { it.uri },
                    enableMultiSelect = true,
                    systemRingtonePicker = UltimateRingtonePicker.SystemRingtonePicker(
                        customSection = UltimateRingtonePicker.SystemRingtonePicker.CustomSection(),
                        defaultSection = UltimateRingtonePicker.SystemRingtonePicker.DefaultSection(
                            defaultUri = UltimateRingtonePicker.createRawRingtoneUri(
                                this,
                                R.raw.default_ringtone
                            )
                        ),
                        ringtoneTypes = listOf(
                            RingtoneManager.TYPE_RINGTONE,
                            RingtoneManager.TYPE_NOTIFICATION,
                            RingtoneManager.TYPE_ALARM
                        )
                    ),
                    deviceRingtonePicker = UltimateRingtonePicker.DeviceRingtonePicker(
                        deviceRingtoneTypes = listOf(
                            UltimateRingtonePicker.RingtoneCategoryType.All,
                            UltimateRingtonePicker.RingtoneCategoryType.Artist,
                            UltimateRingtonePicker.RingtoneCategoryType.Album,
                            UltimateRingtonePicker.RingtoneCategoryType.Folder
                        )
                    )
                ),
                windowTitle = "Multi Select"
            ),
            REQUEST_CODE_ACTIVITY
        )
    }

    fun safPick(view: View) {
        RingtonePickerDialog.createInstance(
            UltimateRingtonePicker.Settings(
                preSelectUris = currentSelectedRingtones.map { it.uri },
                systemRingtonePicker = UltimateRingtonePicker.SystemRingtonePicker(
                    customSection = UltimateRingtonePicker.SystemRingtonePicker.CustomSection(
                        useSafSelect = true
                    ),
                    ringtoneTypes = listOf(
                        RingtoneManager.TYPE_RINGTONE,
                        RingtoneManager.TYPE_NOTIFICATION,
                        RingtoneManager.TYPE_ALARM
                    )
                )
            ),
            "All Device Ringtones"
        ).show(supportFragmentManager, null)
    }

    fun onlySafPick(view: View) {
        startActivityForResult(
            RingtonePickerActivity.getIntent(
                context = this,
                settings = UltimateRingtonePicker.Settings(
                    deviceRingtonePicker = UltimateRingtonePicker.DeviceRingtonePicker(
                        alwaysUseSaf = true
                    )
                ),
                windowTitle = "SAF!"
            ),
            REQUEST_CODE_ACTIVITY
        )
    }

    fun ephemeralDialog(view: View) {
        RingtonePickerDialog.createEphemeralInstance(
            createStandardSettings(),
            "Ephemeral Dialog",
            object : UltimateRingtonePicker.RingtonePickerListener {
                override fun onRingtonePicked(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
                    toast("Ephemeral!")
                    Handler().postDelayed({
                        handleResult(ringtones)
                    }, 1000)
                }
            }
        ).show(supportFragmentManager, null)
    }

    private fun createStandardSettings(): UltimateRingtonePicker.Settings =
        UltimateRingtonePicker.Settings(
            preSelectUris = currentSelectedRingtones.map { it.uri },
            loop = false,
            systemRingtonePicker = UltimateRingtonePicker.SystemRingtonePicker(
                customSection = UltimateRingtonePicker.SystemRingtonePicker.CustomSection(),
                defaultSection = UltimateRingtonePicker.SystemRingtonePicker.DefaultSection(
                    showSilent = true,
                    defaultUri = UltimateRingtonePicker.createRawRingtoneUri(
                        this,
                        R.raw.default_ringtone
                    ),
                    defaultTitle = "Default Ringtone",
                    additionalRingtones = listOf(
                        UltimateRingtonePicker.RingtoneEntry(
                            UltimateRingtonePicker.createRawRingtoneUri(this, R.raw.short_message),
                            "R.raw.short_message"
                        )
                    )
                ),
                ringtoneTypes = listOf(
                    RingtoneManager.TYPE_RINGTONE,
                    RingtoneManager.TYPE_NOTIFICATION,
                    RingtoneManager.TYPE_ALARM
                )
            ),
            deviceRingtonePicker = UltimateRingtonePicker.DeviceRingtonePicker(
                deviceRingtoneTypes = listOf(
                    UltimateRingtonePicker.RingtoneCategoryType.All,
                    UltimateRingtonePicker.RingtoneCategoryType.Artist,
                    UltimateRingtonePicker.RingtoneCategoryType.Album,
                    UltimateRingtonePicker.RingtoneCategoryType.Folder
                )
            )
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ACTIVITY && resultCode == Activity.RESULT_OK) {
            handleResult(RingtonePickerActivity.getPickerResult(data!!))
        }
    }

    override fun onRingtonePicked(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
        handleResult(ringtones)
    }

    private fun handleResult(ringtones: List<UltimateRingtonePicker.RingtoneEntry>) {
        currentSelectedRingtones = ringtones
        toast(ringtones.joinToString(separator = "\n") { it.name })
    }
}

private const val REQUEST_CODE_ACTIVITY = 315
