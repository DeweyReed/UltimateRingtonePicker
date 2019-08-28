@file:Suppress("NOTHING_TO_INLINE", "unused")

package xyz.aprildown.ultimateringtonepicker.app

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

// region Short Toast

inline fun Context.toast(message: Int): Toast = Toast
    .makeText(this, message, Toast.LENGTH_SHORT)
    .apply {
        show()
    }

inline fun Context.toast(message: CharSequence): Toast = Toast
    .makeText(this, message, Toast.LENGTH_SHORT)
    .apply {
        show()
    }

// endregion

// region Long Toast

inline fun Context.longToast(message: Int): Toast = Toast
    .makeText(this, message, Toast.LENGTH_LONG)
    .apply {
        show()
    }

inline fun Context.longToast(message: CharSequence): Toast = Toast
    .makeText(this, message, Toast.LENGTH_LONG)
    .apply {
        show()
    }

// endregion

// region Fragment

inline fun Fragment.toast(message: Int): Toast = requireContext().toast(message)
inline fun Fragment.longToast(message: Int): Toast = requireContext().longToast(message)

// endregion
