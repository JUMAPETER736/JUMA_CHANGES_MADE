# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================================================
# Keep Parcelable classes to prevent ClassNotFoundException
# ============================================================================

# Keep Dialog and related data models
-keep class com.uyscuti.social.circuit.data.model.Dialog { *; }
-keep class com.uyscuti.social.circuit.data.model.Message { *; }
-keep class com.uyscuti.social.circuit.data.model.User { *; }

# Keep all Parcelable implementations and their CREATOR fields
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
    *;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep chatsuit library interfaces and models
-keep class com.uyscuti.social.chatsuit.commons.models.** { *; }

# Keep all data models (if you have more in the package)
-keep class com.uyscuti.social.circuit.data.model.** { *; }