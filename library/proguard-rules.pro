# kotlinx.serialization https://github.com/Kotlin/kotlinx.serialization#androidjvm
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class xyz.aprildown.ultimateringtonepicker.**$$serializer { *; }
-keepclassmembers class xyz.aprildown.ultimateringtonepicker.** {
    *** Companion;
}
-keepclasseswithmembers class xyz.aprildown.ultimateringtonepicker.** {
    kotlinx.serialization.KSerializer serializer(...);
}
