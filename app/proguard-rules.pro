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

# ========================================
# AALAY APP SPECIFIC RULES
# ========================================

# Keep application class
-keep public class com.aalay.app.AalayApplication { *; }

# Keep all activities
-keep public class * extends android.app.Activity
-keep public class * extends androidx.activity.ComponentActivity
-keep public class * extends androidx.fragment.app.Fragment

# Keep all services
-keep public class * extends android.app.Service
-keep public class com.aalay.app.services.** { *; }

# Keep data models and DTOs
-keep class com.aalay.app.data.models.** { *; }
-keep class com.aalay.app.data.remote.** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keep class * implements java.io.Serializable { *; }

# ========================================
# ANDROID JETPACK / ANDROIDX RULES
# ========================================

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }

# Navigation Component
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.** { *; }

# LiveData
-keep class androidx.lifecycle.LiveData { *; }
-keep class androidx.lifecycle.MutableLiveData { *; }

# Room Database
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *

# Hilt/Dagger
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @javax.inject.Singleton class * { *; }

# ========================================
# THIRD PARTY LIBRARIES
# ========================================

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firestore.v1.** { *; }

# Firebase Storage
-keep class com.google.firebase.storage.** { *; }

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }

# Google Play Services
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.maps.** { *; }

# Facebook SDK
-keep class com.facebook.** { *; }
-dontwarn com.facebook.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Timber
-keep class timber.log.** { *; }
-dontwarn org.jetbrains.annotations.**

# Lottie
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Accompanist
-keep class com.google.accompanist.** { *; }

# ========================================
# KOTLIN SPECIFIC RULES
# ========================================

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Kotlin Reflection
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# Keep Kotlin metadata
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }

# ========================================
# SECURITY & OBFUSCATION
# ========================================

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove Timber logging in production
-assumenosideeffects class timber.log.Timber* {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Obfuscate API keys and sensitive strings (add your specific strings here)
-obfuscationdictionary obfuscation-dictionary.txt
-classobfuscationdictionary obfuscation-dictionary.txt
-packageobfuscationdictionary obfuscation-dictionary.txt

# ========================================
# GENERAL RULES
# ========================================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick.
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# Keep classes with @Keep annotation
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ========================================
# OPTIMIZATION
# ========================================

# Allow aggressive optimization
-allowaccessmodification
-repackageclasses ''
-dontpreverify

# Optimization passes
-optimizationpasses 5

# Don't skip non-public library classes
-dontskipnonpubliclibraryclassmembers

# Print mapping for debugging
-printmapping mapping.txt
-printseeds seeds.txt
-printusage usage.txt
