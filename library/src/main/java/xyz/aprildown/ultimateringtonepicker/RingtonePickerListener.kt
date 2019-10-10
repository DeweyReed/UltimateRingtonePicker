package xyz.aprildown.ultimateringtonepicker

interface RingtonePickerListener {
    /**
     * @param ringtones It may be empty or contain one or more entries.
     *                  You should also check Uri.EMPTY if user select the silent ringtone.
     */
    fun onRingtonePicked(ringtones: List<RingtonePickerEntry>)
}
