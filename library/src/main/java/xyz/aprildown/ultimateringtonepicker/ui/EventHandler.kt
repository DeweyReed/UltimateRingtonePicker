package xyz.aprildown.ultimateringtonepicker.ui

internal interface EventHandler {
    fun onSelect()

    /**
     * @return If the event consumed.
     */
    fun onBack(): Boolean
}
