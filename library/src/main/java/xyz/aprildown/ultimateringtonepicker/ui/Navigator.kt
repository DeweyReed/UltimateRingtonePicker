package xyz.aprildown.ultimateringtonepicker.ui

internal interface Navigator {
    interface Selector {
        fun onSelect()

        /**
         * @return If the event consumed.
         */
        fun onBack(): Boolean
    }
}
