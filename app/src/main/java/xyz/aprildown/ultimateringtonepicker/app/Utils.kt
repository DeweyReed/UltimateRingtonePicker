package xyz.aprildown.ultimateringtonepicker.app

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.AnyRes

/**
 * @param resourceId identifies an application resource
 * @return the Uri by which the application resource is accessed
 */
internal fun Context.getResourceUri(@AnyRes resourceId: Int): Uri = Uri.Builder()
    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
    .authority(packageName)
    .path(resourceId.toString())
    .build()
