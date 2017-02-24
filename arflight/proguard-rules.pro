# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/saki/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class javax.jmdns.**
-dontwarn javax.jmdns.test.*
-dontwarn java.awt.*
-dontwarn org.apache.sanselan.**
-dontwarn javax.naming.**

-keepattributes *Annotation*

-keep class * extends android.os.IInterface
-keep class * extends android.os.Binder

-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

#-keep class android.net.http.** { *; }
-dontwarn android.net.http.**
-keep class android.net.compatibility.** { *; }

-dontwarn com.google.android.gms.**

-keep class com.serenegiant.net.NetworkChangedReceiver { *; }

-keep class com.parrot.arsdk.**
-keep class com.parrot.arsdk.** { *; }
-keep class com.parrot.arsdk.arsal.**
-keep class com.parrot.arsdk.arsal.** { *; }
-keep class com.parrot.arsdk.arsal.ARSALBLEManager
-keep class com.parrot.arsdk.arsal.ARSALBLEManager { *; }
-keep class com.parrot.arsdk.arsal.ARSALBLEManager.**
-keep class com.parrot.arsdk.arsal.ARSALBLEManager.** { *; }
-keep class com.parrot.arsdk.arsal.ARSALBLEManager.*$*
-keep class com.parrot.arsdk.arsal.ARSALBLEManager.*$* { *; }
-dontnote com.parrot.arsdk.arsal.ARSALBLEManager

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}
