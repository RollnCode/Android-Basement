# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/lazarus/Android/Sdk/tools/proguard/proguard-android.txt
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

-keep public class com.rollncode.** {*;}

-keepattributes Exceptions
-keepattributes Signature
-keepparameternames


#Fabric
-keep public class * extends java.lang.Exception
-keepattributes *Annotation*, SourceFile, LineNumberTable

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

#AppCompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#RoboSpice
-dontwarn com.octo.android.robospice.SpiceService

#OkHttp
-keep class com.squareup.okhttp3.** { *; }
-dontwarn okio.**

#appsflyer
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**
-dontwarn com.appsflyer.InstanceIDListener

#Common
-keepattributes Signature

-keepclassmembers class * {
    static java.lang.String *;
}