package xyz.aprildown.ultimateringtonepicker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import androidx.navigation.ui.R as RNavigation

internal val RINGTONE_URI_SILENT: Uri = Uri.EMPTY
internal val RINGTONE_URI_NULL: Uri = Uri.EMPTY

internal const val TAG_RINGTONE_PICKER = "ringtone_picker"
internal const val EXTRA_SETTINGS = "settings"

internal const val ASSET_URI_PREFIX = "file:///android_asset/"

internal const val EXTRA_CATEGORY_TYPE = "category_type"
internal const val EXTRA_CATEGORY_ID = "category_id"

internal fun Context.safeContext(): Context =
    takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isDeviceProtectedStorage }?.let {
        ContextCompat.createDeviceProtectedStorageContext(it) ?: it
    } ?: this

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.LOLLIPOP)
internal fun isLOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
internal fun isOOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
internal fun isQOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

internal fun ImageView.startDrawableAnimation() {
    (drawable as? Animatable)?.start()
}

internal fun View.gone() {
    visibility = View.GONE
}

internal fun Fragment.requireRingtonePickerListener(): UltimateRingtonePicker.RingtonePickerListener =
    when {
        parentFragment is UltimateRingtonePicker.RingtonePickerListener -> parentFragment as UltimateRingtonePicker.RingtonePickerListener
        context is UltimateRingtonePicker.RingtonePickerListener -> context as UltimateRingtonePicker.RingtonePickerListener
        activity is UltimateRingtonePicker.RingtonePickerListener -> activity as UltimateRingtonePicker.RingtonePickerListener
        else -> throw IllegalStateException("Cannot find RingtonePickerListener")
    }

internal fun Fragment.launchSaf() {
    try {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("audio/*")
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                /**
                 * The docs for SAF is quite vague. I add [Intent.FLAG_GRANT_READ_URI_PERMISSION]
                 * flag following [Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION]'s doc.
                 */
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            0
        )
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_LONG).show()
    }
}

internal fun createDefaultNavOptions(): NavOptions {
    return navOptions {
        anim {
            enter = RNavigation.animator.nav_default_enter_anim
            exit = RNavigation.animator.nav_default_exit_anim
            popEnter = RNavigation.animator.nav_default_pop_enter_anim
            popExit = RNavigation.animator.nav_default_pop_exit_anim
        }
    }
}
