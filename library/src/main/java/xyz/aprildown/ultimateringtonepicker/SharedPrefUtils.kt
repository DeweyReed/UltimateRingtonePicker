package xyz.aprildown.ultimateringtonepicker

import android.content.Context
import android.content.SharedPreferences
import xyz.aprildown.ultimateringtonepicker.safeContext


/**
 * This is a class that contains utils for the [SharedPreferences]
 * @author Al Mujahid Khan
 * */
object SharedPrefUtils {

    private var preferences: SharedPreferences? = null

    /**
     * This method initializes the utils
     * @param context application context
     * */
    fun init(context: Context) {
        if (preferences == null) {
            preferences = context.safeContext().getSharedPreferences(
                    "music_picker_prefs", Context.MODE_PRIVATE
            )
        }
    }

    /**
     * This method writes a value to the [SharedPreferences]
     * @param key the key we are using to keep the value
     * @param value the value we want to keep
     * @return if the value is stored or not
     * */
    fun write(key: String, value: String): Boolean {
        val editor = preferences!!.edit()
        editor.putString(key, value)
        return editor.commit()
    }

    /**
     * This method writes a value to the [SharedPreferences]
     * @param key the key we are using to keep the value
     * @param value the value we want to keep
     * @return if the value is stored or not
     * */
    fun write(key: String, value: Boolean): Boolean {
        val editor = preferences!!.edit()
        editor.putBoolean(key, value)
        return editor.commit()
    }

    /**
     * This method writes a value to the [SharedPreferences]
     * @param key the key we are using to keep the value
     * @param value the value we want to keep
     * @return if the value is stored or not
     * */
    fun write(key: String, value: Int): Boolean {
        val editor = preferences!!.edit()
        editor.putInt(key, value)
        return editor.commit()
    }

    /**
     * This method writes a value to the [SharedPreferences]
     * @param key the key we are using to keep the value
     * @param value the value we want to keep
     * @return if the value is stored or not
     * */
    fun write(key: String, value: Long): Boolean {
        val editor = preferences!!.edit()
        editor.putLong(key, value)
        return editor.commit()
    }

    /**
     * This method returns a string if there exists any string for the key at [SharedPreferences].
     * Otherwise, returns the default string given in the parameter
     *
     * @param key key to search at [SharedPreferences]
     * @param defaultString this string will be returned if no string is found using the key
     * @return desired string
     * */
    fun readString(key: String, defaultString: String = ""): String {
        return preferences!!.getString(key, defaultString)!!
    }


    /**
     * This method returns a value from the [SharedPreferences]
     * @param key the key we are using to keep the value
     * @return desired value
     * */
    fun readBoolean(key: String): Boolean {
        return preferences!!.getBoolean(key, false)
    }

    /**
     * This method returns if the [SharedPreferences] contains the key or not
     * @param key the key we are using to keep the value
     * @return if the key is available or not
     * */
    operator fun contains(key: String): Boolean {
        return preferences!!.contains(key)
    }

    /**
     * This method clears the [SharedPreferences]
     * */
    fun clear() {
        preferences!!.edit().clear().apply()
    }

    /**
     * This method deletes a value from the [SharedPreferences]
     * @param key the key we are using to keep the value
     * */
    fun delete(key: String) {
        preferences!!.edit().remove(key).apply()
    }
}