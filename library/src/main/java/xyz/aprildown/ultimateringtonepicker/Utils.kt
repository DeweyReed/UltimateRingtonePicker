@file:Suppress("unused", "NOTHING_TO_INLINE")

package xyz.aprildown.ultimateringtonepicker

import android.content.ContentResolver
import android.content.Context
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.AnyRes
import androidx.core.content.ContextCompat
import xyz.aprildown.ultimateringtonepicker.data.CustomRingtone
import java.text.Collator

internal val MUSIC_SILENT: Uri = Uri.EMPTY
internal val NO_MUSIC_URI: Uri = Uri.EMPTY

internal fun MutableList<CustomRingtone>.sortWithCollator() {
    val collator = Collator.getInstance()
    sortWith(Comparator { o1, o2 ->
        collator.compare(o1.title, o2.title)
    })
}

internal fun Context.safeContext(): Context =
    takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isDeviceProtectedStorage }?.let {
        ContextCompat.createDeviceProtectedStorageContext(it) ?: it
    } ?: this

/**
 * @param resourceId identifies an application resource
 * @return the Uri by which the application resource is accessed
 */
internal fun Context.getResourceUri(@AnyRes resourceId: Int): Uri = Uri.Builder()
    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
    .authority(packageName)
    .path(resourceId.toString())
    .build()

internal fun isLOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

internal fun isNOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

internal fun isOOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

//
// View Helpers
//

internal fun ImageView.startDrawableAnimation() {
    (drawable as? Animatable)?.start()
}

internal fun ImageView.stopDrawableAnimation() {
    (drawable as? Animatable)?.run { if (isRunning) stop() }
}


internal inline fun View.show() {
    visibility = View.VISIBLE
}

internal inline fun View.hide() {
    visibility = View.INVISIBLE
}

internal inline fun View.gone() {
    visibility = View.GONE
}