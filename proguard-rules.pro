# Project-wide ProGuard rules.

# Add project specific keep rules here.

-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-keep class com.example.todo.data.* { *; }
