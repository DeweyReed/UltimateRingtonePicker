package xyz.aprildown.ringtone

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import xyz.aprildown.ringtone.ui.MusicPickerFragment

/**
 * Created on 2018/4/16.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class UltimateMusicPicker {
    companion object {
        @IntDef(TYPE_RINGTONE, TYPE_NOTIFICATION, TYPE_ALARM, TYPE_MUSIC)
        @Retention(AnnotationRetention.SOURCE)
        internal annotation class MusicType

        internal const val TYPE_RINGTONE = RingtoneManager.TYPE_RINGTONE
        internal const val TYPE_NOTIFICATION = RingtoneManager.TYPE_NOTIFICATION
        internal const val TYPE_ALARM = RingtoneManager.TYPE_ALARM
        internal const val TYPE_MUSIC = 8

        internal const val EXTRA_SETTING_BUNDLE = "ump_setting_bundle"
        const val EXTRA_WINDOW_TITLE = "ump_window_title"

        const val EXTRA_SELECTED_TITLE = "ump_selected_title"
        const val EXTRA_SELECTED_URI = "ump_selected_uri"

        /**
         * Useful when customize how to start activity. Check [MusicPickerActivity.onCreate].
         */
        fun createFragmentFromIntent(intent: Intent): Fragment =
            MusicPickerFragment.newInstance(
                intent.getParcelableExtra(EXTRA_SETTING_BUNDLE)
            )
    }

    private var windowTitle: String = ""

    private var hasDefault: Boolean = false
    private var defaultTitle: String = ""
    private var defaultUri: Uri = MUSIC_SILENT
    private var hasSilent: Boolean = true
    private var selectedUri: Uri? = null
    private val additional = mutableListOf<Pair<String, Uri>>()
    private var streamType: Int = AudioManager.STREAM_MUSIC
    private val musicTypes = mutableSetOf<Int>()

    /**
     * MusicPicker's activity or dialog title
     */
    fun windowTitle(t: String): UltimateMusicPicker = apply {
        windowTitle = t
    }

    /**
     * Add a default music item. Name is "default".
     */
    fun defaultUri(uri: Uri): UltimateMusicPicker = apply {
        hasDefault = true
        defaultUri = uri
    }

    /**
     * Add a default music item and its name.
     */
    fun defaultTitleAndUri(title: String, uri: Uri): UltimateMusicPicker = apply {
        hasDefault = true
        defaultTitle = title
        defaultUri = uri
    }

    /**
     * There is a "silent" item by default. Use this method to remove it.
     */
    fun removeSilent(): UltimateMusicPicker = apply {
        hasSilent = false
    }

    /**
     * Preselect a item.
     */
    fun selectUri(uri: Uri): UltimateMusicPicker = apply {
        selectedUri = uri
    }

    /**
     * Add an extra music item.
     */
    fun additional(title: String, uri: Uri) = apply {
        additional.add(title to uri)
    }

    /**
     * Set activity's volume control and music play stream type.
     */
    fun streamType(type: Int): UltimateMusicPicker = apply {
        streamType = type
    }

    /**
     * Show ringtones from [RingtoneManager.TYPE_RINGTONE].
     */
    fun ringtone(): UltimateMusicPicker = apply {
        musicTypes.add(TYPE_RINGTONE)
    }

    /**
     * Show ringtones from [RingtoneManager.TYPE_NOTIFICATION].
     */
    fun notification(): UltimateMusicPicker = apply {
        musicTypes.add(TYPE_NOTIFICATION)
    }

    /**
     * Show ringtones from [RingtoneManager.TYPE_ALARM].
     */
    fun alarm(): UltimateMusicPicker = apply {
        musicTypes.add(TYPE_ALARM)
    }

    /**
     * Show ringtones from external storage.
     */
    fun music(): UltimateMusicPicker = apply {
        musicTypes.add(TYPE_MUSIC)
    }

    /**
     * Create a setting [Parcelable]. Useful when customize how to start activity
     */
    fun buildParcelable(): Parcelable = MusicPickerSetting(
        hasDefault, defaultTitle, defaultUri, hasSilent, selectedUri,
        additional, streamType, musicTypes.toIntArray()
    )

    /**
     * Put a setting [Parcelable] into a [Intent]. Useful when customize how to start activity
     */
    fun putSettingIntoIntent(intent: Intent): Intent = intent.apply {
        putExtra(EXTRA_SETTING_BUNDLE, buildParcelable())
    }

    /**
     * Start music picking activity from an [Activity].
     * @param activity Current activity
     * @param requestCode To get picked music result
     * @param c An activity implementing [MusicPickerListener]. Just like [MusicPickerActivity].
     */
    fun goWithActivity(activity: Activity, requestCode: Int, c: Class<out AppCompatActivity>) {
        activity.startActivityForResult(
            Intent(activity, c)
                .apply {
                    putSettingIntoIntent(this)
                    putExtra(EXTRA_WINDOW_TITLE, windowTitle)
                }, requestCode
        )
    }

    /**
     * Start music picking activity from a [Fragment].
     * @param fragment Current fragment
     * @param requestCode To get picked music result
     * @param c An activity implementing [MusicPickerListener]. Just like [MusicPickerActivity].
     */
    fun goWithActivity(
        fragment: Fragment,
        requestCode: Int,
        c: Class<out AppCompatActivity>
    ) {
        fragment.startActivityForResult(
            Intent(fragment.requireContext(), c)
                .apply {
                    putSettingIntoIntent(this)
                    putExtra(EXTRA_WINDOW_TITLE, windowTitle)
                }, requestCode
        )
    }

    /**
     * Start music picking dialog.
     * Please implement [MusicPickerListener] for the class who calls this method
     * @param fm Current fragment
     */
    fun goWithDialog(fm: FragmentManager) {
        val dialog = MusicPickerDialog.newInstance(buildParcelable(), windowTitle)
        dialog.show(fm, "music_picker")
    }
}