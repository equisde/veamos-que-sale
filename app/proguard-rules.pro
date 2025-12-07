# Add project specific ProGuard rules here.
-keep class com.roro.lgthinq.** { *; }
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}
